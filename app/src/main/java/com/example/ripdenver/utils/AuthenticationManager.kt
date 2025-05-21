package com.example.ripdenver.utils

import com.example.ripdenver.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

    object AuthenticationManager {
    private val auth: FirebaseAuth = Firebase.auth
    private val database = Firebase.database.reference

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun signInAnonymously(): String {
        val result = auth.signInAnonymously().await()
        val userId = result.user?.uid ?: throw Exception("Failed to get user ID")
        
        // Check if this is a new user
        val userSnapshot = database.child("users").child(userId).get().await()
        if (!userSnapshot.exists()) {
            // Create new user with default content
            createNewUser(userId)
        }
        
        return userId
    }

    private suspend fun createNewUser(userId: String) {
        // Create user record
        val user = User(
            userId = userId,
            name = "User $userId",
            createdAt = System.currentTimeMillis(),
            lastLogin = System.currentTimeMillis()
        )
        
        // Save user data
        database.child("users").child(userId).setValue(user).await()
        
        // Initialize default content for this user
        initializeDefaultContent(userId)
    }

    private suspend fun initializeDefaultContent(userId: String) {
        // Add default folders
        DefaultContent.defaultFolders.forEach { folderMap ->
            val folderId = folderMap["id"] as String
            val folder = com.example.ripdenver.models.Folder(
                id = folderId,
                name = folderMap["name"] as String,
                color = folderMap["color"] as String,
                createdAt = System.currentTimeMillis()
            )
            database.child("users").child(userId).child("folders").child(folderId).setValue(folder).await()
        }

        // Add default cards
        DefaultContent.defaultCards.forEach { cardMap ->
            val cardId = java.util.UUID.randomUUID().toString()
            val card = com.example.ripdenver.models.Card(
                id = cardId,
                label = cardMap["label"] as String,
                vocalization = cardMap["vocalization"] as String,
                color = cardMap["color"] as String,
                folderId = cardMap["folderId"] as String,
                cloudinaryUrl = cardMap["cloudinaryUrl"] as String,
                cloudinaryPublicId = cardMap["cloudinaryPublicId"] as String,
                usageCount = 0,
                lastUsed = System.currentTimeMillis()
            )
            database.child("users").child(userId).child("cards").child(cardId).setValue(card).await()
        }

        // Initialize settings
        database.child("users").child(userId).child("settings").setValue(mapOf(
            "columnCount" to 6,
            "showPredictions" to true
        )).await()
    }
} 