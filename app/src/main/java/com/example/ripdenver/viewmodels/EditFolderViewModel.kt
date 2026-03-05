package com.example.ripdenver.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.Folder
import com.example.ripdenver.repository.LocalDataRepository
import com.example.ripdenver.state.EditFolderState
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditFolderViewModel @Inject constructor(
    private val localDataRepository: LocalDataRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditFolderState())
    val uiState: StateFlow<EditFolderState> = _uiState.asStateFlow()

    fun loadFolderData(folderId: String) {
        viewModelScope.launch {
            try {
                // Prefer loading from local database first for offline support
                val localFolder = localDataRepository.getFolderById(folderId)
                if (localFolder != null) {
                    _uiState.value = EditFolderState(
                        folderId = localFolder.id,
                        folderLabel = localFolder.name,
                        folderColor = localFolder.color
                    )
                    return@launch
                }

                // Fallback to Firebase if needed (e.g., before first sync)
                val userId = com.example.ripdenver.utils.AuthenticationManager.getCurrentUserId() ?: return@launch
                val folderSnapshot = Firebase.database.reference
                    .child("users")
                    .child(userId)
                    .child("folders")
                    .child(folderId)
                    .get()
                    .await()

                val remoteFolder = folderSnapshot.getValue(Folder::class.java)
                remoteFolder?.let {
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
                        createdAt = System.currentTimeMillis()
                    )
                }

                // Write to local database first and mark for sync
                localDataRepository.upsertUserFolder(folder)

                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}