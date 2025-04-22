package com.example.ripdenver.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
        Row(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Left side - Card preview
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Using Card component from second document
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
                            if (uiState.cardImagePath.first.isNotEmpty()) {
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
                    }
                }
            }

            // Right side - Form fields
            Column(
                modifier = Modifier
                    .weight(2f)
                    .verticalScroll(rememberScrollState())
            ) {
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

                // Color picker
                Text(
                    text = "Mga Kulay",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 5.dp, bottom = 4.dp)
                )
                ColorSelectionRow(
                    selectedColor = uiState.cardColor,
                    onColorSelected = { viewModel.updateCardColor(it) }
                )

                // Symbol search
                Text(
                    "Search Symbols",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    label = { Text("Search Symbols") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

            }
        }
    }
}

