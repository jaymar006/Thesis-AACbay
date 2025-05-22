package com.example.ripdenver.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Ngram
import com.example.ripdenver.utils.AuthenticationManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class NgramAnalysis(
    val sequence: List<Card>,
    val frequency: Int,
    val matchingCards: List<Card>,
    val totalFrequency: Int
)

@HiltViewModel
class NgramVisualizationViewModel @Inject constructor() : ViewModel() {
    private val database = Firebase.database.reference

    // State for selected cards
    private val _selectedCards = MutableStateFlow<List<Card>>(emptyList())
    val selectedCards = _selectedCards.asStateFlow()

    // State for predicted cards with probabilities
    private val _predictedCards = MutableStateFlow<List<Pair<Card, Float>>>(emptyList())
    val predictedCards = _predictedCards.asStateFlow()

    // State for matching n-grams with analysis
    private val _matchingNgrams = MutableStateFlow<List<NgramAnalysis>>(emptyList())
    val matchingNgrams = _matchingNgrams.asStateFlow()

    // State for prediction explanation
    private val _predictionExplanation = MutableStateFlow<String>("")
    val predictionExplanation = _predictionExplanation.asStateFlow()

    // Cache for cards
    private val cardCache = mutableMapOf<String, Card>()

    init {
        Log.d("NgramVisualization", "Initializing ViewModel")
        loadAllCards()
    }

    private fun loadAllCards() {
        viewModelScope.launch {
            try {
                val userId = AuthenticationManager.getCurrentUserId() ?: return@launch
                Log.d("NgramVisualization", "Loading cards for user: $userId")
                val snapshot = database.child("users").child(userId).child("cards").get().await()
                snapshot.children.forEach { cardSnapshot ->
                    val card = cardSnapshot.getValue(Card::class.java)
                    card?.let {
                        cardCache[it.id] = it
                        Log.d("NgramVisualization", "Cached card: ${it.label} (${it.id})")
                    }
                }
                Log.d("NgramVisualization", "Loaded ${cardCache.size} cards")
            } catch (e: Exception) {
                Log.e("NgramVisualization", "Error loading cards", e)
            }
        }
    }

    fun getCardById(cardId: String): Card? {
        return cardCache[cardId]
    }

    fun updateSelectedCards(cards: List<Card>) {
        Log.d("NgramVisualization", "Updating selected cards: ${cards.map { it.label }}")
        _selectedCards.value = cards
        updatePredictions(cards)
        updateMatchingNgrams(cards)
    }

    private fun updatePredictions(selectedCards: List<Card>) {
        viewModelScope.launch {
            if (selectedCards.isEmpty()) {
                Log.d("NgramVisualization", "No selected cards, clearing predictions")
                _predictedCards.value = emptyList()
                _predictionExplanation.value = "Select cards to see predictions"
                return@launch
            }

            try {
                val userId = AuthenticationManager.getCurrentUserId() ?: return@launch
                val lastCardId = selectedCards.last().id
                Log.d("NgramVisualization", "Finding predictions for last card: ${selectedCards.last().label}")

                // Get all n-grams that start with the last selected card
                val snapshot = database.child("users").child(userId).child("ngrams")
                    .orderByChild("sequenceHash")
                    .startAt("${lastCardId}_")
                    .endAt("${lastCardId}_\uf8ff")
                    .get()
                    .await()

                Log.d("NgramVisualization", "Found ${snapshot.childrenCount} matching n-grams")

                // Calculate total frequency for normalization
                var totalFrequency = 0
                val cardFrequencies = mutableMapOf<String, Int>()
                val ngramDetails = mutableListOf<String>()

                snapshot.children.forEach { ngramSnapshot ->
                    val ngram = ngramSnapshot.getValue(Ngram::class.java)
                    ngram?.let {
                        val nextCardId = it.sequence.getOrNull(1)
                        if (nextCardId != null) {
                            val frequency = it.frequency
                            totalFrequency += frequency
                            cardFrequencies[nextCardId] = (cardFrequencies[nextCardId] ?: 0) + frequency
                            
                            // Add n-gram details for explanation
                            val sequence = it.sequence.mapNotNull { id -> getCardById(id) }
                            if (sequence.size >= 2) {
                                ngramDetails.add("'${sequence[0].label}' → '${sequence[1].label}' (${frequency} times)")
                            }
                        }
                    }
                }

                Log.d("NgramVisualization", "Total frequency: $totalFrequency")
                Log.d("NgramVisualization", "Card frequencies: $cardFrequencies")

                // Convert frequencies to probabilities and create predictions
                val predictions = cardFrequencies.mapNotNull { (cardId, frequency) ->
                    val card = cardCache[cardId]
                    if (card != null && totalFrequency > 0) {
                        card to (frequency.toFloat() / totalFrequency)
                    } else null
                }.sortedByDescending { it.second }

                _predictedCards.value = predictions
                Log.d("NgramVisualization", "Generated ${predictions.size} predictions")

                // Create prediction explanation
                val explanation = buildString {
                    append("Based on ${selectedCards.last().label}, the system found:\n")
                    append("• Total occurrences: $totalFrequency\n")
                    append("• Matching sequences:\n")
                    ngramDetails.forEach { detail ->
                        append("  - $detail\n")
                    }
                    if (predictions.isNotEmpty()) {
                        append("\nPredicted next cards:\n")
                        predictions.take(3).forEach { (card, prob) ->
                            append("• ${card.label}: ${(prob * 100).toInt()}%\n")
                        }
                    }
                }
                _predictionExplanation.value = explanation

            } catch (e: Exception) {
                Log.e("NgramVisualization", "Error calculating predictions", e)
                _predictedCards.value = emptyList()
                _predictionExplanation.value = "Error calculating predictions: ${e.message}"
            }
        }
    }

    private fun updateMatchingNgrams(selectedCards: List<Card>) {
        viewModelScope.launch {
            if (selectedCards.isEmpty()) {
                Log.d("NgramVisualization", "No selected cards, clearing matching n-grams")
                _matchingNgrams.value = emptyList()
                return@launch
            }

            try {
                val userId = AuthenticationManager.getCurrentUserId() ?: return@launch
                val cardIds = selectedCards.map { it.id }
                val sequenceHash = cardIds.joinToString("_")
                Log.d("NgramVisualization", "Finding matching n-grams for sequence: $sequenceHash")

                // Get all n-grams that match the current sequence
                val snapshot = database.child("users").child(userId).child("ngrams")
                    .orderByChild("sequenceHash")
                    .startAt(sequenceHash)
                    .endAt("${sequenceHash}_\uf8ff")
                    .get()
                    .await()

                Log.d("NgramVisualization", "Found ${snapshot.childrenCount} matching n-grams")

                val matchingNgrams = snapshot.children.mapNotNull { ngramSnapshot ->
                    val ngram = ngramSnapshot.getValue(Ngram::class.java)
                    ngram?.let {
                        val sequence = it.sequence.mapNotNull { id -> getCardById(id) }
                        if (sequence.isNotEmpty()) {
                            NgramAnalysis(
                                sequence = sequence,
                                frequency = it.frequency,
                                matchingCards = sequence.drop(selectedCards.size),
                                totalFrequency = it.frequency
                            )
                        } else null
                    }
                }.sortedByDescending { it.frequency }

                _matchingNgrams.value = matchingNgrams
                Log.d("NgramVisualization", "Processed ${matchingNgrams.size} matching n-grams")

            } catch (e: Exception) {
                Log.e("NgramVisualization", "Error finding matching n-grams", e)
                _matchingNgrams.value = emptyList()
            }
        }
    }
} 