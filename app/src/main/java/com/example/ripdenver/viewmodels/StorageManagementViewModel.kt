package com.example.ripdenver.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.models.Ngram
import com.example.ripdenver.utils.AuthenticationManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TableData(
    val name: String,
    val columns: List<String>,
    val rows: List<Map<String, Any>>
)

@HiltViewModel
class StorageManagementViewModel @Inject constructor() : ViewModel() {
    private val database = Firebase.database.reference

    private val _tables = MutableStateFlow<List<TableData>>(emptyList())
    val tables = _tables.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        loadDatabaseContents()
    }

    private fun loadDatabaseContents() {
        viewModelScope.launch {
            try {
                val userId = AuthenticationManager.getCurrentUserId() ?: return@launch
                _isLoading.value = true

                // Create a list to hold all table data
                val tablesList = mutableListOf<TableData>()

                // Load cards
                database.child("users").child(userId).child("cards")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val cards = snapshot.children.mapNotNull { it.getValue(Card::class.java) }
                            val cardTable = TableData(
                                name = "Cards",
                                columns = listOf("ID", "Label", "Folder ID", "Color", "Usage Count", "Last Used"),
                                rows = cards.map { card ->
                                    mapOf(
                                        "ID" to card.id,
                                        "Label" to card.label,
                                        "Folder ID" to card.folderId,
                                        "Color" to card.color,
                                        "Usage Count" to card.usageCount,
                                        "Last Used" to card.lastUsed
                                    )
                                }
                            )
                            tablesList.add(cardTable)
                            updateTables(tablesList)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("StorageManagement", "Error loading cards", error.toException())
                        }
                    })

                // Load folders
                database.child("users").child(userId).child("folders")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val folders = snapshot.children.mapNotNull { it.getValue(Folder::class.java) }
                            val folderTable = TableData(
                                name = "Folders",
                                columns = listOf("ID", "Name", "Color", "Order"),
                                rows = folders.map { folder ->
                                    mapOf(
                                        "ID" to folder.id,
                                        "Name" to folder.name,
                                        "Color" to folder.color,
                                        "Order" to folder.order
                                    )
                                }
                            )
                            tablesList.add(folderTable)
                            updateTables(tablesList)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("StorageManagement", "Error loading folders", error.toException())
                        }
                    })

                // Load ngrams
                database.child("users").child(userId).child("ngrams")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val ngrams = snapshot.children.mapNotNull { it.getValue(Ngram::class.java) }
                            val ngramTable = TableData(
                                name = "N-grams",
                                columns = listOf("User ID", "Sequence", "Frequency", "Last Used"),
                                rows = ngrams.map { ngram ->
                                    mapOf(
                                        "User ID" to ngram.userId,
                                        "Sequence" to ngram.sequence.joinToString(" → "),
                                        "Frequency" to ngram.frequency,
                                        "Last Used" to ngram.lastUsed
                                    )
                                }
                            )
                            tablesList.add(ngramTable)
                            updateTables(tablesList)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("StorageManagement", "Error loading ngrams", error.toException())
                        }
                    })

                // Load settings
                database.child("users").child(userId).child("settings")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val settingsTable = TableData(
                                name = "Settings",
                                columns = listOf("Key", "Value"),
                                rows = snapshot.children.map { setting ->
                                    mapOf(
                                        "Key" to (setting.key ?: ""),
                                        "Value" to (setting.value?.toString() ?: "null")
                                    )
                                }
                            )
                            tablesList.add(settingsTable)
                            updateTables(tablesList)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("StorageManagement", "Error loading settings", error.toException())
                        }
                    })

            } catch (e: Exception) {
                Log.e("StorageManagement", "Error loading database contents", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateTables(tablesList: List<TableData>) {
        _tables.value = tablesList
    }

    fun refreshData() {
        loadDatabaseContents()
    }
} 