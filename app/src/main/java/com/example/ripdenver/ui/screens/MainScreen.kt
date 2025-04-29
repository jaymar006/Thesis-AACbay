package com.example.ripdenver.ui.screens

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ripdenver.AACbayApplication
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.models.Ngram
import com.example.ripdenver.ui.components.CardItem
import com.example.ripdenver.ui.components.FolderItem
import com.example.ripdenver.viewmodels.MainViewModel
import com.example.ripdenver.viewmodels.SortType
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable

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
    isEditMode: Boolean,
    onToggleEditMode: (Boolean) -> Unit,
    isDeleteMode: Boolean,
    itemsToDelete: List<Any>,
    onToggleDeleteMode: (Boolean) -> Unit,
    onToggleItemForDeletion: (Any) -> Unit,
    onDeleteSelectedItems: () -> Unit,
    mainViewModel: MainViewModel,
) {


    val unassignedCards = cards.filter { it.folderId.isEmpty() }
    val showDeleteConfirmation = remember { mutableStateOf(false)}
    val items = remember { mutableStateListOf<Any>() }
    val database = Firebase.database.reference
    val showSortMenu = remember { mutableStateOf(false) }
    val sortedItems = mainViewModel.sortedItems.collectAsState()
    val isOffline = mainViewModel.isOffline.collectAsState().value
    val currentColumnCount = mainViewModel.columnCount.collectAsState().value
    val showPredictions = mainViewModel.showPredictions.collectAsState().value

    LaunchedEffect(Unit) {
        mainViewModel.checkConnectivity()
    }

    LaunchedEffect(sortedItems.value, folders, unassignedCards) {
        if (sortedItems.value.isNotEmpty()) {
            items.clear()
            items.addAll(sortedItems.value)
        } else {
            items.clear()
            items.addAll(folders)
            items.addAll(unassignedCards)
        }
    }


    val reorderableState = rememberReorderableLazyGridState(
        onMove = { from, to ->
            items.apply {
                val item = this[from.index]
                removeAt(from.index)
                add(to.index, item)

                // Update orders in Firebase after reordering
                forEachIndexed { index, item ->
                    when (item) {
                        is Folder -> {
                            database.child("folders")
                                .child(item.id)
                                .child("order")
                                .setValue(index)
                        }
                        is Card -> {
                            database.child("cards")
                                .child(item.id)
                                .child("order")
                                .setValue(index)
                        }
                    }
                }
            }
        }
    )


    Scaffold(
        floatingActionButton = {
            if (!isDeleteMode && !isEditMode && !isOffline) {
                ControlButtons(
                    navController = navController,
                    onMicClick = onMicClick,
                    modifier = Modifier
                )
            } else if (isDeleteMode) {
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
            } else if (isEditMode) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sort FAB
                    FloatingActionButton(
                        onClick = { showSortMenu.value = true },
                        containerColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Sort, "Sort items")
                    }
                    // Cancel FAB
                    FloatingActionButton(
                        onClick = { onToggleEditMode(false) },
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Icon(Icons.Default.Close, "Cancel Edit")
                    }

                    if (showSortMenu.value) {
                        DropdownMenu(
                            expanded = showSortMenu.value,
                            onDismissRequest = { showSortMenu.value = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Folder First") },
                                onClick = {
                                    mainViewModel.sortItems(SortType.FOLDER_FIRST)
                                    showSortMenu.value = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Card First") },
                                onClick = {
                                    mainViewModel.sortItems(SortType.CARD_FIRST)
                                    showSortMenu.value = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Unsorted") },
                                onClick = {
                                    mainViewModel.sortItems(SortType.UNSORTED)
                                    showSortMenu.value = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("By Label (A-Z)") },
                                onClick = {
                                    mainViewModel.sortItems(SortType.BY_LABEL_ASC)
                                    showSortMenu.value = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("By Label (Z-A)") },
                                onClick = {
                                    mainViewModel.sortItems(SortType.BY_LABEL_DESC)
                                    showSortMenu.value = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("By Color") },
                                onClick = {
                                    mainViewModel.sortItems(SortType.BY_COLOR)
                                    showSortMenu.value = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("By Usage") },
                                onClick = {
                                    mainViewModel.sortItems(SortType.BY_USAGE)
                                    showSortMenu.value = false
                                }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()
        ) {
            if (isOffline) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val rotation = remember { Animatable(0f) }

                    LaunchedEffect(Unit) {
                        while (true) {
                            rotation.animateTo(
                                targetValue = rotation.value + 360f,
                                animationSpec = tween(
                                    durationMillis = 1000,
                                    easing = LinearEasing
                                )
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Loading",
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                rotationZ = rotation.value
                            },
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reconnecting...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    SelectionContainer(
                        selectedItems = selectedCards,
                        onClearOne = onRemoveLastSelection,
                        onClearAll = onClearSelection,
                        onAddClick = onAddClick,
                        onToggleDeleteMode = onToggleDeleteMode,
                        onToggleEditMode = onToggleEditMode,
                        isEditMode = isEditMode,
                        navController = navController
                    )

                    if (showPredictions) {
                        PredictiveContainer(
                            selectedCards = selectedCards,
                            onCardClick = onCardClick,
                            mainViewModel = mainViewModel
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(currentColumnCount),
                        modifier = Modifier
                            .weight(1f)
                            .reorderable(reorderableState)
                            .detectReorderAfterLongPress(reorderableState)
                            .padding(8.dp),
                        state = reorderableState.gridState
                    ) {

                        items(
                            items = items,
                            key = { item ->
                                when (item) {
                                    is Folder -> "folder_${item.id}"
                                    is Card -> "card_${item.id}"
                                    else -> ""
                                }
                            }
                        ) { item ->
                            val isDragging = remember { mutableStateOf(false) }
                            ReorderableItem(
                                reorderableState = reorderableState,
                                key = item
                            ) { isDragging ->
                                when (item) {
                                    is Card -> CardListItem(
                                        card = item,
                                        isEditMode = isEditMode,
                                        isDeleteMode = isDeleteMode,
                                        isSelected = item in itemsToDelete,
                                        isDragging = isDragging,
                                        onClick = {
                                            when {
                                                isDeleteMode -> onToggleItemForDeletion(item)
                                                isEditMode -> navController.navigate("edit_card/${item.id}")
                                                else -> onCardClick(item)
                                            }
                                        },
                                        onToggleDelete = { onToggleItemForDeletion(item) }
                                    )

                                    is Folder -> FolderListItem(
                                        folder = item,
                                        isEditMode = isEditMode,
                                        isDeleteMode = isDeleteMode,
                                        isSelected = item in itemsToDelete,
                                        isDragging = isDragging,
                                        onClick = {
                                            when {
                                                isDeleteMode -> onToggleItemForDeletion(item)
                                                isEditMode -> navController.navigate("edit_folder/${item.id}")
                                                else -> onFolderClick(item)
                                            }
                                        },
                                        onToggleDelete = { onToggleItemForDeletion(item) }
                                    )
                                }
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
    onToggleEditMode: (Boolean) -> Unit,
    isEditMode: Boolean,
    currentFolderId: String? = null,
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val showDropdownMenu = remember { mutableStateOf(false) }
    val lazyListState: LazyListState = rememberLazyListState()
    val context = LocalContext.current
    val tts = remember {
        (context.applicationContext as AACbayApplication).ttsManager
    }
    val database = Firebase.database.reference


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
            .padding(horizontal = 5.dp)
            .clickable {
                // Update usage for all selected cards
                selectedItems.forEach { card ->
                    val cardRef = database.child("cards").child(card.id)

                    // First get the current value from database
                    cardRef.get().addOnSuccessListener { snapshot ->
                        val currentUsageCount = snapshot.child("usageCount").getValue(Int::class.java) ?: 0

                        cardRef.updateChildren(
                            mapOf(
                                "usageCount" to (currentUsageCount + 1),
                                "lastUsed" to System.currentTimeMillis()
                            )
                        )
                    }
                }

                // Create bigrams if there are 2 or more cards
                if (selectedItems.size >= 2) {
                    for (i in 0 until selectedItems.size - 1) {
                        val pair = listOf(selectedItems[i].id, selectedItems[i + 1].id)
                        val ngramHash = pair.joinToString("_")

                        // Update or create bigram
                        val ngramRef = database.child("ngrams").child(ngramHash)
                        ngramRef.get().addOnSuccessListener { snapshot ->
                            val existing = snapshot.getValue(Ngram::class.java)
                            val updated = if (existing != null) {
                                existing.increment()
                            } else {
                                Ngram(
                                    sequence = pair,
                                    sequenceHash = ngramHash
                                )
                            }
                            ngramRef.setValue(updated)
                        }
                    }
                }

                // Speak text (existing TTS logic)
                selectedItems.firstOrNull()?.let { firstCard ->
                    val firstText = firstCard.vocalization.ifEmpty { firstCard.label }
                    tts.speak(firstText)
                }
                selectedItems.drop(1).forEach { card ->
                    val textToSpeak = card.vocalization.ifEmpty { card.label }
                    tts.speakQueued(textToSpeak)
                }
            },
        verticalAlignment = Alignment.CenterVertically
    )  {
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
                    text = { Text("I-edit ang Kard/Folder") },
                    onClick = {
                        Log.d("MainScreen", "Edit card clicked")
                        onToggleEditMode(true)
                        showDropdownMenu.value = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                )
                DropdownMenuItem(
                    text = { Text("Tangalin ang Kard/Folder") },
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

        IconButton(onClick = { navController.navigate("settings") }) {
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
    navController: NavController,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = { navController.navigate("recording") },
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(Icons.Default.Mic, contentDescription = "Voice input")
    }
}

@Composable
fun EditableItem(
    isEditMode: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(isEditMode) {
        if (isEditMode) {
            while (true) {
                scale.animateTo(0.9f,
                    animationSpec = tween(500, easing = FastOutSlowInEasing))
                scale.animateTo(1f,
                    animationSpec = tween(500, easing = FastOutSlowInEasing))
                delay(1000)
            }
        } else {
            scale.snapTo(1f)
        }
    }

    Box(
        modifier = Modifier
            .scale(scale.value)
            .clickable(enabled = isEditMode) { onClick() }
    ) {
        content()
        if (isEditMode) {
            Box(
                modifier = Modifier
                    .matchParentSize()
            )
        }
    }
}

@Composable
fun CardListItem(
    card: Card,
    isEditMode: Boolean,
    isDeleteMode: Boolean,
    isSelected: Boolean,
    isDragging: Boolean,
    onClick: () -> Unit,
    onToggleDelete: () -> Unit,
    onDragStart: () -> Unit = {}
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isSelected) {
        if (isSelected && isDeleteMode) {
            while (true) {
                rotation.animateTo(
                    targetValue = 2f,
                    animationSpec = tween(100, easing = LinearEasing)
                )
                rotation.animateTo(
                    targetValue = -2f,
                    animationSpec = tween(100, easing = LinearEasing)
                )
            }
        } else {
            rotation.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .scale(if (isDragging) 1.1f else 1f)
            .graphicsLayer(
                rotationZ = rotation.value
            )
    ) {
        EditableItem(
            isEditMode = isEditMode,
            onClick = onClick
        ) {
            CardItem(
                card = card,
                onClick = if (isDeleteMode) onToggleDelete else onClick,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isDeleteMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleDelete() },
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
private fun FolderListItem(
    folder: Folder,
    isEditMode: Boolean,
    isDeleteMode: Boolean,
    isSelected: Boolean,
    isDragging: Boolean,
    onClick: () -> Unit,
    onToggleDelete: () -> Unit,
    onDragStart: () -> Unit = {}
) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(isSelected) {
        if (isSelected && isDeleteMode) {
            while (true) {
                rotation.animateTo(
                    targetValue = 2f,
                    animationSpec = tween(100, easing = LinearEasing)
                )
                rotation.animateTo(
                    targetValue = -2f,
                    animationSpec = tween(100, easing = LinearEasing)
                )
            }
        } else {
            rotation.snapTo(0f)
        }
    }

    Box(
        modifier = Modifier
            .scale(if (isDragging) 1.1f else 1f)
            .graphicsLayer(
                rotationZ = rotation.value
            )
    ) {
        EditableItem(
            isEditMode = isEditMode,
            onClick = onClick
        ) {
            FolderItem(
                folder = folder,
                onClick = if (isDeleteMode) onToggleDelete else onClick
            )
        }

        if (isDeleteMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleDelete() },
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
fun PredictiveContainer(
    selectedCards: List<Card>,
    onCardClick: (Card) -> Unit,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel
) {
    val predictedCards = mainViewModel.predictedCards.collectAsState().value

    // Add logging
    LaunchedEffect(selectedCards) {
        android.util.Log.d("PredictiveContainer", "Selected cards: ${selectedCards.map { it.id }}")
        mainViewModel.predictNextCards(selectedCards)
    }

    // Log predictions
    LaunchedEffect(predictedCards) {
        android.util.Log.d("PredictiveContainer", "Predicted cards: ${predictedCards.map { it.id }}")
    }

    if (selectedCards.isNotEmpty() && predictedCards.isNotEmpty()) {
        val lazyListState = rememberLazyListState()

        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                .padding(horizontal = 5.dp)
        ) {
            Row(
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                LazyRow(
                    state = lazyListState,
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(predictedCards) { card ->
                        CardItem(
                            card = card,
                            onClick = { onCardClick(card) },
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RetryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Walang koneksyon sa internet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Retry",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Subukang muli")
        }
    }
}


