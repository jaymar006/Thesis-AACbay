package com.example.ripdenver.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.ui.components.CardItem
import com.example.ripdenver.ui.components.FolderItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    cards: List<Card>,
    folders: List<Folder>,
    selectedCards: List<Card>,
    onCardClick: (Card) -> Unit,
    onFolderClick: (Folder) -> Unit,
    onAddClick: () -> Unit,
    onMicClick: () -> Unit,
    onClearSelection: () -> Unit,
    onRemoveLastSelection: () -> Unit,
    navController: NavController,
    gridColumns: Int = 6,
    isGridColumn: Boolean = true
) {

    Scaffold(

    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SelectionContainer(
                    selectedItems = selectedCards,
                    onClearOne = onRemoveLastSelection,
                    onClearAll = onClearSelection,
                    onAddClick = onAddClick
                )

                LazyVerticalGrid(
                    if(isGridColumn) GridCells.Fixed(gridColumns)
                    else GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(folders) { folder ->
                        FolderItem(
                            folder = folder,
                            onClick = {
                                onFolderClick(folder)
                                navController.navigate("folder/${folder.id}")
                            }
                        )
                    }

                    items(cards) { card ->
                        CardItem(
                            card = card,
                            onClick = {
                                onCardClick(card)
                            }
                        )
                    }
                }
            }

            // Floating buttons OVERLAY
            ControlButtons(
                onMicClick = onMicClick,
                modifier = Modifier
                    .fillMaxSize() // only needed to align
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun SelectionContainer(
    selectedItems: List<Card>,
    onClearOne: () -> Unit,
    onClearAll: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showDropdownMenu = remember { mutableStateOf(false) }

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
            selectedItems.forEach { card ->
                SelectedCardItem(card = card)
            }
        }

        // Control buttons
        IconButton(onClick = onClearOne) {
            Icon(Icons.AutoMirrored.Filled.Backspace, "Remove last")
        }
        IconButton(onClick = onClearAll) {
            Icon(Icons.Default.RemoveCircle, "Clear all")
        }
        Box {
            IconButton(onClick = { showDropdownMenu.value = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            DropdownMenu(
                expanded = showDropdownMenu.value,
                onDismissRequest = { showDropdownMenu.value = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Magdagdag ng Kard") },
                    onClick = {
                        onAddClick()
                        showDropdownMenu.value = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                )
                DropdownMenuItem(
                    text = { Text("I-edit ang Kard") },
                    onClick = {
                        showDropdownMenu.value = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                )
                DropdownMenuItem(
                    text = { Text("Tangalin ang Kard") },
                    onClick = {
                        showDropdownMenu.value = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                )
            }
        }
        IconButton(onClick = { /* Settings */ }) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
    }
}

@Composable
private fun SelectedCardItem(card: Card) {
    CardItem(
        card = card,
        onClick = {}, // No action needed for selected items
        modifier = Modifier.size(60.dp)// Smaller size for the selected card,
    )
}

@Composable
private fun ControlButtons(
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        FloatingActionButton(
            onClick = onMicClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation()
        ) {
            Icon(Icons.Default.Mic, contentDescription = "Voice input")
        }
    }
}
// Add this in a utils file or where you keep shared functions
@Composable
fun calculateItemSize(columns: Int): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val padding = 8.dp // Total horizontal padding per item
    return (screenWidth - (padding * columns)) / columns
}
