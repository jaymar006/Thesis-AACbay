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




                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        MainScreen(
                            cards = cards,
                            folders = folders,
                            onCardClick = { text -> /* TTS implementation */ },
                            onFolderClick = { folderId ->
                                navController.navigate("folder/$folderId")
                            },
                            onAddClick = { navController.navigate("addModule") },
                            onMicClick = { /* Handle mic click */ }
                        )
                    }
                    composable("addModule") {
                        val addModuleViewModel: AddModuleViewModel = viewModel()
                        AddModuleScreen(
                            viewModel = addModuleViewModel,
                            onBack = { navController.popBackStack() },
                            onSaveComplete = { navController.popBackStack() }
                        )
                    }

//                    composable("folder/{folderId}") { backStackEntry ->
//                        val folderId = backStackEntry.arguments?.getString("folderId") ?: ""
//                        FolderScreen(
//                            folderId = folderId,
//                            onBack = { navController.popBackStack() }
//                        )
//                    }
                    // Add folder screen composable when ready
                }
            }
        }
    }
}