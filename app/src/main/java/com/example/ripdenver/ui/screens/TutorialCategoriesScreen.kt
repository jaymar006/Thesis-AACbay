package com.example.ripdenver.ui.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ripdenver.ui.components.TutorialCategory
import com.example.ripdenver.ui.components.TutorialCategoryCard
import com.example.ripdenver.ui.components.TutorialProgressIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialCategoriesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTutorial: (category: String) -> Unit
) {
    // Mock data for tutorial categories
    val tutorialCategories = listOf(
        TutorialCategory(
            title = "Getting Started",
            description = "Learn the basics of using AACBAY",
            icon = Icons.Default.PlayArrow,
            color = Color(0xFF4CAF50),
            tutorialCount = 3,
            isCompleted = true
        ),
        TutorialCategory(
            title = "Communication Board",
            description = "Master the main communication interface",
            icon = Icons.Default.Dashboard,
            color = Color(0xFF2196F3),
            tutorialCount = 4,
            isCompleted = false
        ),
        TutorialCategory(
            title = "Managing Content",
            description = "Add, edit, and organize your cards and folders",
            icon = Icons.Default.Edit,
            color = Color(0xFFFF9800),
            tutorialCount = 3,
            isCompleted = false
        ),
        TutorialCategory(
            title = "Advanced Features",
            description = "Speech-to-text, settings, and customization",
            icon = Icons.Default.Settings,
            color = Color(0xFF607D8B),
            tutorialCount = 2,
            isCompleted = false
        )
    )

    val totalTutorials = tutorialCategories.sumOf { it.tutorialCount }
    val completedTutorials = tutorialCategories.count { it.isCompleted } * 3 // Mock completed count

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Tutorial Categories",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Show help */ }) {
                        Icon(Icons.Default.Help, "Help")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Learn AACBAY",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Choose a category to start learning how to use AACBAY effectively",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Progress indicator
            TutorialProgressIndicator(
                completedSteps = completedTutorials,
                totalSteps = totalTutorials
            )

            // Categories
            Text(
                text = "Tutorial Categories",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            tutorialCategories.forEach { category ->
                TutorialCategoryCard(
                    category = category,
                    onClick = { onNavigateToTutorial(category.title) }
                )
            }

            // Quick actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Reset progress */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Progress")
                        }
                        
                        Button(
                            onClick = { /* Start all tutorials */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start All")
                        }
                    }
                }
            }
        }
    }
}
