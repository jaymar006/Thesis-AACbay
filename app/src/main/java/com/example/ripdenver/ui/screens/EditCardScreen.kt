package com.example.ripdenver.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ripdenver.ui.components.TutorialModal
import com.example.ripdenver.viewmodels.EditCardViewModel
import com.example.ripdenver.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    viewModel: EditCardViewModel,
    cardId: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery = remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedSymbolUrl: String? by remember { mutableStateOf(null) }

    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showSymbolSearchDialog by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(false) }

    val onPreviewClick = {
        showImageSourceDialog = true
    }

    val onSymbolSelected: (String) -> Unit = { imageUrl ->
        isLoading = true
        imageUri = Uri.parse(imageUrl)
        selectedSymbolUrl = imageUrl // <-- Store image URL
        isLoading = false
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadAllPictograms()
    }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("AACBAY_PREFS", Context.MODE_PRIVATE)
        val hasSeenEditTutorial = prefs.getBoolean("has_seen_edit_tutorial", false)
        if (!hasSeenEditTutorial) {
            showTutorial = true
            prefs.edit().putBoolean("has_seen_edit_tutorial", true).apply()
        }
    }

    LaunchedEffect(cardId) {
        viewModel.loadCardData(cardId)
    }

    LaunchedEffect(searchQuery.value) {
        if (searchQuery.value.isNotEmpty()) {
            viewModel.searchPictograms(searchQuery.value)
        } else {
            viewModel.loadAllPictograms()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Card") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            selectedSymbolUrl?.let { imageUrl ->
                                viewModel.handleSymbolSelection(
                                    context = context,
                                    imageUrl = imageUrl,
                                    onSuccess = { urlAndId ->
                                        coroutineScope.launch {
                                            viewModel.updateCard(context) {
                                                isLoading = false
                                                mainViewModel.sortItems(mainViewModel.lastSortType.value)
                                                navController.popBackStack()
                                            }
                                        }
                                    },
                                    onError = { error ->
                                        errorMessage = error
                                        mainViewModel.sortItems(mainViewModel.lastSortType.value)
                                        isLoading = false
                                    }
                                )
                            } ?: run {
                                viewModel.updateCard(context) {
                                    isLoading = false
                                    mainViewModel.sortItems(mainViewModel.lastSortType.value)
                                    navController.popBackStack()
                                }
                            }
                        }
                    }) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 16.dp)
                    .clickable { onPreviewClick() },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .size(180.dp)
                        .aspectRatio(1f)
                        .border(
                            width = 1.dp,
                            color = Color(android.graphics.Color.parseColor(uiState.cardColor)),
                            shape = MaterialTheme.shapes.medium
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(android.graphics.Color.parseColor(uiState.cardColor)).copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(android.graphics.Color.parseColor(uiState.cardColor)).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (selectedSymbolUrl != null) {
                                AsyncImage(
                                    model = selectedSymbolUrl,
                                    contentDescription = "Card image",
                                    modifier = Modifier.size(100.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else if (uiState.cardImagePath.first.isNotEmpty()) {
                                AsyncImage(
                                    model = uiState.cardImagePath.first,
                                    contentDescription = "Card image",
                                    modifier = Modifier.size(100.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "No image",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = uiState.cardLabel.ifEmpty { "Pangalan" },
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Image",
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(24.dp)
                                .clickable { onPreviewClick() },
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(2f)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = uiState.cardLabel,
                    onValueChange = { viewModel.updateCardLabel(it) },
                    label = { Text("Pangalan") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = uiState.cardVocalization,
                    onValueChange = { viewModel.updateCardVocalization(it) },
                    label = { Text("Pagbigkas") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Text(
                    text = "Mga Kulay",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 5.dp, bottom = 4.dp)
                )
                ColorSelectionRow(
                    selectedColor = uiState.cardColor,
                    onColorSelected = { viewModel.updateCardColor(it) }
                )
            }
        }
    }

    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showImageSourceDialog = false },
            onSymbolSelected = {
                showImageSourceDialog = false
                showSymbolSearchDialog = true
            },
            onGallerySelected = {
                showImageSourceDialog = false
                imagePickerLauncher.launch("image/*")
            }
        )
    }

    if (showSymbolSearchDialog) {
        SymbolSearchDialog(
            viewModel = viewModel,
            onDismiss = { showSymbolSearchDialog = false },
            onSymbolSelected = { imageUrl ->
                onSymbolSelected(imageUrl)
                showSymbolSearchDialog = false
            }
        )
    }

    if (showTutorial) {
        TutorialModal(
            title = "Paano I-edit ang Kard",
            content = "Para i-edit ang isang kard:\n\n" +
                     "1. Baguhin ang salita o parirala sa 'Pangalan' field at 'Pagbigkas' kung paano ito bibigkasin\n" +
                     "2. Pumili ng bagong larawan sa pamamagitan ng pag-click sa kahon na may larawan\n" +
                     "3. Pumili ng bagong kulay para sa kard\n" +
                     "4. I-click ang 'Save' button para i-save ang mga pagbabago",
            onDismiss = { showTutorial = false }
        )
    }
}

@Composable
fun SymbolSearchDialog(
    viewModel: EditCardViewModel,
    onDismiss: () -> Unit,
    onSymbolSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    CustomDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Pumili ng Simbolo",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    viewModel.searchPictograms(query)
                },
                label = { Text("Search Symbols(english)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (viewModel.isLoadingPictograms) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .height(500.dp)
                        .fillMaxWidth()
                ) {
                    items(viewModel.pictograms.size) { index ->
                        val pictogram = viewModel.pictograms[index]
                        PictogramItem(
                            pictogram = pictogram,
                            onClick = {
                                onSymbolSelected(pictogram.getImageUrl(500))
                                onDismiss()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Cancel")
            }
        }
    }
}


