package com.example.ripdenver.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.repository.LocalDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestDatabaseViewModel @Inject constructor(
    private val localDataRepository: LocalDataRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()
    
    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    val folders: StateFlow<List<Folder>> = _folders.asStateFlow()
    
    private val _databaseStatus = MutableStateFlow("Initializing...")
    val databaseStatus: StateFlow<String> = _databaseStatus.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            try {
                _databaseStatus.value = "Loading data from local database..."
                
                // Load cards
                localDataRepository.getAllCards().collect { cardList ->
                    _cards.value = cardList
                    _databaseStatus.value = "Loaded ${cardList.size} cards from local database"
                }
                
                // Load folders
                localDataRepository.getAllFolders().collect { folderList ->
                    _folders.value = folderList
                    _databaseStatus.value = "Loaded ${folderList.size} folders and ${_cards.value.size} cards from local database"
                }
                
                _isLoading.value = false
            } catch (e: Exception) {
                _databaseStatus.value = "Error loading data: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun refreshData() {
        _isLoading.value = true
        loadData()
    }
}

