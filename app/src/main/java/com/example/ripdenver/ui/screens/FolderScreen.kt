package com.example.ripdenver.ui.screens

// FolderScreen.kt
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.viewmodels.MainViewModel

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
    isEditMode: Boolean = false,
    onToggleEditMode: (Boolean) -> Unit = {},
    navController: NavController,
    isDeleteMode: Boolean,
    itemsToDelete: List<Any>,
    onToggleItemForDeletion: (Any) -> Unit,
    onDeleteSelectedItems: () -> Unit,
    mainViewModel: MainViewModel // Add this parameter
) {
    val showDeleteConfirmation = remember { mutableStateOf(false) }
    
    Log.d("FolderScreen", "isEditMode: $isEditMode") // Debug log

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
        },
        floatingActionButton = {
            if (isEditMode) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Cancel FAB
                    FloatingActionButton(
                        onClick = { onToggleEditMode(false) },
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Icon(Icons.Default.Close, "Cancel Edit")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                SelectionContainer(
                    selectedItems = selectedItems,
                    onClearOne = onClearOne,
                    onClearAll = onClearAll,
                    onAddClick = onAddClick,
                    currentFolderId = folder.id,
                    onToggleDeleteMode = onToggleDeleteMode,
                    onToggleEditMode = { newEditMode ->
                        Log.d("FolderScreen", "Toggle Edit Mode called: $newEditMode") // Debug log
                        onToggleEditMode(newEditMode)
                    },
                    isEditMode = isEditMode,
                    navController = navController,
                    mainViewModel = mainViewModel
                )

                // Add PredictiveContainer here
                if (mainViewModel.showPredictions.collectAsState().value) {
                    PredictiveContainer(
                        selectedCards = selectedItems,
                        onCardClick = onCardClick,
                        mainViewModel = mainViewModel
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(cards) { card ->
                        Box {
                            CardListItem(
                                card = card,
                                isEditMode = isEditMode,
                                isDeleteMode = isDeleteMode,
                                isSelected = card in itemsToDelete,
                                isDragging = false,
                                onClick = {
                                    if (isDeleteMode) {
                                        onToggleItemForDeletion(card)
                                    } else if (isEditMode) {
                                        Log.d("FolderScreen", "Navigating to edit card: ${card.id}") // Debug log
                                        navController.navigate("edit_card/${card.id}")
                                    } else {
                                        onCardClick(card)
                                    }
                                },
                                onToggleDelete = { onToggleItemForDeletion(card) },
                                onDragStart = {},
                                mainViewModel = mainViewModel
                            )
                            if (isDeleteMode) {
                                Checkbox(
                                    checked = card in itemsToDelete,
                                    onCheckedChange = { onToggleItemForDeletion(card) },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                )
                            }
                        }
                    }
                }
            }

            // Delete mode FABs
            if (isDeleteMode) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { onToggleDeleteMode(false) },
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Icon(Icons.Default.Close, "Cancel")
                    }
                    FloatingActionButton(
                        onClick = {
                            if (itemsToDelete.isNotEmpty()) {
                                showDeleteConfirmation.value = true
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            }

            // Delete confirmation dialog
            if (showDeleteConfirmation.value) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation.value = false },
                    title = { Text("Kumpirmahin ang Pagtanggal") },
                    text = { Text("Sigurado ka bang gusto mong tanggalin ang mga ito? Hindi na ito maibabalik pa.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onDeleteSelectedItems()
                                showDeleteConfirmation.value = false
                            }
                        ) {
                            Text("Tanggalin")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteConfirmation.value = false }
                        ) {
                            Text("Kanselahin")
                        }
                    }
                )
            }
        }
    }
}
