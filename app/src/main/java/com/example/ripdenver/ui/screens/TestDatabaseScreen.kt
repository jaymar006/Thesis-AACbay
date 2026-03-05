package com.example.ripdenver.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.viewmodels.TestDatabaseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestDatabaseScreen(
    onBack: () -> Unit,
    viewModel: TestDatabaseViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val cards by viewModel.cards.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val databaseStatus by viewModel.databaseStatus.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Database Test") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLoading) MaterialTheme.colorScheme.primaryContainer 
                                   else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Database Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = databaseStatus,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (isLoading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard("Folders", folders.size.toString())
                StatCard("Cards", cards.size.toString())
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Refresh Button
            Button(
                onClick = { viewModel.refreshData() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh Data")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Folders List
            Text(
                text = "Folders (${folders.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(folders) { folder ->
                    FolderItem(folder = folder)
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier.width(120.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun FolderItem(folder: Folder) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .padding(4.dp)
            ) {
                // Color indicator
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                ) {
                    // You could add a colored box here if needed
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "ID: ${folder.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

