package com.example.ripdenver.ui.screens

// FolderScreen.kt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
    onAddClick: () -> Unit,
    onBack: () -> Unit,
    onClearOne: () -> Unit,
    onClearAll: () -> Unit,
    onToggleDeleteMode: (Boolean) -> Unit,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folder.name) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("main")
                    }) {
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
        ) {
            // Reused Selection Container
            SelectionContainer(
                selectedItems = selectedItems,
                onClearOne = onClearOne,
                onClearAll = onClearAll,
                onAddClick = onAddClick,
                currentFolderId = folder.id,
                onToggleDeleteMode = onToggleDeleteMode

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