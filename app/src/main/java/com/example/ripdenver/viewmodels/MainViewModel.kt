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

class MainViewModel : ViewModel() {
    private val database = Firebase.database.reference

    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    private val _folders = MutableStateFlow<List<Folder>>(emptyList())

    val cards = _cards.asStateFlow()
    val folders = _folders.asStateFlow()

    init {
        loadCards()
        loadFolders()
    }


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