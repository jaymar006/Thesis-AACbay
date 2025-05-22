package com.example.ripdenver.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Ngram
import com.example.ripdenver.ui.components.CardItem
import com.example.ripdenver.viewmodels.NgramVisualizationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NgramVisualizationScreen(
    onNavigateBack: () -> Unit,
    viewModel: NgramVisualizationViewModel = hiltViewModel()
) {
    val selectedCards by viewModel.selectedCards.collectAsState()
    val predictedCards by viewModel.predictedCards.collectAsState()
    val matchingNgrams by viewModel.matchingNgrams.collectAsState()
    val predictionExplanation by viewModel.predictionExplanation.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("N-gram Visualization") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Card Selection History
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Current Sequence",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (selectedCards.isEmpty()) {
                        Text(
                            text = "Select cards to see n-gram analysis",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(selectedCards) { card ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(60.dp)
                                ) {
                                    CardItem(
                                        card = card,
                                        onClick = { /* No click action needed */ },
                                        modifier = Modifier.size(60.dp)
                                    )
                                    Text(
                                        text = card.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.width(60.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Prediction Explanation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Prediction Analysis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = predictionExplanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3. Predicted Next Cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Predicted Next Cards",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (predictedCards.isEmpty()) {
                        Text(
                            text = "No predictions available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(predictedCards) { (card, probability) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(60.dp)
                                ) {
                                    CardItem(
                                        card = card,
                                        onClick = { /* No click action needed */ },
                                        modifier = Modifier.size(60.dp)
                                    )
                                    Text(
                                        text = card.label,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.width(60.dp)
                                    )
                                    LinearProgressIndicator(
                                        progress = probability,
                                        modifier = Modifier
                                            .width(60.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                    )
                                    Text(
                                        text = "${(probability * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Matching N-gram Paths
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Matching N-gram Paths",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (matchingNgrams.isEmpty()) {
                        Text(
                            text = "No matching n-grams found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            matchingNgrams.forEach { ngram ->
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Show sequence of cards
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(ngram.sequence) { card ->
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.width(60.dp)
                                            ) {
                                                CardItem(
                                                    card = card,
                                                    onClick = { /* No click action needed */ },
                                                    modifier = Modifier.size(60.dp)
                                                )
                                                Text(
                                                    text = card.label,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.width(60.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Show frequency and matching cards
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Frequency: ${ngram.frequency}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (ngram.matchingCards.isNotEmpty()) {
                                            Text(
                                                text = "→ ${ngram.matchingCards.joinToString(" → ") { it.label }}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 