package com.example.ripdenver.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ripdenver.models.ArasaacPictogram
import com.example.ripdenver.viewmodels.AddModuleViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddModuleScreen(
    viewModel: AddModuleViewModel = viewModel(),
    folderId: String = "",
    onBack: () -> Unit,
    onSaveComplete: () -> Unit
) {

    LaunchedEffect(folderId) {
        viewModel.setFolderId(folderId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State for image handling
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedSymbolUrl: String? by remember { mutableStateOf(null) }



    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showSymbolSearchDialog by remember { mutableStateOf(false) }

    // handle preview click
    val onPreviewClick = {
        if (uiState.isCardSelected) {
            showImageSourceDialog = true

        }
    }


    LaunchedEffect(Unit) {
        viewModel.loadAllPictograms()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isCardSelected) "Magdagdag ng Kard" else "Magdagdag ng Folder") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // LEFT SIDE: PREVIEW AND SAVE BUTTON ==============================
            Column(
                modifier = Modifier
                    .padding(end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // PREVIEW (CARD OR FOLDER)
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.0f))
                        .padding(16.dp)
                        .clickable { onPreviewClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isCardSelected) {
                        // CARD PREVIEW
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
                                    if (imageUri != null) {
                                        AsyncImage(
                                            model = imageUri,
                                            contentDescription = "Preview image",
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
                    } else {
                        // FOLDER PREVIEW
                        Card(
                            modifier = Modifier
                                .size(180.dp)
                                .aspectRatio(1f)
                                .border(
                                    width = 1.dp,
                                    color = Color(android.graphics.Color.parseColor(uiState.folderColor)),
                                    shape = MaterialTheme.shapes.medium
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(android.graphics.Color.parseColor(uiState.folderColor)).copy(alpha = 0.5f)
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(android.graphics.Color.parseColor(uiState.folderColor)).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = uiState.folderLabel.ifEmpty { "Pangalan ng Folder" },
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }

                // SAVE BUTTON under the preview
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            if (uiState.isCardSelected) {
                                selectedSymbolUrl?.let { imageUrl ->
                                    viewModel.handleSymbolSelection(
                                        context = context,
                                        imageUrl = imageUrl,
                                        onSuccess = { urlAndPublicId ->
                                            viewModel.saveCard {
                                                isLoading = false
                                                onSaveComplete()
                                            }
                                        },
                                        onError = { error ->
                                            errorMessage = error
                                            isLoading = false
                                        }
                                    )
                                } ?: run {
                                    viewModel.saveCard {
                                        isLoading = false
                                        onSaveComplete()
                                    }
                                }
                            } else {
                                viewModel.saveFolder()
                                isLoading = false
                                onSaveComplete()
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(top = 16.dp),
                    enabled = !isLoading && when {
                        uiState.isCardSelected -> uiState.cardLabel.isNotBlank()
                        else -> uiState.folderLabel.isNotBlank()
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save")
                    }
                }
            }

            // RIGHT SIDE: FORM FIELDS ==============================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // Radio buttons for Card/Folder selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = uiState.isCardSelected,
                            onClick = { viewModel.selectCardType(true) }
                        )
                        Text("Kard", modifier = Modifier.padding(start = 8.dp, end = 16.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = !uiState.isCardSelected,
                            onClick = { viewModel.selectCardType(false) }
                        )
                        Text("Folder", modifier = Modifier.padding(start = 8.dp))
                    }
                }

                if (uiState.isCardSelected) {
                    // Card Fields
                    OutlinedTextField(
                        value = uiState.cardLabel,
                        onValueChange = { viewModel.updateCardLabel(it) },
                        label = { Text("Pangalan") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )

                    OutlinedTextField(
                        value = uiState.cardVocalization,
                        onValueChange = { viewModel.updateCardVocalization(it) },
                        label = { Text("Pagbigkas") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )

                    // Color Picker
                    Text(
                        text = "Mga Kulay",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 5.dp, bottom = 4.dp)
                    )
                    ColorSelectionRow(
                        selectedColor = uiState.cardColor,
                        onColorSelected = { viewModel.updateCardColor(it) }
                    )
                } else {
                    // Folder Fields
                    OutlinedTextField(
                        value = uiState.folderLabel,
                        onValueChange = { viewModel.updateFolderLabel(it) },
                        label = { Text("Pangalan ng Folder") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    )

                    Text(
                        text = "Mga Kulay",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                    ColorSelectionRow(
                        selectedColor = uiState.folderColor,
                        onColorSelected = { viewModel.updateFolderColor(it) }
                    )
                }
            }
        }

        // Dialogs
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
    }
}

@Composable
private fun ColorSelectionRow(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#FF0000" to "Red",
        "#00FF00" to "Green",
        "#0000FF" to "Blue",
        "#FFFF00" to "Yellow",
        "#FFFFFF" to "White",
        "#00FFFF" to "Cyan",
        "#FFA500" to "Orange",
        "#800080" to "Purple",
        "#808080" to "Gray"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        colors.forEach { (hex, _) ->
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .background(
                        color = Color(android.graphics.Color.parseColor(hex)),
                        shape = CircleShape
                    )
                    .border(
                        width = if (selectedColor == hex) 2.dp else 1.dp,
                        color = if (selectedColor == hex) MaterialTheme.colorScheme.primary else Color.Gray,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(hex) }
            )
        }
    }
}


@Composable
fun PictogramItem(
    pictogram: ArasaacPictogram,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .padding(4.dp)
    ) {
        val imageUrl = "https://static.arasaac.org/pictograms/${pictogram._id}/${pictogram._id}_300.png"
        AsyncImage(
            model = imageUrl,
            contentDescription = pictogram.keywords.firstOrNull()?.keyword ?: "Symbol",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun CustomDialog(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f) // Adjust width as needed (90% of screen width)
                .wrapContentHeight()
        ) {
            content()
        }
    }
}

@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onSymbolSelected: () -> Unit,
    onGallerySelected: () -> Unit
) {
    AlertDialog(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onDismissRequest = onDismiss,
        title = { Text("") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .width(600.dp)
            ) {
                Button(
                    onClick = onSymbolSelected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pumili ng Simbolo")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onGallerySelected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pumili mula sa Gallery")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@Composable
fun SymbolSearchDialog(
    viewModel: AddModuleViewModel,
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
