package com.example.ripdenver.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.models.Ngram
import com.example.ripdenver.utils.AuthenticationManager
import com.example.ripdenver.utils.CloudinaryManager
import com.example.ripdenver.utils.DefaultContent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class SortType {
    FOLDER_FIRST,
    CARD_FIRST,
    UNSORTED,
    BY_LABEL_ASC,
    BY_LABEL_DESC,
    BY_COLOR,
    BY_USAGE
}

class MainViewModel : ViewModel() {
    private val database = Firebase.database.reference

    // Data States
    private val _predictedCards = MutableStateFlow<List<Card>>(emptyList())
    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    private val _selectedCards = MutableStateFlow<List<Card>>(emptyList())
    private val _isDeleteMode = MutableStateFlow(false)
    private val _itemsToDelete = MutableStateFlow<List<Any>>(emptyList())
    private val _isEditMode = MutableStateFlow(false)
    private val _sortedItems = MutableStateFlow<List<Any>>(emptyList())
    private val _lastSortType = MutableStateFlow(SortType.UNSORTED)
    private val _isOffline = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(true)

    val cards = _cards.asStateFlow()
    val folders = _folders.asStateFlow()
    val selectedCards = _selectedCards.asStateFlow()
    val isDeleteMode = _isDeleteMode.asStateFlow()
    val itemsToDelete = _itemsToDelete.asStateFlow()
    val sortedItems = _sortedItems.asStateFlow()
    val isEditMode = _isEditMode.asStateFlow()
    val lastSortType = _lastSortType.asStateFlow()
    val predictedCards = _predictedCards.asStateFlow()
    val isOffline = _isOffline.asStateFlow()
    val isLoading = _isLoading.asStateFlow()

    private val _columnCount = MutableStateFlow(6)
    val columnCount = _columnCount.asStateFlow()

    private val _showPredictions = MutableStateFlow(true)
    val showPredictions = _showPredictions.asStateFlow()

    private var itemOrderPreference = MutableStateFlow(ItemOrder.UNSORTED)
    private enum class ItemOrder {
        FOLDER_FIRST,
        CARD_FIRST,
        UNSORTED
    }

    init {
        initializeUser()
    }

    private fun initializeUser() {
        viewModelScope.launch {
            try {
                val userId = AuthenticationManager.signInAnonymously()
                loadUserData(userId)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to initialize user", e)
                _isOffline.value = true
            }
        }
    }

    private fun loadUserData(userId: String) {
        loadCards(userId)
        loadFolders(userId)
        observeGridSettings(userId)
        _isLoading.value = false
    }

    private fun loadCards(userId: String) {
        database.child("users").child(userId).child("cards")
            .orderByChild("order")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _cards.value = snapshot.children.mapNotNull { it.getValue(Card::class.java) }
                    updateSortedItems()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainViewModel", "Failed to load cards", error.toException())
                }
            })
    }

    private fun loadFolders(userId: String) {
        database.child("users").child(userId).child("folders")
            .orderByChild("order")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _folders.value = snapshot.children.mapNotNull { it.getValue(Folder::class.java) }
                    updateSortedItems()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainViewModel", "Failed to load folders", error.toException())
                }
            })
    }

    private fun observeGridSettings(userId: String) {
        database.child("users").child(userId).child("settings")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.child("columnCount").getValue(Int::class.java)?.let {
                        _columnCount.value = it
                    }
                    snapshot.child("showPredictions").getValue(Boolean::class.java)?.let {
                        _showPredictions.value = it
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainViewModel", "Failed to load grid settings", error.toException())
                }
            })
    }

    // Predict the next card
    fun predictNextCards(selectedCards: List<Card>) {
        viewModelScope.launch {
            val userId = AuthenticationManager.getCurrentUserId() ?: return@launch
            if (selectedCards.isEmpty()) {
                _predictedCards.value = emptyList()
                return@launch
            }

            val lastCardId = selectedCards.last().id
            database.child("users").child(userId).child("ngrams")
                .orderByChild("sequenceHash")
                .startAt("${lastCardId}_")
                .endAt("${lastCardId}_\uf8ff")
                .get()
                .addOnSuccessListener { snapshot ->
                    val predictedCardIds = mutableSetOf<String>()
                    snapshot.children.forEach { ngramSnapshot ->
                        val ngram = ngramSnapshot.getValue(Ngram::class.java)
                        ngram?.sequence?.getOrNull(1)?.let { nextCardId ->
                            predictedCardIds.add(nextCardId)
                        }
                    }

                    val predictions = predictedCardIds
                        .mapNotNull { id -> cards.value.find { it.id == id } }
                        .sortedByDescending { card ->
                            snapshot.children
                                .firstOrNull {
                                    it.getValue(Ngram::class.java)?.sequence?.get(1) == card.id
                                }
                                ?.getValue(Ngram::class.java)
                                ?.frequency ?: 0
                        }

                    _predictedCards.value = predictions
                }
        }
    }

    // Add this new function to save ngrams
    fun saveNgram(selectedCards: List<Card>) {
        viewModelScope.launch {
            val userId = AuthenticationManager.getCurrentUserId() ?: return@launch
            if (selectedCards.size < 2) {
                Log.d("MainViewModel", "Not enough cards for ngram (${selectedCards.size} cards)")
                return@launch
            }

            try {
                // Create sequence of card IDs
                val sequence = selectedCards.map { it.id }
                val sequenceHash = sequence.joinToString("_")
                Log.d("MainViewModel", "Saving ngram with sequence: $sequenceHash")

                // Check if this ngram already exists
                val ngramSnapshot = database.child("users").child(userId).child("ngrams")
                    .orderByChild("sequenceHash")
                    .equalTo(sequenceHash)
                    .get()
                    .await()

                if (ngramSnapshot.exists()) {
                    // Update existing ngram
                    ngramSnapshot.children.firstOrNull()?.let { existingNgram ->
                        val ngram = existingNgram.getValue(Ngram::class.java)
                        ngram?.let {
                            val updatedNgram = it.increment()
                            Log.d("MainViewModel", "Updating existing ngram: ${existingNgram.key}")
                            database.child("users").child(userId).child("ngrams")
                                .child(existingNgram.key!!)
                                .setValue(updatedNgram)
                                .await()
                        }
                    }
                } else {
                    // Create new ngram
                    val ngram = Ngram(
                        userId = userId,
                        sequence = sequence,
                        frequency = 1,
                        lastUsed = System.currentTimeMillis(),
                        sequenceHash = sequenceHash
                    )
                    Log.d("MainViewModel", "Creating new ngram with sequence: $sequenceHash")
                    database.child("users").child(userId).child("ngrams")
                        .push()
                        .setValue(ngram)
                        .await()
                }
                Log.d("MainViewModel", "Successfully saved ngram")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error saving ngram", e)
            }
        }
    }

    // Check if online :>
    fun checkConnectivity() {
        viewModelScope.launch {
            try {
                Firebase.database.reference.child(".info/connected")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val connected = snapshot.getValue(Boolean::class.java) ?: false
                            _isOffline.value = !connected
                        }

                        override fun onCancelled(error: DatabaseError) {
                            _isOffline.value = true
                        }
                    })
            } catch (e: Exception) {
                _isOffline.value = true
            }
        }
    }

    fun toggleEditMode(enabled: Boolean) {
        _isEditMode.value = enabled
        if (!enabled) {
            // Reset any edit-related state if needed
        }
    }

    // Selection Management
    fun addToSelection(card: Card) {
        _selectedCards.update { current ->
            val newSelection = current + card
            newSelection
        }
    }

    fun addCardToSelection(card: Card) {
        _selectedCards.update { current ->
            val newSelection = current + card
            newSelection
        }
    }

    fun removeLastSelection() {
        _selectedCards.update { current ->
            if (current.isNotEmpty()) current.dropLast(1) else current
        }
    }

    fun clearSelection() {
        _selectedCards.update { emptyList() }
    }

    // Folder Operations
    fun getCardsInFolder(folderId: String): List<Card> {
        return _cards.value.filter { it.folderId == folderId }
    }

    fun getFolderById(folderId: String): Folder? {
        return _folders.value.find { it.id == folderId }
    }

    // Data Loading
    private fun updateSortedItems() {
        val allItems = mutableListOf<Any>()
        allItems.addAll(_folders.value)
        allItems.addAll(_cards.value.filter { it.folderId.isEmpty() })

        // Apply current sorting if any
        when (itemOrderPreference.value) {
            ItemOrder.FOLDER_FIRST -> allItems.sortBy { it !is Folder }
            ItemOrder.CARD_FIRST -> allItems.sortBy { it !is Card }
            ItemOrder.UNSORTED -> {} // No sorting needed
        }

        _sortedItems.value = allItems
    }

    fun sortItems(sortType: SortType) = viewModelScope.launch {
        _lastSortType.value = sortType
        val allItems = mutableListOf<Any>()
        allItems.addAll(_folders.value)
        allItems.addAll(_cards.value.filter { it.folderId.isEmpty() })

        // Update item order preference if sorting by card/folder
        when (sortType) {
            SortType.FOLDER_FIRST -> itemOrderPreference.value = ItemOrder.FOLDER_FIRST
            SortType.CARD_FIRST -> itemOrderPreference.value = ItemOrder.CARD_FIRST
            SortType.UNSORTED -> itemOrderPreference.value = ItemOrder.UNSORTED
            else -> {} // Keep current preference
        }

        // First sort by type (if needed)
        val typeOrderedItems = when (itemOrderPreference.value) {
            ItemOrder.FOLDER_FIRST -> allItems.sortedBy { it !is Folder }
            ItemOrder.CARD_FIRST -> allItems.sortedBy { it !is Card }
            ItemOrder.UNSORTED -> allItems
        }

        // Then apply additional sorting criteria
        val sortedItems = when (sortType) {
            SortType.FOLDER_FIRST,
            SortType.CARD_FIRST,
            SortType.UNSORTED -> typeOrderedItems
            SortType.BY_LABEL_ASC -> typeOrderedItems.sortedWith(
                compareBy<Any> {
                    // First sort by card/folder according to preference
                    when (itemOrderPreference.value) {
                        ItemOrder.FOLDER_FIRST -> if (it is Folder) 0 else 1
                        ItemOrder.CARD_FIRST -> if (it is Card) 0 else 1
                        ItemOrder.UNSORTED -> 0
                    }
                }.thenBy {
                    // Then sort by label/name ignoring case
                    when (it) {
                        is Card -> it.label.lowercase()
                        is Folder -> it.name.lowercase()
                        else -> ""
                    }
                }
            )
            SortType.BY_LABEL_DESC -> typeOrderedItems.sortedWith(
                compareBy<Any> {
                    // First sort by card/folder according to preference
                    when (itemOrderPreference.value) {
                        ItemOrder.FOLDER_FIRST -> if (it is Folder) 0 else 1
                        ItemOrder.CARD_FIRST -> if (it is Card) 0 else 1
                        ItemOrder.UNSORTED -> 0
                    }
                }.thenByDescending {
                    // Then sort by label/name ignoring case
                    when (it) {
                        is Card -> it.label.lowercase()
                        is Folder -> it.name.lowercase()
                        else -> ""
                    }
                }
            )
            SortType.BY_COLOR -> typeOrderedItems.sortedWith(
                compareBy<Any> {
                    when (itemOrderPreference.value) {
                        ItemOrder.FOLDER_FIRST -> it !is Folder
                        ItemOrder.CARD_FIRST -> it !is Card
                        ItemOrder.UNSORTED -> false
                    }
                }.thenBy {
                    when (it) {
                        is Card -> it.color
                        is Folder -> it.color
                        else -> ""
                    }
                }
            )
            SortType.BY_USAGE -> typeOrderedItems.sortedWith(
                compareBy<Any> {
                    when (itemOrderPreference.value) {
                        ItemOrder.FOLDER_FIRST -> it !is Folder
                        ItemOrder.CARD_FIRST -> it !is Card
                        ItemOrder.UNSORTED -> false
                    }
                }.thenBy {
                    when (it) {
                        is Card -> -it.usageCount
                        else -> 0
                    }
                }
            )
        }

        // Update Firebase order
        sortedItems.forEachIndexed { index, item ->
            when (item) {
                is Folder -> {
                    database.child("users").child(AuthenticationManager.getCurrentUserId() ?: "").child("folders")
                        .child(item.id)
                        .child("order")
                        .setValue(index)
                }
                is Card -> {
                    database.child("users").child(AuthenticationManager.getCurrentUserId() ?: "").child("cards")
                        .child(item.id)
                        .child("order")
                        .setValue(index)
                }
            }
        }

        _sortedItems.value = sortedItems
    }
    //Card Operation (Delete)
    fun toggleDeleteMode(enabled: Boolean) {
        _isDeleteMode.value = enabled
        if (!enabled) {
            _itemsToDelete.value = emptyList()
        }
    }

    fun toggleItemForDeletion(item: Any) {
        _itemsToDelete.update { current ->
            if (item in current) current - item else current + item
        }
    }

    // Also modify deleteSelectedItems to update UI immediately
    fun deleteSelectedItems() = viewModelScope.launch {
        try {
            _itemsToDelete.value.forEach { item ->
                when (item) {
                    is Card -> deleteCard(item)
                    is Folder -> deleteFolder(item)
                }
            }
            // Update sorted items immediately after deletion
            updateSortedItems()
        } finally {
            toggleDeleteMode(false)
        }
    }

    private suspend fun deleteCard(card: Card) {
        val userId = AuthenticationManager.getCurrentUserId() ?: return
        try {
            // Check if this is a default card by comparing its properties with DefaultContent
            val isDefaultCard = DefaultContent.defaultCards.any { defaultCard ->
                defaultCard["label"] == card.label &&
                defaultCard["vocalization"] == card.vocalization &&
                defaultCard["color"] == card.color &&
                defaultCard["folderId"] == card.folderId &&
                defaultCard["cloudinaryPublicId"] == card.cloudinaryPublicId
            }

            // Only delete from Cloudinary if it's not a default card
            if (!isDefaultCard && card.cloudinaryPublicId.isNotEmpty()) {
                val deleteResult = CloudinaryManager.deleteImage(card.cloudinaryPublicId)
                Log.d("CardDeletion", "Cloudinary deletion result: $deleteResult")
            }

            database.child("users").child(userId).child("cards").child(card.id).removeValue().await()
        } catch (e: Exception) {
            Log.e("CardDeletion", "Error deleting card", e)
            throw e
        }
    }

    private suspend fun deleteFolder(folder: Folder) {
        val userId = AuthenticationManager.getCurrentUserId() ?: return
        try {
            val snapshot = database.child("users").child(userId).child("cards")
                .orderByChild("folderId")
                .equalTo(folder.id)
                .get()
                .await()

            snapshot.children.forEach { cardSnapshot ->
                val card = cardSnapshot.getValue(Card::class.java)
                card?.let { deleteCard(it) }
            }

            database.child("users").child(userId).child("folders").child(folder.id).removeValue().await()
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error deleting folder", e)
        }
    }
}