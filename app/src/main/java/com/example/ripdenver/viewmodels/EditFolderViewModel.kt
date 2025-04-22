package com.example.ripdenver.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.Folder
import com.example.ripdenver.state.EditFolderState
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EditFolderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditFolderState())
    val uiState: StateFlow<EditFolderState> = _uiState.asStateFlow()

    fun loadFolderData(folderId: String) {
        viewModelScope.launch {
            try {
                val folderSnapshot = Firebase.database.reference
                    .child("folders")
                    .child(folderId)
                    .get()
                    .await()

                val folder = folderSnapshot.getValue(Folder::class.java)
                folder?.let {
                    _uiState.value = EditFolderState(
                        folderId = it.id,
                        folderLabel = it.name,
                        folderColor = it.color
                    )
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    fun updateFolderLabel(label: String) {
        _uiState.value = _uiState.value.copy(folderLabel = label)
    }

    fun updateFolderColor(color: String) {
        _uiState.value = _uiState.value.copy(folderColor = color)
    }

    fun updateFolder(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val folder = uiState.value.run {
                    Folder(
                        id = folderId,
                        name = folderLabel,
                        color = folderColor,
                        createdAt = System.currentTimeMillis() // This should be retrieved from the original folder
                    )
                }

                Firebase.database.reference
                    .child("folders")
                    .child(folder.id)
                    .setValue(folder)
                    .await()

                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}