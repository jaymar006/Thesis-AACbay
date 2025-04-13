package com.example.ripdenver.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {
    private val database = Firebase.database.reference

    // Data States
    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    private val _selectedCards = MutableStateFlow<List<Card>>(emptyList()) // Card lang pare

    val cards = _cards.asStateFlow()
    val folders = _folders.asStateFlow()
    val selectedCards = _selectedCards.asStateFlow()

    init {
        loadCards()
        loadFolders()
    }

    // Selection Management
    fun addToSelection(card: Card) {
        _selectedCards.update { current ->
            if (!current.contains(card)) current + card else current
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
}