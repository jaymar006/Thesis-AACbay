package com.example.ripdenver

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ripdenver.ui.screens.AddModuleScreen
import com.example.ripdenver.ui.screens.CrashLogsScreen
import com.example.ripdenver.ui.screens.DeveloperScreen
import com.example.ripdenver.ui.screens.EditCardScreen
import com.example.ripdenver.ui.screens.EditFolderScreen
import com.example.ripdenver.ui.screens.FolderScreen
import com.example.ripdenver.ui.screens.HelpScreen
import com.example.ripdenver.ui.screens.MainScreen
import com.example.ripdenver.ui.screens.NgramVisualizationScreen
import com.example.ripdenver.ui.screens.RecordingScreen
import com.example.ripdenver.ui.screens.SettingsScreen
import com.example.ripdenver.ui.screens.StorageManagementScreen
import com.example.ripdenver.ui.theme.RIPDenverTheme
import com.example.ripdenver.utils.CloudinaryManager
import com.example.ripdenver.viewmodels.AddModuleViewModel
import com.example.ripdenver.viewmodels.MainViewModel
import com.example.ripdenver.viewmodels.NgramVisualizationViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.content.Context

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CloudinaryManager.initialize(this)
        
        // Check if this is the first launch
        val sharedPrefs = getSharedPreferences("AACBAY_PREFS", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPrefs.getBoolean("is_first_launch", true)
        
        setContent {
            RIPDenverTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = viewModel()
                val cards by mainViewModel.cards.collectAsState()
                val folders by mainViewModel.folders.collectAsState()
                val selectedCards by mainViewModel.selectedCards.collectAsState()

                NavHost(
                    navController = navController,
                    startDestination = if (isFirstLaunch) "help" else "main"
                ) {
                    composable("help") {
                        HelpScreen(
                            onNavigateBack = {
                                // Save that tutorial has been shown
                                sharedPrefs.edit().putBoolean("is_first_launch", false).apply()
                                navController.navigate("main") {
                                    popUpTo("help") { inclusive = true }
                                }
                            },
                            showTutorial = true
                        )
                    }

                    composable("main") {
                        val isDeleteMode by mainViewModel.isDeleteMode.collectAsState()
                        val itemsToDelete by mainViewModel.itemsToDelete.collectAsState()
                        val isEditMode by mainViewModel.isEditMode.collectAsState()
                        MainScreen(
                            cards = cards,
                            folders = folders,
                            selectedCards = selectedCards,
                            onCardClick = { card ->
                                mainViewModel.addCardToSelection(card)
                            },
                            onFolderClick = { folder ->
                                navController.navigate("folder/${folder.id}")
                            },
                            onAddClick = { navController.navigate("addModule") },
                            onMicClick = { /* Handle mic click */ },
                            onClearSelection = { mainViewModel.clearSelection() },
                            onRemoveLastSelection = { mainViewModel.removeLastSelection() },
                            onToggleDeleteMode = { mainViewModel.toggleDeleteMode(it) },
                            isDeleteMode = isDeleteMode,
                            isEditMode = isEditMode,
                            onToggleEditMode = { mainViewModel.toggleEditMode(it) },
                            itemsToDelete = itemsToDelete,
                            onToggleItemForDeletion = { mainViewModel.toggleItemForDeletion(it) },
                            onDeleteSelectedItems = { mainViewModel.deleteSelectedItems() },
                            mainViewModel = mainViewModel,
                            navController = navController
                        )
                    }

                    composable("folder/{folderId}") { backStackEntry ->
                        val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
                        val folder = folders.find { it.id == folderId }
                        val folderCards = cards.filter { it.folderId == folderId }
                        val isDeleteMode by mainViewModel.isDeleteMode.collectAsState()
                        val itemsToDelete by mainViewModel.itemsToDelete.collectAsState()
                        val isEditMode by mainViewModel.isEditMode.collectAsState()

                        if (folder != null) {
                            FolderScreen(
                                folder = folder,
                                cards = folderCards,
                                selectedItems = selectedCards,
                                onCardClick = { card ->
                                    mainViewModel.addToSelection(card)
                                },
                                onBack = { navController.navigate("main") },
                                onClearOne = { mainViewModel.removeLastSelection() },
                                onClearAll = { mainViewModel.clearSelection() },
                                onAddClick = { navController.navigate("addModule?folderId=$folderId") },
                                onToggleDeleteMode = { mainViewModel.toggleDeleteMode(it) },
                                isDeleteMode = isDeleteMode,
                                itemsToDelete = itemsToDelete,
                                onToggleItemForDeletion = { mainViewModel.toggleItemForDeletion(it) },
                                onDeleteSelectedItems = { mainViewModel.deleteSelectedItems() },
                                isEditMode = isEditMode,
                                onToggleEditMode = { mainViewModel.toggleEditMode(it) },
                                navController = navController,
                                mainViewModel = mainViewModel // Add this parameter
                            )
                        }
                    }

                    composable(
                        "addModule?folderId={folderId}",
                        arguments = listOf(navArgument("folderId") {
                            type = NavType.StringType
                            defaultValue = ""
                        })
                    ) { backStackEntry ->
                        val addModuleViewModel: AddModuleViewModel = viewModel()
                        val folderId = backStackEntry.arguments?.getString("folderId") ?: ""

                        AddModuleScreen(
                            viewModel = addModuleViewModel,
                            mainViewModel = mainViewModel,
                            folderId = folderId,  // Pass the folderId to AddModuleScreen
                            onBack = { navController.popBackStack() },
                            onSaveComplete = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = "edit_card/{cardId}",
                        arguments = listOf(navArgument("cardId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
                        EditCardScreen(
                            navController = navController,
                            mainViewModel = mainViewModel,
                            viewModel = hiltViewModel(),
                            cardId = cardId
                        )
                    }

                    composable(
                        route = "edit_folder/{folderId}",
                        arguments = listOf(navArgument("folderId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val folderId = backStackEntry.arguments?.getString("folderId") ?: return@composable
                        EditFolderScreen(
                            navController = navController,
                            viewModel = hiltViewModel(),
                            folderId = folderId
                        )
                    }

                    composable("recording") {
                        RecordingScreen(
                            onDismiss = { navController.popBackStack() }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToDeveloper = { navController.navigate("developer") },
                            onNavigateToHelp = { navController.navigate("help") }
                        )
                    }

                    composable(
                        "help?showTutorial={showTutorial}",
                        arguments = listOf(
                            navArgument("showTutorial") {
                                type = NavType.BoolType
                                defaultValue = false
                            }
                        )
                    ) { backStackEntry ->
                        val showTutorial = backStackEntry.arguments?.getBoolean("showTutorial") ?: false
                        HelpScreen(
                            onNavigateBack = { navController.popBackStack() },
                            showTutorial = showTutorial
                        )
                    }

                    composable("developer") {
                        DeveloperScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToNgramVisualization = { navController.navigate("ngram_visualization") },
                            onNavigateToStorageManagement = { navController.navigate("storage_management") },
                            onNavigateToCrashLogs = { navController.navigate("crash_logs") }
                        )
                    }

                    composable("crash_logs") {
                        CrashLogsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("storage_management") {
                        StorageManagementScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("ngram_visualization") {
                        val viewModel: NgramVisualizationViewModel = hiltViewModel()
                        
                        // Get selected cards from MainViewModel and update NgramVisualizationViewModel
                        LaunchedEffect(Unit) {
                            mainViewModel.selectedCards.collect { selectedCards ->
                                Log.d("MainActivity", "Updating NgramVisualization with ${selectedCards.size} selected cards")
                                viewModel.updateSelectedCards(selectedCards)
                            }
                        }
                        
                        NgramVisualizationScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
