package com.example.ripdenver.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.viewmodels.AddModuleViewModel
// Add these imports at the top
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.lifecycle.viewmodel.compose.viewModel
// OR
import androidx.lifecycle.compose.collectAsStateWithLifecycle  // For StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddModuleScreen(
    viewModel: AddModuleViewModel = viewModel(),
    onBack: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val screenTitle = if (uiState.isCardSelected) "Add new Card" else "Add new Folder"
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onBack) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (uiState.isCardSelected) {
                                viewModel.saveCard()
                            } else {
                                viewModel.saveFolder()
                            }
                            onSaveComplete()
                        },
                        enabled = when {
                            uiState.isCardSelected -> uiState.cardLabel.isNotBlank()
                            else -> uiState.folderLabel.isNotBlank()
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
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
                    Text("Card", modifier = Modifier.padding(start = 8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !uiState.isCardSelected,
                        onClick = { viewModel.selectCardType(false) }
                    )
                    Text("Folder", modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isCardSelected) {
                // Card Fields
                OutlinedTextField(
                    value = uiState.cardLabel,
                    onValueChange = { viewModel.updateCardLabel(it) },
                    label = { Text("Card Label") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.cardVocalization,
                    onValueChange = { viewModel.updateCardVocalization(it) },
                    label = { Text("Vocalization Text") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Color Picker
                ColorSelectionDropdown(
                    selectedColor = uiState.cardColor,
                    onColorSelected = { viewModel.updateCardColor(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Image Picker
                ImageSelectionSection(
                    selectedImage = uiState.cardImagePath,
                    onImageSelected = { viewModel.updateCardImage(it) }
                )
            } else {
                // Folder Fields
                OutlinedTextField(
                    value = uiState.folderLabel,
                    onValueChange = { viewModel.updateFolderLabel(it) },
                    label = { Text("Folder Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Color Picker
                ColorSelectionDropdown(
                    selectedColor = uiState.folderColor,
                    onColorSelected = { viewModel.updateFolderColor(it) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Image Picker
                ImageSelectionSection(
                    selectedImage = uiState.folderImagePath,
                    onImageSelected = { viewModel.updateFolderImage(it) }
                )
            }
        }
    }
}

@Composable
private fun ColorSelectionDropdown(
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    val colors = listOf(
        "#FF0000" to "Red",
        "#00FF00" to "Green",
        "#0000FF" to "Blue",
        "#FFFF00" to "Yellow",
        "#FF00FF" to "Magenta"
    )

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(android.graphics.Color.parseColor(selectedColor)))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Color")
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            colors.forEach { (hex, name) ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name)
                        }
                    },
                    onClick = {
                        onColorSelected(hex)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ImageSelectionSection(
    selectedImage: String,
    onImageSelected: (String) -> Unit
) {
    // Simplified implementation - you'll need to implement actual image picking
    Column {
        Text("Select Image", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))

        // Placeholder for image selection
        Button(onClick = {
            // Implement image picker logic
            // For now just setting a placeholder
            onImageSelected("images/placeholder.png")
        }) {
            Icon(Icons.Default.Image, "Select Image")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Choose Image")
        }

        if (selectedImage.isNotEmpty()) {
            // Show selected image preview
            AsyncImage(
                model = selectedImage,
                contentDescription = "Selected image",
                modifier = Modifier
                    .size(100.dp)
                    .padding(top = 8.dp)
            )
        }
    }
}