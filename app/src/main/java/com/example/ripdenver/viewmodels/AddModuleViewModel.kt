package com.example.ripdenver.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.state.AddModuleState
import com.example.ripdenver.utils.CloudinaryManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

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

    suspend fun saveCard(imageUri: Uri?): Boolean {
        return try {
            val card = uiState.value.run {
                Card(
                    id = UUID.randomUUID().toString(), // Generate ID if not already set
                    label = cardLabel,
                    vocalization = cardVocalization,
                    color = cardColor,
                    cloudinaryUrl = if (imageUri != null) {
                        // This will be handled by the caller who has Context
                        "" // Temporary empty string
                    } else {
                        ""
                    }
                )
            }

            Firebase.database.reference.child("cards").child(card.id)
                .setValue(card)
                .await() // Wait for Firebase operation to complete

            true // Return success
        } catch (e: Exception) {
            e.printStackTrace()
            false // Return failure
        }
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

    suspend fun uploadImageAndGetUrl(context: android.content.Context, uri: Uri): String {
        return CloudinaryManager.uploadImage(context, uri)
    }

    fun saveCard(onComplete: () -> Unit) = viewModelScope.launch {
        try {
            val card = uiState.value.run {
                Card(
                    id = UUID.randomUUID().toString(), // Generate ID if not already set
                    label = cardLabel,
                    vocalization = cardVocalization,
                    color = cardColor,
                    cloudinaryUrl = cardImagePath // Use the already updated path
                )
            }

            Firebase.database.reference.child("cards").child(card.id)
                .setValue(card)
                .await()

            onComplete()
        } catch (e: Exception) {
            // Handle error
        }
    }
}