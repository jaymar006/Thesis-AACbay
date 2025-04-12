package com.example.ripdenver.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // This imports all default icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ripdenver.models.Card
import com.example.ripdenver.ui.components.CardItem
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Backspace
import com.example.ripdenver.models.Folder
import com.example.ripdenver.ui.components.FolderItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    cards: List<Card>,
    folders: List<Folder>,  // Add folders parameter
    onCardClick: (String) -> Unit,
    onFolderClick: (String) -> Unit,  // Add folder click handler
    onAddClick: () -> Unit,
    onMicClick: () -> Unit
) {
    var selectedItems by remember { mutableStateOf<List<Any>>(emptyList()) }  // Can hold both Cards and Folders

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ACCBay") },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, "Settings")
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
            // Selection Container
            SelectionContainer(
                selectedItems = selectedItems,
                onClearOne = { selectedItems = selectedItems.dropLast(1) },
                onClearAll = { selectedItems = emptyList() }
            )

            // Main Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Folders Section
                items(folders) { folder ->
                    FolderItem(
                        folder = folder,
                        onClick = {
                            selectedItems = selectedItems + folder
                            onFolderClick(folder.id)
                        }
                    )
                }

                // Cards Section
                items(cards) { card ->
                    CardItem(
                        card = card,
                        onClick = {
                            selectedItems = selectedItems + card
                            onCardClick(card.vocalization)
                        }
                    )
                }
            }

            // Control Buttons
            ControlButtons(
                onAddClick = onAddClick,
                onMicClick = onMicClick
            )
        }
    }
}

@Composable
private fun SelectionContainer(
    selectedItems: List<Any>,
    onClearOne: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selected items
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            selectedItems.forEach { item ->
                when (item) {
                    is Card -> SelectedCardItem(card = item)
                    is Folder -> SelectedFolderItem(folder = item)
                }
            }
        }

        // Control buttons
        IconButton(onClick = onClearOne) {
            Icon(Icons.Default.Backspace, "Remove last")
        }
        IconButton(onClick = onClearAll) {
            Icon(Icons.Default.Delete, "Clear all")
        }
    }
}

@Composable
private fun SelectedFolderItem(folder: Folder) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(
                color = Color(android.graphics.Color.parseColor(folder.color)),
                shape = MaterialTheme.shapes.small
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = folder.name.take(3),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SelectedCardItem(card: Card) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(
                color = Color(android.graphics.Color.parseColor(card.color)),
                shape = MaterialTheme.shapes.small
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = card.label.take(3), // Show first 3 letters
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ControlButtons(
    onAddClick: () -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(Modifier.width(8.dp))
            Text("Add")
        }

        IconButton(
            onClick = onMicClick,
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        ) {
            Icon(
                Icons.Default.Mic,
                "Voice input",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}