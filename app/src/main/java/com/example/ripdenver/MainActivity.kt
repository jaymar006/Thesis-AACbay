package com.example.ripdenver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ripdenver.ui.screens.AddModuleScreen
import com.example.ripdenver.ui.screens.FolderScreen
import com.example.ripdenver.ui.screens.MainScreen
import com.example.ripdenver.ui.theme.RIPDenverTheme
import com.example.ripdenver.viewmodels.AddModuleViewModel
import com.example.ripdenver.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RIPDenverTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = viewModel()
                val cards by mainViewModel.cards.collectAsState()
                val folders by mainViewModel.folders.collectAsState()
                val selectedCards by mainViewModel.selectedCards.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        MainScreen(
                            cards = cards,
                            folders = folders,
                            selectedCards = selectedCards,
                            onCardClick = { card ->
                                mainViewModel.addCardToSelection(card)
                                // Trigger TTS here if needed
                            },
                            onFolderClick = { folder ->
                                //mainViewModel.addToSelection(folder)
                                navController.navigate("folder/${folder.id}")
                            },
                            onAddClick = { navController.navigate("addModule") },
                            onMicClick = { /* Handle mic click */ },
                            onClearSelection = { mainViewModel.clearSelection() },
                            onRemoveLastSelection = { mainViewModel.removeLastSelection() },
                            navController = navController
                        )
                    }

                    composable("folder/{folderId}") { backStackEntry ->
                        val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
                        val folder = folders.find { it.id == folderId }
                        val folderCards = cards.filter { it.folderId == folderId }

                        if (folder != null) {
                            FolderScreen(
                                folder = folder,
                                cards = folderCards,
                                selectedItems = selectedCards,
                                onCardClick = { card ->
                                    mainViewModel.addToSelection(card)
                                },
                                onBack = { navController.popBackStack() },
                                onClearOne = { mainViewModel.removeLastSelection() },
                                onClearAll = { mainViewModel.clearSelection() }
                            )
                        }
                    }

                    composable("addModule") {
                        val addModuleViewModel: AddModuleViewModel = viewModel()
                        AddModuleScreen(
                            viewModel = addModuleViewModel,
                            onBack = { navController.popBackStack() },
                            onSaveComplete = {
                                navController.popBackStack()
                                // Refresh data if needed
                            }
                        )
                    }
                }
            }
        }
    }
}
