package com.example.ripdenver.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.utils.CloudinaryManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainViewModel : ViewModel() {
    private val database = Firebase.database.reference

    // Data States
    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    private val _selectedCards = MutableStateFlow<List<Card>>(emptyList()) // Card lang pare
    private val _isDeleteMode = MutableStateFlow(false)
    private val _itemsToDelete = MutableStateFlow<List<Any>>(emptyList())


    val cards = _cards.asStateFlow()
    val folders = _folders.asStateFlow()
    val selectedCards = _selectedCards.asStateFlow()
    val isDeleteMode = _isDeleteMode.asStateFlow()
    val itemsToDelete = _itemsToDelete.asStateFlow()


    init {
        loadCards()
        loadFolders()
    }



    // Selection Management
    fun addToSelection(card: Card) {
        _selectedCards.update { current ->
            current + card // So that we can select multiple of the same card
            // we an use (if (card in current) current - card else current + card) for single selection
        }
    }

    fun addCardToSelection(card: Card) {
        _selectedCards.update { current ->
            current + card
        }
    }

    fun removeLastSelection() {
        _selectedCards.update { current ->
            if (current.isNotEmpty()) current.dropLast(1) else current
        }
    }

    fun clearSelection() {
        _selectedCards.update { emptyList() }
    }

    // Folder Operations
    fun getCardsInFolder(folderId: String): List<Card> {
        return _cards.value.filter { it.folderId == folderId }
    }

    fun getFolderById(folderId: String): Folder? {
        return _folders.value.find { it.id == folderId }
    }

    // Data Loading
    private fun loadCards() {
        database.child("cards").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _cards.value = snapshot.children.mapNotNull { it.getValue(Card::class.java) }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun loadFolders() {
        database.child("folders").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _folders.value = snapshot.children.mapNotNull { it.getValue(Folder::class.java) }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    //Card Operation (Delete)
    fun toggleDeleteMode(enabled: Boolean) {
        _isDeleteMode.value = enabled
        if (!enabled) {
            _itemsToDelete.value = emptyList()
        }
    }

    fun toggleItemForDeletion(item: Any) {
        _itemsToDelete.update { current ->
            if (item in current) current - item else current + item
        }
    }

    fun deleteSelectedItems() = viewModelScope.launch {
        try {
            _itemsToDelete.value.forEach { item ->
                when (item) {
                    is Card -> deleteCard(item)
                    is Folder -> deleteFolder(item)
                }
            }
        } finally {
            toggleDeleteMode(false)
        }
    }

    private suspend fun deleteCard(card: Card) {
        try {
            android.util.Log.d("CardDeletion", "Starting deletion for card ID: ${card.id}")
            android.util.Log.d("CardDeletion", "Using Cloudinary Public ID: ${card.cloudinaryPublicId}")

            // Delete from Cloudinary first using the cloudinaryPublicId
            if (card.cloudinaryPublicId.isNotEmpty()) {
                android.util.Log.d("CardDeletion", "Attempting Cloudinary deletion")
                // Here's where we pass the cloudinaryPublicId, not the card.id
                val deleteResult = CloudinaryManager.deleteImage(card.cloudinaryPublicId)
                android.util.Log.d("CardDeletion", "Cloudinary deletion result: $deleteResult")
            }

            // Then delete from Firebase using card.id
            database.child("cards").child(card.id).removeValue().await()
            android.util.Log.d("CardDeletion", "Firebase deletion successful")
        } catch (e: Exception) {
            android.util.Log.e("CardDeletion", "Error deleting card", e)
            throw e
        }
    }


    private suspend fun deleteFolder(folder: Folder) {
        try {
            // Get all cards in folder
            val snapshot = database.child("cards")
                .orderByChild("folderId")
                .equalTo(folder.id)
                .get()
                .await()

            // Delete all cards in folder
            snapshot.children.forEach { cardSnapshot ->
                val card = cardSnapshot.getValue(Card::class.java)
                card?.let { deleteCard(it) }
            }

            // Delete folder after all cards are processed
            database.child("folders").child(folder.id).removeValue().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}