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


enum class SortType {
    FOLDER_FIRST,
    CARD_FIRST,
    UNSORTED,
    BY_LABEL_ASC,
    BY_LABEL_DESC,
    BY_COLOR,
    BY_USAGE
}
class MainViewModel : ViewModel() {
    private val database = Firebase.database.reference
    private val _sortedItems = MutableStateFlow<List<Any>>(emptyList())
    // Data States
    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    private val _selectedCards = MutableStateFlow<List<Card>>(emptyList()) // Card lang pare
    private val _isDeleteMode = MutableStateFlow(false)
    private val _itemsToDelete = MutableStateFlow<List<Any>>(emptyList())
    private val _isEditMode = MutableStateFlow(false)


    val cards = _cards.asStateFlow()
    val folders = _folders.asStateFlow()
    val selectedCards = _selectedCards.asStateFlow()
    val isDeleteMode = _isDeleteMode.asStateFlow()
    val itemsToDelete = _itemsToDelete.asStateFlow()
    val sortedItems = _sortedItems.asStateFlow()
    val isEditMode = _isEditMode.asStateFlow()


    private var itemOrderPreference = MutableStateFlow(ItemOrder.UNSORTED)
    private enum class ItemOrder {
        FOLDER_FIRST,
        CARD_FIRST,
        UNSORTED
    }

    init {
        loadCards()
        loadFolders()
    }

    fun toggleEditMode(enabled: Boolean) {
        _isEditMode.value = enabled
        if (!enabled) {
            // Reset any edit-related state if needed
        }
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
        database.child("cards")
            .orderByChild("order")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _cards.value = snapshot.children.mapNotNull { it.getValue(Card::class.java) }
                    updateSortedItems() // Add this
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun loadFolders() {
        database.child("folders")
            .orderByChild("order")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _folders.value = snapshot.children.mapNotNull { it.getValue(Folder::class.java) }
                    updateSortedItems() // Add this
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    // Add this new function
    private fun updateSortedItems() {
        val allItems = mutableListOf<Any>()
        allItems.addAll(_folders.value)
        allItems.addAll(_cards.value.filter { it.folderId.isEmpty() })

        // Apply current sorting if any
        when (itemOrderPreference.value) {
            ItemOrder.FOLDER_FIRST -> allItems.sortBy { it !is Folder }
            ItemOrder.CARD_FIRST -> allItems.sortBy { it !is Card }
            ItemOrder.UNSORTED -> {} // No sorting needed
        }

        _sortedItems.value = allItems
    }




    fun sortItems(sortType: SortType) = viewModelScope.launch {
        val allItems = mutableListOf<Any>()
        allItems.addAll(_folders.value)
        allItems.addAll(_cards.value.filter { it.folderId.isEmpty() })

        // Update item order preference if sorting by card/folder
        when (sortType) {
            SortType.FOLDER_FIRST -> itemOrderPreference.value = ItemOrder.FOLDER_FIRST
            SortType.CARD_FIRST -> itemOrderPreference.value = ItemOrder.CARD_FIRST
            SortType.UNSORTED -> itemOrderPreference.value = ItemOrder.UNSORTED
            else -> {} // Keep current preference
        }

        // First sort by type (if needed)
        val typeOrderedItems = when (itemOrderPreference.value) {
            ItemOrder.FOLDER_FIRST -> allItems.sortedBy { it !is Folder }
            ItemOrder.CARD_FIRST -> allItems.sortedBy { it !is Card }
            ItemOrder.UNSORTED -> allItems
        }

        // Then apply additional sorting criteria
        val sortedItems = when (sortType) {
            SortType.FOLDER_FIRST,
            SortType.CARD_FIRST,
            SortType.UNSORTED -> typeOrderedItems
            SortType.BY_LABEL_ASC -> typeOrderedItems.sortedWith(
                compareBy<Any> {
                    // First sort by card/folder according to preference
                    when (itemOrderPreference.value) {
                        ItemOrder.FOLDER_FIRST -> if (it is Folder) 0 else 1
                        ItemOrder.CARD_FIRST -> if (it is Card) 0 else 1
                        ItemOrder.UNSORTED -> 0
                    }
                }.thenBy {
                    // Then sort by label/name ignoring case
                    when (it) {
                        is Card -> it.label.lowercase()
                        is Folder -> it.name.lowercase()
                        else -> ""
                    }
                }
            )
            SortType.BY_LABEL_DESC -> typeOrderedItems.sortedWith(
                compareBy<Any> {
                    // First sort by card/folder according to preference
                    when (itemOrderPreference.value) {
                        ItemOrder.FOLDER_FIRST -> if (it is Folder) 0 else 1
                        ItemOrder.CARD_FIRST -> if (it is Card) 0 else 1
                        ItemOrder.UNSORTED -> 0
                    }
                }.thenByDescending {
                    // Then sort by label/name ignoring case
                    when (it) {
                        is Card -> it.label.lowercase()
                        is Folder -> it.name.lowercase()
                        else -> ""
                    }
                }
            )
            SortType.BY_COLOR -> typeOrderedItems.sortedWith(
                compareBy<Any> {
                    when (itemOrderPreference.value) {
                        ItemOrder.FOLDER_FIRST -> it !is Folder
                        ItemOrder.CARD_FIRST -> it !is Card
                        ItemOrder.UNSORTED -> false
                    }
                }.thenBy {
                    when (it) {
                        is Card -> it.color
                        is Folder -> it.color
                        else -> ""
                    }
                }
            )
            SortType.BY_USAGE -> typeOrderedItems.sortedWith(
                compareBy<Any> {
                    when (itemOrderPreference.value) {
                        ItemOrder.FOLDER_FIRST -> it !is Folder
                        ItemOrder.CARD_FIRST -> it !is Card
                        ItemOrder.UNSORTED -> false
                    }
                }.thenBy {
                    when (it) {
                        is Card -> -it.usageCount
                        else -> 0
                    }
                }
            )
        }

        // Update Firebase order
        sortedItems.forEachIndexed { index, item ->
            when (item) {
                is Folder -> {
                    database.child("folders")
                        .child(item.id)
                        .child("order")
                        .setValue(index)
                }
                is Card -> {
                    database.child("cards")
                        .child(item.id)
                        .child("order")
                        .setValue(index)
                }
            }
        }

        _sortedItems.value = sortedItems
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

    // Also modify deleteSelectedItems to update UI immediately
    fun deleteSelectedItems() = viewModelScope.launch {
        try {
            _itemsToDelete.value.forEach { item ->
                when (item) {
                    is Card -> deleteCard(item)
                    is Folder -> deleteFolder(item)
                }
            }
            // Update sorted items immediately after deletion
            updateSortedItems()
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