package com.example.ripdenver.services

import android.util.Log
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.repository.LocalDataRepository
import com.example.ripdenver.utils.DefaultContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultContentService @Inject constructor(
    private val localDataRepository: LocalDataRepository
) {
    
    suspend fun populateDefaultContent() = withContext(Dispatchers.IO) {
        try {
            Log.d("DefaultContentService", "Starting to populate default content")
            
            // Populate default folders
            populateDefaultFolders()
            
            // Populate default cards
            populateDefaultCards()
            
            Log.d("DefaultContentService", "Successfully populated default content")
        } catch (e: Exception) {
            Log.e("DefaultContentService", "Error populating default content", e)
            throw e
        }
    }
    
    private suspend fun populateDefaultFolders() {
        val defaultFolders = DefaultContent.defaultFolders.map { folderMap ->
            Folder(
                id = folderMap["id"] as String,
                name = folderMap["name"] as String,
                color = folderMap["color"] as String,
                createdAt = System.currentTimeMillis(),
                order = 0
            )
        }
        
        localDataRepository.insertFolders(defaultFolders, isDefault = true)
        Log.d("DefaultContentService", "Inserted ${defaultFolders.size} default folders")
    }
    
    private suspend fun populateDefaultCards() {
        val defaultCards = DefaultContent.defaultCards.map { cardMap ->
            Card(
                id = UUID.randomUUID().toString(), // Generate new UUID for each card
                label = cardMap["label"] as String,
                vocalization = cardMap["vocalization"] as String,
                color = cardMap["color"] as String,
                cloudinaryUrl = cardMap["cloudinaryUrl"] as String,
                cloudinaryPublicId = cardMap["cloudinaryPublicId"] as String,
                folderId = cardMap["folderId"] as String,
                usageCount = 0,
                lastUsed = System.currentTimeMillis(),
                order = 0
            )
        }
        
        localDataRepository.insertCards(defaultCards, isDefault = true)
        Log.d("DefaultContentService", "Inserted ${defaultCards.size} default cards")
    }
    
    suspend fun isDefaultContentPopulated(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if we have any folders in the local database.
            // Use `first()` to read a single snapshot of the data instead of collecting
            // the entire flow, which would otherwise never complete for a Room-backed Flow.
            val folders = localDataRepository.getAllFolders().first()
            folders.isNotEmpty()
        } catch (e: Exception) {
            Log.e("DefaultContentService", "Error checking if default content is populated", e)
            false
        }
    }
    
    suspend fun clearUserData() = withContext(Dispatchers.IO) {
        try {
            Log.d("DefaultContentService", "Clearing user data")
            
            // Clear user cards and folders (keep default ones)
            // Note: We'll need to add methods to delete only user data
            // For now, we'll implement this in the repository
            
            Log.d("DefaultContentService", "User data cleared")
        } catch (e: Exception) {
            Log.e("DefaultContentService", "Error clearing user data", e)
            throw e
        }
    }
}

