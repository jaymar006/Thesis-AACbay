package com.example.ripdenver.ui.screens

// FolderScreen.kt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.ui.components.CardItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    folder: Folder,
    cards: List<Card>,
    selectedItems: List<Card>,
    onCardClick: (Card) -> Unit,
    onBack: () -> Unit,
    onClearOne: () -> Unit,
    onClearAll: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folder.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Reused Selection Container
            SelectionContainer(
                selectedItems = selectedItems,
                onClearOne = onClearOne,
                onClearAll = onClearAll
            )

            // Cards Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(cards) { card ->
                    CardItem(
                        card = card,
                        onClick = { onCardClick(card) }
                    )
                }
            }
        }
    }
}