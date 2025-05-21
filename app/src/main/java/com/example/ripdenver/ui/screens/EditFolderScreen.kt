package com.example.ripdenver.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ripdenver.ui.theme.availableColors
import com.example.ripdenver.viewmodels.EditFolderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFolderScreen(
    navController: NavController,
    viewModel: EditFolderViewModel,
    folderId: String
) {
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val showColorPicker = remember { mutableStateOf(false) }

    // Load folder data when screen is first displayed
    LaunchedEffect(folderId) {
        viewModel.loadFolderData(folderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Folder") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.updateFolder {
                            navController.popBackStack()
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
            // Folder preview
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(150.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        Color(android.graphics.Color.parseColor(uiState.folderColor)),
                        RoundedCornerShape(8.dp)
                    )
                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = "Folder",
                    modifier = Modifier
                        .padding(16.dp)
                        .size(60.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Text(
                    text = uiState.folderLabel,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Folder name input
            OutlinedTextField(
                value = uiState.folderLabel,
                onValueChange = { viewModel.updateFolderLabel(it) },
                label = { Text("Folder Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Color picker (replace dropdown with ColorSelectionRow)
            Text(
                text = "Mga Kulay",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 0.dp, bottom = 4.dp)
            )
            ColorSelectionRow(
                selectedColor = uiState.folderColor,
                onColorSelected = { viewModel.updateFolderColor(it) }
            )
        }
    }
}