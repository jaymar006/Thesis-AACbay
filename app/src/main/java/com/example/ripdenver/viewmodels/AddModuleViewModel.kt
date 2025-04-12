package com.example.ripdenver.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.state.AddModuleState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddModuleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AddModuleState())
    val uiState: StateFlow<AddModuleState> = _uiState.asStateFlow()

    fun selectCardType(isCard: Boolean) {
        _uiState.value = _uiState.value.copy(isCardSelected = isCard)
    }

    fun updateCardLabel(label: String) {
        _uiState.value = _uiState.value.copy(cardLabel = label)
    }

    fun updateCardVocalization(text: String) {
        _uiState.value = _uiState.value.copy(cardVocalization = text)
    }

    fun updateCardColor(color: String) {
        _uiState.value = _uiState.value.copy(cardColor = color)
    }

    fun updateCardImage(path: String) {
        _uiState.value = _uiState.value.copy(cardImagePath = path)
    }

    fun updateFolderLabel(label: String) {
        _uiState.value = _uiState.value.copy(folderLabel = label)
    }

    fun updateFolderColor(color: String) {
        _uiState.value = _uiState.value.copy(folderColor = color)
    }

    fun updateFolderImage(path: String) {
        _uiState.value = _uiState.value.copy(folderImagePath = path)
    }

    fun saveCard() = viewModelScope.launch {
        // Implement actual save logic
        val newCard = uiState.value.run {
            Card(
                label = cardLabel,
                vocalization = cardVocalization,
                color = cardColor,
                imagePath = cardImagePath
            )
        }
        // TODO: Save to Firebase
    }

    fun saveFolder() = viewModelScope.launch {
        // Implement actual save logic
        val newFolder = uiState.value.run {
            Folder(
                name = folderLabel,
                color = folderColor
            )
        }
        // TODO: Save to Firebase
    }
}