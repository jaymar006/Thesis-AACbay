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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    isGridColumn: Boolean = true,
    isDeleteMode: Boolean,
    itemsToDelete: List<Any>,
    onToggleDeleteMode: (Boolean) -> Unit,
    onToggleItemForDeletion: (Any) -> Unit,
    onDeleteSelectedItems: () -> Unit
) {
    val unassignedCards = cards.filter { it.folderId == null || it.folderId.isEmpty() }
    val showDeleteConfirmation = remember { mutableStateOf(false)}
    Scaffold(
        floatingActionButton = {
            if (isDeleteMode) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
            } else {
                // Your existing mic button
                ControlButtons(
                    onMicClick = onMicClick,
                    modifier = Modifier
                        .fillMaxSize() // only needed to align
                        .padding(top = 16.dp)
                )
            }
        }
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
                    onAddClick = onAddClick,
                    onToggleDeleteMode = onToggleDeleteMode
                )

                LazyVerticalGrid(
                    if(isGridColumn) GridCells.Fixed(gridColumns)
                    else GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(folders) {  folder ->
                        Box {
                            FolderItem(folder = folder, onClick = {
                                if (isDeleteMode) {
                                    onToggleItemForDeletion(folder)
                                } else {
                                    onFolderClick(folder)
                                }
                            })
                            if (isDeleteMode) {
                                Checkbox(
                                    checked = folder in itemsToDelete,
                                    onCheckedChange = { onToggleItemForDeletion(folder) },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                )
                            }
                        }
                    }
                    items(unassignedCards) { card ->
                        Box {
                            CardItem(card = card, onClick = {
                                if (isDeleteMode) {
                                    onToggleItemForDeletion(card)
                                } else {
                                    onCardClick(card)
                                }
                            })
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


            // Floating buttons OVERLAY

        }
    }
}

@Composable
fun SelectionContainer(
    selectedItems: List<Card>,
    onClearOne: () -> Unit,
    onClearAll: () -> Unit,
    onAddClick: () -> Unit,
    onToggleDeleteMode: (Boolean) -> Unit,
    currentFolderId: String? = null,
    modifier: Modifier = Modifier
) {
    val showDropdownMenu = remember { mutableStateOf(false) }
    val lazyListState: LazyListState = rememberLazyListState()

    LaunchedEffect(selectedItems.size) {
        if (selectedItems.isNotEmpty()) {
            lazyListState.animateScrollToItem(selectedItems.size - 1)
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Scrollable selected items
        androidx.compose.foundation.lazy.LazyRow(
            state = lazyListState,
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(selectedItems) { card ->
                SelectedCardItem(
                    card = card,
                    modifier = Modifier.size(60.dp)
                )
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
                    text = {
                        Text(
                            if (currentFolderId != null) "Magdagdag ng Kard sa Folder"
                            else "Magdagdag ng Kard/Folder"
                        )
                    },
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
                        onToggleDeleteMode(true)
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
private fun SelectedCardItem(
    card: Card,
    modifier: Modifier = Modifier
) {
    Box(
    ) {
        CardItem(
            card = card,
            onClick = {},
            modifier.fillMaxSize()
        )
    }
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

