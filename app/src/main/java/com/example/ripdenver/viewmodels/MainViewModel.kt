package com.example.ripdenver.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.models.Ngram
import com.example.ripdenver.repository.LocalDataRepository
import com.example.ripdenver.services.DataSyncService
import com.example.ripdenver.services.DefaultContentService
import com.example.ripdenver.utils.AuthenticationManager
import com.example.ripdenver.utils.CloudinaryManager
import com.example.ripdenver.utils.DefaultContent
import com.example.ripdenver.utils.ConnectivityObserver
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

enum class SortType {
    FOLDER_FIRST,
    CARD_FIRST,
    UNSORTED,
    BY_LABEL_ASC,
    BY_LABEL_DESC,
    BY_COLOR,
    BY_USAGE
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val localDataRepository: LocalDataRepository,
    private val dataSyncService: DataSyncService,
    private val defaultContentService: DefaultContentService,
    connectivityObserver: ConnectivityObserver
) : ViewModel() {
    private val database = Firebase.database.reference

    // Data States
    private val _predictedCards = MutableStateFlow<List<Pair<Card, Float>>>(emptyList())
    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    private val _folders = MutableStateFlow<List<Folder>>(emptyList())
    private val _selectedCards = MutableStateFlow<List<Card>>(emptyList())
    private val _isDeleteMode = MutableStateFlow(false)
    private val _itemsToDelete = MutableStateFlow<List<Any>>(emptyList())
    private val _isEditMode = MutableStateFlow(false)
    private val _sortedItems = MutableStateFlow<List<Any>>(emptyList())
    private val _lastSortType = MutableStateFlow(SortType.UNSORTED)
    private val _isOffline = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(true)

    val cards = _cards.asStateFlow()
    val folders = _folders.asStateFlow()
    val selectedCards = _selectedCards.asStateFlow()
    val isDeleteMode = _isDeleteMode.asStateFlow()
    val itemsToDelete = _itemsToDelete.asStateFlow()
    val sortedItems = _sortedItems.asStateFlow()
    val isEditMode = _isEditMode.asStateFlow()
    val lastSortType = _lastSortType.asStateFlow()
    val predictedCards = _predictedCards.asStateFlow()
    val isOffline = _isOffline.asStateFlow()
    val isLoading = _isLoading.asStateFlow()

    private val _columnCount = MutableStateFlow(6)
    val columnCount = _columnCount.asStateFlow()

    private val _showPredictions = MutableStateFlow(true)
    val showPredictions = _showPredictions.asStateFlow()

    private val _boardImageSize = MutableStateFlow("katamtaman")
    val boardImageSize = _boardImageSize.asStateFlow()

    private val _containerImageSize = MutableStateFlow("katamtaman")
    val containerImageSize = _containerImageSize.asStateFlow()

    private val _boardTextSize = MutableStateFlow("katamtaman")
    val boardTextSize = _boardTextSize.asStateFlow()

    private val _containerTextSize = MutableStateFlow("katamtaman")
    val containerTextSize = _containerTextSize.asStateFlow()

    private var itemOrderPreference = MutableStateFlow(ItemOrder.UNSORTED)
    private var lastConnectivityState: Boolean? = null
    private enum class ItemOrder {
        FOLDER_FIRST,
        CARD_FIRST,
        UNSORTED
    }

    init {
        initializeUser()

        // Observe connectivity changes and trigger sync when coming back online
        viewModelScope.launch {
            connectivityObserver.isConnected.collect { isConnected ->
                val previous = lastConnectivityState
                lastConnectivityState = isConnected

                _isOffline.value = !isConnected

                if (isConnected && previous == false) {
                    onConnectivityRestored()
                }
            }
        }

        // Test database connectivity after a short delay
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000) // 2 second delay
            testDatabaseConnectivity()
        }

        // Add a safety timeout to ensure loading state doesn't get stuck
        viewModelScope.launch {
            kotlinx.coroutines.delay(15000) // 15 second safety timeout
            if (_isLoading.value) {
                Log.w("MainViewModel", "Loading timeout reached, forcing loading to false")
                _isLoading.value = false
            }
        }
    }

    private fun onConnectivityRestored() {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "Connectivity restored, attempting sync")

                val currentUserId = AuthenticationManager.getCurrentUserId()
                val userId = currentUserId ?: AuthenticationManager.signInAnonymously()

                _isOffline.value = false
                _isLoading.value = true

                val syncResult = dataSyncService.syncAllUserData(userId)
                Log.d("MainViewModel", "Auto sync after connectivity restored: $syncResult")

                // Reload data from local database (now synced)
                loadUserData(userId)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to sync after connectivity restored", e)
                _isOffline.value = true
                _isLoading.value = false
            }
        }
    }

    private fun initializeUser() {
        viewModelScope.launch {
            try {
                val userId = AuthenticationManager.signInAnonymously()
                _isOffline.value = false
                
                // First sync all user data from Firebase to local database
                val syncResult = dataSyncService.syncAllUserData(userId)
                Log.d("MainViewModel", "Sync result: $syncResult")
                
                // Then load data (will use local database)
                loadUserData(userId)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to initialize user, falling back to offline mode", e)
                _isOffline.value = true
                // Load offline data without requiring authentication
                loadOfflineDataWithoutAuth()
            }
        }
    }

    private fun loadUserData(userId: String) {
        // Load from local database (which now has synced data from Firebase)
        loadOfflineData()
        
        // Also set up Firebase listeners for real-time updates
        setupFirebaseListeners(userId)
    }
    
    private fun setupFirebaseListeners(userId: String) {
        // Set up Firebase listeners for real-time updates
        loadCards(userId)
        loadFolders(userId)
        observeGridSettings(userId)
    }

    private fun loadOfflineData() {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "Loading offline data from local database")
                
                // Load cards from local database
                val cardList = localDataRepository.getAllCards().first()
                _cards.value = cardList
                Log.d("MainViewModel", "Loaded ${cardList.size} cards from local database")
                
                // Load folders from local database
                val folderList = localDataRepository.getAllFolders().first()
                _folders.value = folderList
                Log.d("MainViewModel", "Loaded ${folderList.size} folders from local database")
                
                // Update sorted items after loading both cards and folders
                updateSortedItems()
                
                Log.d("MainViewModel", "After updateSortedItems - _cards.value.size: ${_cards.value.size}, _folders.value.size: ${_folders.value.size}, _sortedItems.value.size: ${_sortedItems.value.size}")
                
                // Load settings from local database (if available)
                val userId = AuthenticationManager.getCurrentUserId()
                if (userId != null) {
                    try {
                        val settings = localDataRepository.getSettingsByUserFlow(userId).first()
                        settings?.let {
                            _columnCount.value = it.columnCount
                            _showPredictions.value = it.showPredictions
                            _boardImageSize.value = it.boardImageSize
                            _containerImageSize.value = it.containerImageSize
                            _boardTextSize.value = it.boardTextSize
                            _containerTextSize.value = it.containerTextSize
                            Log.d("MainViewModel", "Loaded settings from local database")
                        }
                    } catch (e: Exception) {
                        Log.d("MainViewModel", "No settings found in local database, using defaults")
                    }
                }
                
                _isLoading.value = false
                Log.d("MainViewModel", "Offline data loaded successfully")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading offline data", e)
                _isLoading.value = false
            }
        }
    }

    private fun loadOfflineDataWithoutAuth() {
        viewModelScope.launch {
            try {
                Log.d("MainViewModel", "Loading offline data from local database (no auth)")
                
                // Add a small delay to ensure database initialization is complete
                kotlinx.coroutines.delay(1000)
                
                // Add a timeout to prevent infinite loading
                kotlinx.coroutines.withTimeout(10000) { // 10 second timeout
                    // Load cards and folders from local database
                    var cardList = localDataRepository.getAllCards().first()
                    var folderList = localDataRepository.getAllFolders().first()

                    Log.d("MainViewModel", "Initial offline load (no auth) - cards: ${cardList.size}, folders: ${folderList.size}")

                    // If nothing is in the local database, populate default content once
                    if (cardList.isEmpty() && folderList.isEmpty()) {
                        Log.d("MainViewModel", "No local content found, populating default content")
                        defaultContentService.populateDefaultContent()

                        // Re-read after seeding
                        cardList = localDataRepository.getAllCards().first()
                        folderList = localDataRepository.getAllFolders().first()
                        Log.d("MainViewModel", "After seeding default content - cards: ${cardList.size}, folders: ${folderList.size}")
                    }

                    _cards.value = cardList
                    _folders.value = folderList
                    Log.d("MainViewModel", "Loaded ${cardList.size} cards and ${folderList.size} folders from local database (no auth)")
                    
                    // Update sorted items after loading both cards and folders
                    updateSortedItems()
                    
                    Log.d("MainViewModel", "After updateSortedItems (no auth) - _cards.value.size: ${_cards.value.size}, _folders.value.size: ${_folders.value.size}, _sortedItems.value.size: ${_sortedItems.value.size}")
                    
                    // Use default settings when offline without auth
                    Log.d("MainViewModel", "Using default settings for offline mode")
                }
                
                _isLoading.value = false
                Log.d("MainViewModel", "Offline data loaded successfully (no auth)")
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                Log.e("MainViewModel", "Timeout loading offline data, using empty data", e)
                _cards.value = emptyList()
                _folders.value = emptyList()
                updateSortedItems()
                _isLoading.value = false
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading offline data without auth", e)
                _cards.value = emptyList()
                _folders.value = emptyList()
                updateSortedItems()
                _isLoading.value = false
            }
        }
    }

    private fun loadCards(userId: String) {
        var isFirstLoad = true
        database.child("users").child(userId).child("cards")
            .orderByChild("order")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val cards = snapshot.children.mapNotNull { it.getValue(Card::class.java) }
                    _cards.value = cards
                    
                    // Sync to local database
                    viewModelScope.launch {
                        try {
                            cards.forEach { card ->
                                localDataRepository.insertCard(card, isDefault = false)
                            }
                        } catch (e: Exception) {
                            Log.e("MainViewModel", "Error syncing cards to local database", e)
                        }
                    }
                    
                    updateSortedItems()
                    
                    // Set loading to false after first load
                    if (isFirstLoad) {
                        _isLoading.value = false
                        isFirstLoad = false
                        Log.d("MainViewModel", "Loaded ${cards.size} cards from Firebase")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainViewModel", "Failed to load cards from Firebase, falling back to local", error.toException())
                    // Fallback to local database
                    loadOfflineCards()
                }
            })
    }

    private fun loadOfflineCards() {
        viewModelScope.launch {
            try {
                val cardList = localDataRepository.getAllCards().first()
                _cards.value = cardList
                updateSortedItems()
                Log.d("MainViewModel", "Loaded ${cardList.size} cards from offline database")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading offline cards", e)
            }
        }
    }

    private fun loadFolders(userId: String) {
        var isFirstLoad = true
        database.child("users").child(userId).child("folders")
            .orderByChild("order")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val folders = snapshot.children.mapNotNull { it.getValue(Folder::class.java) }
                    _folders.value = folders
                    
                    // Sync to local database
                    viewModelScope.launch {
                        try {
                            folders.forEach { folder ->
                                localDataRepository.insertFolder(folder, isDefault = false)
                            }
                        } catch (e: Exception) {
                            Log.e("MainViewModel", "Error syncing folders to local database", e)
                        }
                    }
                    
                    updateSortedItems()
                    
                    // Set loading to false after first load
                    if (isFirstLoad) {
                        _isLoading.value = false
                        isFirstLoad = false
                        Log.d("MainViewModel", "Loaded ${folders.size} folders from Firebase")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainViewModel", "Failed to load folders from Firebase, falling back to local", error.toException())
                    // Fallback to local database
                    loadOfflineFolders()
                }
            })
    }

    private fun loadOfflineFolders() {
        viewModelScope.launch {
            try {
                val folderList = localDataRepository.getAllFolders().first()
                _folders.value = folderList
                updateSortedItems()
                Log.d("MainViewModel", "Loaded ${folderList.size} folders from offline database")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error loading offline folders", e)
            }
        }
    }

    private fun observeGridSettings(userId: String) {
        database.child("users").child(userId).child("settings")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        snapshot.child("columnCount").getValue(Int::class.java)?.let {
                            _columnCount.value = it
                        }
                        snapshot.child("showPredictions").getValue(Boolean::class.java)?.let {
                            _showPredictions.value = it
                        }
                        snapshot.child("boardImageSize").getValue(String::class.java)?.let {
                            _boardImageSize.value = it
                            Log.d("MainViewModel", "Board image size updated to: $it")
                        }
                        snapshot.child("containerImageSize").getValue(String::class.java)?.let {
                            _containerImageSize.value = it
                            Log.d("MainViewModel", "Container image size updated to: $it")
                        }
                        snapshot.child("boardTextSize").getValue(String::class.java)?.let {
                            _boardTextSize.value = it
                            Log.d("MainViewModel", "Board text size updated to: $it")
                        }
                        snapshot.child("containerTextSize").getValue(String::class.java)?.let {
                            _containerTextSize.value = it
                            Log.d("MainViewModel", "Container text size updated to: $it")
                        }
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Error updating settings", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("MainViewModel", "Failed to load grid settings", error.toException())
                }
            })
    }

    // Predict the next card
    fun predictNextCards(selectedCards: List<Card>) {
        viewModelScope.launch {
            val userId = AuthenticationManager.getCurrentUserId() ?: "local_offline_user"
            if (selectedCards.isEmpty()) {
                _predictedCards.value = emptyList()
                return@launch
            }

            val lastCardId = selectedCards.last().id

            try {
                // Load ngrams for this user from local database
                val ngrams = localDataRepository.getNgramsByUser(userId).first()

                // Calculate total frequency and card frequencies where the sequence starts with lastCardId
                var totalFrequency = 0
                val cardFrequencies = mutableMapOf<String, Int>()

                ngrams.forEach { ngram ->
                    val firstCardId = ngram.sequence.firstOrNull()
                    val nextCardId = ngram.sequence.getOrNull(1)
                    if (firstCardId == lastCardId && nextCardId != null) {
                        val frequency = ngram.frequency
                        totalFrequency += frequency
                        cardFrequencies[nextCardId] = (cardFrequencies[nextCardId] ?: 0) + frequency
                    }
                }

                // Convert frequencies to probabilities and create predictions
                val predictions = cardFrequencies.mapNotNull { (cardId, frequency) ->
                    val card = cards.value.find { it.id == cardId }
                    if (card != null && totalFrequency > 0) {
                        card to (frequency.toFloat() / totalFrequency)
                    } else null
                }.sortedByDescending { it.second }

                _predictedCards.value = predictions
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error predicting next cards from local ngrams", e)
                _predictedCards.value = emptyList()
            }
        }
    }

    fun saveNgram(selectedCards: List<Card>) {
        viewModelScope.launch {
            val userId = AuthenticationManager.getCurrentUserId() ?: "local_offline_user"
            if (selectedCards.size < 2) {
                Log.d("MainViewModel", "Not enough cards for ngram (${selectedCards.size} cards)")
                return@launch
            }

            try {
                // Create sequence of card IDs
                val sequence = selectedCards.map { it.id }
                val sequenceHash = sequence.joinToString("_")
                Log.d("MainViewModel", "Saving ngram with sequence: $sequenceHash")

                // Check if this ngram already exists locally
                val existing = localDataRepository.getNgramBySequence(userId, sequenceHash)

                if (existing != null) {
                    val updated = existing.increment()
                    Log.d("MainViewModel", "Updating existing local ngram for sequence: $sequenceHash")
                    localDataRepository.updateNgram(updated)
                } else {
                    val ngram = Ngram(
                        userId = userId,
                        sequence = sequence,
                        frequency = 1,
                        lastUsed = System.currentTimeMillis(),
                        sequenceHash = sequenceHash
                    )
                    Log.d("MainViewModel", "Creating new local ngram with sequence: $sequenceHash")
                    localDataRepository.insertNgram(ngram)
                }

                Log.d("MainViewModel", "Successfully saved ngram locally")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error saving ngram locally", e)
            }
        }
    }

    // Method to manually switch to offline mode if needed
    fun switchToOfflineMode() {
        _isOffline.value = true
        loadOfflineData()
    }

    // Method to manually sync user data from Firebase to local database
    fun syncUserData() = viewModelScope.launch {
        try {
            val userId = AuthenticationManager.getCurrentUserId() ?: return@launch
            _isLoading.value = true
            
            val syncResult = dataSyncService.syncAllUserData(userId)
            Log.d("MainViewModel", "Manual sync completed: $syncResult")
            
            // Reload data from local database
            loadOfflineData()
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error during manual sync", e)
            _isLoading.value = false
        }
    }

    // Method to force stop loading (for debugging)
    fun forceStopLoading() {
        Log.w("MainViewModel", "Force stopping loading state")
        _isLoading.value = false
    }

    // Method to manually populate default content (for debugging)
    fun populateDefaultContent() = viewModelScope.launch {
        try {
            Log.d("MainViewModel", "Manually populating default content")
            // This would need to be injected, but for now let's just reload data
            loadOfflineDataWithoutAuth()
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error manually populating default content", e)
        }
    }

    // Method to test database connectivity (for debugging)
    fun testDatabaseConnectivity() = viewModelScope.launch {
        try {
            Log.d("MainViewModel", "Testing database connectivity")
            val cardCount = localDataRepository.getAllCards().first().size
            val folderCount = localDataRepository.getAllFolders().first().size
            Log.d("MainViewModel", "Database test - Cards: $cardCount, Folders: $folderCount")
        } catch (e: Exception) {
            Log.e("MainViewModel", "Database connectivity test failed", e)
        }
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
            val newSelection = current + card
            newSelection
        }
    }

    fun addCardToSelection(card: Card) {
        _selectedCards.update { current ->
            val newSelection = current + card
            newSelection
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
    private fun updateSortedItems() {
        val allItems = mutableListOf<Any>()
        allItems.addAll(_folders.value)
        allItems.addAll(_cards.value.filter { it.folderId.isEmpty() })

        Log.d("MainViewModel", "updateSortedItems - folders: ${_folders.value.size}, cards: ${_cards.value.size}, unassigned cards: ${_cards.value.filter { it.folderId.isEmpty() }.size}")

        // Apply current sorting if any
        when (itemOrderPreference.value) {
            ItemOrder.FOLDER_FIRST -> allItems.sortBy { it !is Folder }
            ItemOrder.CARD_FIRST -> allItems.sortBy { it !is Card }
            ItemOrder.UNSORTED -> {} // No sorting needed
        }

        _sortedItems.value = allItems
        Log.d("MainViewModel", "updateSortedItems - final sortedItems: ${allItems.size}")
    }

    fun sortItems(sortType: SortType) = viewModelScope.launch {
        _lastSortType.value = sortType
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

        // Update local order and mark items for sync
        sortedItems.forEachIndexed { index, item ->
            when (item) {
                is Folder -> {
                    try {
                        localDataRepository.updateFolderOrder(item.id, index)
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Failed to update folder order locally", e)
                    }
                }
                is Card -> {
                    try {
                        localDataRepository.updateCardOrder(item.id, index)
                    } catch (e: Exception) {
                        Log.e("MainViewModel", "Failed to update card order locally", e)
                    }
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
            // Check if this is a default card by comparing its properties with DefaultContent
            val isDefaultCard = DefaultContent.defaultCards.any { defaultCard ->
                defaultCard["label"] == card.label &&
                defaultCard["vocalization"] == card.vocalization &&
                defaultCard["color"] == card.color &&
                defaultCard["folderId"] == card.folderId &&
                defaultCard["cloudinaryPublicId"] == card.cloudinaryPublicId
            }

            // Only delete from Cloudinary if it's not a default card
            if (!isDefaultCard && card.cloudinaryPublicId.isNotEmpty()) {
                val deleteResult = CloudinaryManager.deleteImage(card.cloudinaryPublicId)
                Log.d("CardDeletion", "Cloudinary deletion result: $deleteResult")
            }

            // Soft-delete locally and mark for sync
            localDataRepository.softDeleteCard(card.id)

            // Update in-memory state so UI reflects the change immediately
            _cards.value = _cards.value.filterNot { it.id == card.id }
        } catch (e: Exception) {
            Log.e("CardDeletion", "Error deleting card", e)
            throw e
        }
    }

    private suspend fun deleteFolder(folder: Folder) {
        try {
            // Delete all cards in this folder (soft-delete, with Cloudinary cleanup)
            val cardsInFolder = _cards.value.filter { it.folderId == folder.id }
            cardsInFolder.forEach { card ->
                deleteCard(card)
            }

            // Soft-delete the folder locally and mark for sync
            localDataRepository.softDeleteFolder(folder.id)

            // Update in-memory state so UI reflects the change immediately
            _folders.value = _folders.value.filterNot { it.id == folder.id }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error deleting folder", e)
        }
    }
}