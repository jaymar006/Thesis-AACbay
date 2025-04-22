package com.example.ripdenver.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.ripdenver.viewmodels.EditCardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    navController: NavController,
    viewModel: EditCardViewModel,
    cardId: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery = remember { mutableStateOf("") }
    val showColorPicker = remember { mutableStateOf(false) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val urlAndId = viewModel.uploadImageAndGetUrl(context, it)
                viewModel.updateCardImage(urlAndId)
            }
        }
    }

    // Load card data when screen is first displayed
    LaunchedEffect(cardId) {
        viewModel.loadCardData(cardId)
    }

    // Search for pictograms when query changes
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            viewModel.updateCard {
                                navController.popBackStack()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Card preview
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(150.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        Color(android.graphics.Color.parseColor(uiState.cardColor)),
                        RoundedCornerShape(8.dp)
                    )
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            ) {
                if (uiState.cardImagePath.first.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(uiState.cardImagePath.first),
                        contentDescription = "Card Image",
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Text(
                    text = uiState.cardLabel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Card label input
            OutlinedTextField(
                value = uiState.cardLabel,
                onValueChange = { viewModel.updateCardLabel(it) },
                label = { Text("Label") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Card vocalization input
            OutlinedTextField(
                value = uiState.cardVocalization,
                onValueChange = { viewModel.updateCardVocalization(it) },
                label = { Text("Vocalization Text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Color picker button
            Text(
                text = "Mga Kulay",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 5.dp, bottom = 4.dp)
            )
            ColorSelectionRow(
                selectedColor = uiState.cardColor,
                onColorSelected = { viewModel.updateCardColor(it) }
            )

            // Image selection options
            Text(
                "Select Image",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("From Device")
                }

                OutlinedButton(
                    onClick = { /* Already in symbol search mode */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("From Symbols")
                }
            }

            // Symbol search
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                label = { Text("Search Symbols") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            if (viewModel.isLoadingPictograms) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.pictograms) { pictogram ->
                        PictogramItem(
                            pictogram = pictogram,
                            onClick = {
                                viewModel.handleSymbolSelection(
                                    context = context,
                                    imageUrl = "https://api.arasaac.org/api/pictograms/${pictogram._id}?download=false",
                                    onSuccess = { urlAndId ->
                                        viewModel.updateCardImage(urlAndId)
                                    },
                                    onError = { /* Handle error */ }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

