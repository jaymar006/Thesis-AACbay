package com.example.ripdenver.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.utils.AuthenticationManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges = _hasUnsavedChanges.asStateFlow()

    private val _columnCount = mutableStateOf(6)
    val columnCount: State<Int> = _columnCount

    private val _boardImageSize = mutableStateOf("medium")
    val boardImageSize: State<String> = _boardImageSize

    private val _containerImageSize = mutableStateOf("medium")
    val containerImageSize: State<String> = _containerImageSize

    private val _boardTextSize = mutableStateOf("medium")
    val boardTextSize: State<String> = _boardTextSize

    private val _containerTextSize = mutableStateOf("medium")
    val containerTextSize: State<String> = _containerTextSize

    val appVersion = "1.0.0" // Replace with actual version

    private val _showPredictions = mutableStateOf(true)
    val showPredictions: State<Boolean> = _showPredictions

    private val _allowDataSharing = mutableStateOf(false)
    val allowDataSharing: State<Boolean> = _allowDataSharing

    private var initialColumnCount = 6
    private var initialShowPredictions = true
    private var initialAllowDataSharing = false
    private var initialBoardImageSize = "medium"
    private var initialContainerImageSize = "medium"
    private var initialBoardTextSize = "medium"
    private var initialContainerTextSize = "medium"

    // Operation status states
    private val _showOperationStatus = mutableStateOf(false)
    val showOperationStatus: State<Boolean> = _showOperationStatus

    private val _operationMessage = mutableStateOf("")
    val operationMessage: State<String> = _operationMessage

    private val _isOperationSuccess = mutableStateOf(false)
    val isOperationSuccess: State<Boolean> = _isOperationSuccess

    init {
        loadUserSettings()
    }

    private fun showOperationResult(success: Boolean, message: String) {
        _isOperationSuccess.value = success
        _operationMessage.value = message
        _showOperationStatus.value = true
    }

    fun dismissOperationStatus() {
        _showOperationStatus.value = false
    }

    private fun loadUserSettings() {
        val userId = AuthenticationManager.getCurrentUserId() ?: return
        Firebase.database.reference.child("users").child(userId).child("settings").get()
            .addOnSuccessListener { snapshot ->
                snapshot.child("columnCount").getValue(Int::class.java)?.let {
                    _columnCount.value = it
                    initialColumnCount = it
                }
                snapshot.child("showPredictions").getValue(Boolean::class.java)?.let {
                    _showPredictions.value = it
                    initialShowPredictions = it
                }
                snapshot.child("allowDataSharing").getValue(Boolean::class.java)?.let {
                    _allowDataSharing.value = it
                    initialAllowDataSharing = it
                }
                snapshot.child("boardImageSize").getValue(String::class.java)?.let {
                    _boardImageSize.value = it
                    initialBoardImageSize = it
                }
                snapshot.child("containerImageSize").getValue(String::class.java)?.let {
                    _containerImageSize.value = it
                    initialContainerImageSize = it
                }
                snapshot.child("boardTextSize").getValue(String::class.java)?.let {
                    _boardTextSize.value = it
                    initialBoardTextSize = it
                }
                snapshot.child("containerTextSize").getValue(String::class.java)?.let {
                    _containerTextSize.value = it
                    initialContainerTextSize = it
                }
            }
            .addOnFailureListener { e ->
                Log.e("SettingsViewModel", "Failed to load settings", e)
            }
    }

    private fun checkForChanges() {
        _hasUnsavedChanges.value = _columnCount.value != initialColumnCount ||
                _showPredictions.value != initialShowPredictions ||
                _allowDataSharing.value != initialAllowDataSharing ||
                _boardImageSize.value != initialBoardImageSize ||
                _containerImageSize.value != initialContainerImageSize ||
                _boardTextSize.value != initialBoardTextSize ||
                _containerTextSize.value != initialContainerTextSize
    }

    fun togglePredictions(enabled: Boolean) {
        _showPredictions.value = enabled
        checkForChanges()
    }

    fun toggleDataSharing(enabled: Boolean) {
        _allowDataSharing.value = enabled
        checkForChanges()
    }

    fun incrementColumns() {
        if (_columnCount.value < 12) {
            _columnCount.value++
            checkForChanges()
        }
    }

    fun decrementColumns() {
        if (_columnCount.value > 2) {
            _columnCount.value--
            checkForChanges()
        }
    }

    fun setBoardImageSize(size: String) {
        _boardImageSize.value = size
        checkForChanges()
    }

    fun setContainerImageSize(size: String) {
        _containerImageSize.value = size
        checkForChanges()
    }

    fun setBoardTextSize(size: String) {
        _boardTextSize.value = size
        checkForChanges()
    }

    fun setContainerTextSize(size: String) {
        _containerTextSize.value = size
        checkForChanges()
    }

    fun saveSettings() {
        viewModelScope.launch {
            val userId = AuthenticationManager.getCurrentUserId() ?: return@launch
            try {
                Firebase.database.reference.child("users").child(userId).child("settings")
                    .updateChildren(
                        mapOf(
                            "columnCount" to _columnCount.value,
                            "showPredictions" to _showPredictions.value,
                            "allowDataSharing" to _allowDataSharing.value,
                            "boardImageSize" to _boardImageSize.value,
                            "containerImageSize" to _containerImageSize.value,
                            "boardTextSize" to _boardTextSize.value,
                            "containerTextSize" to _containerTextSize.value
                        )
                    )
                    .addOnSuccessListener {
                        // Update initial values to match current values
                        initialColumnCount = _columnCount.value
                        initialShowPredictions = _showPredictions.value
                        initialAllowDataSharing = _allowDataSharing.value
                        initialBoardImageSize = _boardImageSize.value
                        initialContainerImageSize = _containerImageSize.value
                        initialBoardTextSize = _boardTextSize.value
                        initialContainerTextSize = _containerTextSize.value
                        _hasUnsavedChanges.value = false
                        
                        // Force a refresh of the settings
                        loadUserSettings()
                        
                        Log.d("SettingsViewModel", "Settings saved successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("SettingsViewModel", "Failed to save settings", e)
                    }
            } catch (e: Exception) {
                Log.e("SettingsViewModel", "Error saving settings", e)
            }
        }
    }

    fun exportDatabase(context: Context) {
        val userId = AuthenticationManager.getCurrentUserId() ?: return
        Firebase.database.reference.child("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                try {
                    // Convert the data to a properly formatted JSON string
                    val jsonData = snapshot.value?.let { data ->
                        val dataMap = data as? Map<String, Any> ?: mapOf()
                        // Create a map with only the necessary data
                        val exportData = mapOf(
                            "cards" to (dataMap["cards"] as? Map<String, Any> ?: mapOf()),
                            "folders" to (dataMap["folders"] as? Map<String, Any> ?: mapOf())
                        )
                        // Convert to JSON string with proper formatting
                        com.google.gson.GsonBuilder()
                            .setPrettyPrinting()
                            .create()
                            .toJson(exportData)
                    } ?: "{}"

                    // Create a file in the Downloads directory
                    val timestamp = System.currentTimeMillis()
                    val fileName = "aacbay_export_$timestamp.json"
                    var fileUri: Uri? = null
                    
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        // For Android 10 and above, use MediaStore
                        val contentValues = android.content.ContentValues().apply {
                            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
                            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
                        }

                        val resolver = context.contentResolver
                        val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        
                        uri?.let {
                            resolver.openOutputStream(it)?.use { outputStream ->
                                outputStream.write(jsonData.toByteArray())
                            }
                            fileUri = uri
                        }
                    } else {
                        // For Android 9 and below, use direct file access
                        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                        val file = File(downloadsDir, fileName)
                        FileOutputStream(file).use {
                            it.write(jsonData.toByteArray())
                        }
                        fileUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                    }

                    // If file was saved successfully, show share options
                    fileUri?.let { uri ->
                        // Create and launch the share intent
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Database"))
                        showOperationResult(true, "Data exported to Downloads folder and ready to share")
                    } ?: showOperationResult(false, "Failed to create file in Downloads")
                } catch (e: Exception) {
                    Log.e("SettingsViewModel", "Export failed", e)
                    showOperationResult(false, "Failed to export data: ${e.message}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("SettingsViewModel", "Export failed", e)
                showOperationResult(false, "Failed to export data: ${e.message}")
            }
    }

    fun importDatabase(context: Context, uri: Uri) {
        val userId = AuthenticationManager.getCurrentUserId() ?: return
        Log.d("SettingsViewModel", "Starting import from JSON file")
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader().use { it?.readText() }
            Log.d("SettingsViewModel", "JSON file read successfully")

            // Parse the JSON data
            json?.let {
                try {
                    Log.d("SettingsViewModel", "Attempting to parse JSON data")
                    val type = object : com.google.gson.reflect.TypeToken<Map<String, Map<String, Any>>>() {}.type
                    val importData = com.google.gson.Gson().fromJson<Map<String, Map<String, Any>>>(it, type)
                    Log.d("SettingsViewModel", "JSON parsed successfully")

                    // Get current user's data to compare
                    Log.d("SettingsViewModel", "Fetching current user data")
                    Firebase.database.reference.child("users").child(userId)
                        .get()
                        .addOnSuccessListener { currentSnapshot ->
                            Log.d("SettingsViewModel", "Current user data fetched successfully")
                            val updates = mutableMapOf<String, Any>()
                            var newItemsCount = 0

                            // Create type indicators for Firebase
                            val folderType = object : com.google.firebase.database.GenericTypeIndicator<Map<String, Any>>() {}
                            val cardType = object : com.google.firebase.database.GenericTypeIndicator<Map<String, Any>>() {}

                            // Collect existing image URLs
                            val existingImageUrls = mutableSetOf<String>()
                            currentSnapshot.child("cards").children.forEach { existingCard ->
                                val existingCardData = existingCard.getValue(cardType)
                                existingCardData?.get("imageUrl")?.toString()?.let { url ->
                                    existingImageUrls.add(url)
                                }
                            }

                            // Process folders
                            Log.d("SettingsViewModel", "Processing folders")
                            importData["folders"]?.forEach { (folderId, folderData) ->
                                try {
                                    val folderMap = folderData as? Map<String, Any>
                                    val folderName = folderMap?.get("name") as? String
                                    Log.d("SettingsViewModel", "Checking folder: $folderName (ID: $folderId)")
                                    
                                    // Skip if folder with same ID exists
                                    if (currentSnapshot.child("folders").child(folderId).exists()) {
                                        Log.d("SettingsViewModel", "Skipping folder - ID exists: $folderId")
                                        return@forEach
                                    }
                                    
                                    // Skip if folder with same name exists
                                    var nameExists = false
                                    currentSnapshot.child("folders").children.forEach { existingFolder ->
                                        val existingFolderData = existingFolder.getValue(folderType)
                                        if (existingFolderData?.get("name") as? String == folderName) {
                                            nameExists = true
                                            Log.d("SettingsViewModel", "Skipping folder - name exists: $folderName")
                                            return@forEach
                                        }
                                    }
                                    
                                    if (!nameExists) {
                                        updates["folders/$folderId"] = folderData
                                        newItemsCount++
                                        Log.d("SettingsViewModel", "Adding new folder: $folderName")
                                    }
                                } catch (e: Exception) {
                                    Log.e("SettingsViewModel", "Error processing folder $folderId", e)
                                }
                            }

                            // Process cards
                            Log.d("SettingsViewModel", "Processing cards")
                            importData["cards"]?.forEach { (cardId, cardData) ->
                                try {
                                    val cardMap = cardData as? Map<String, Any>
                                    val cardLabel = cardMap?.get("label") as? String
                                    val imageUrl = cardMap?.get("imageUrl") as? String
                                    Log.d("SettingsViewModel", "Checking card: $cardLabel (ID: $cardId)")
                                    
                                    // Skip if card with same ID exists
                                    if (currentSnapshot.child("cards").child(cardId).exists()) {
                                        Log.d("SettingsViewModel", "Skipping card - ID exists: $cardId")
                                        return@forEach
                                    }
                                    
                                    // Skip if card with same label exists
                                    var labelExists = false
                                    currentSnapshot.child("cards").children.forEach { existingCard ->
                                        val existingCardData = existingCard.getValue(cardType)
                                        if (existingCardData?.get("label") as? String == cardLabel) {
                                            labelExists = true
                                            Log.d("SettingsViewModel", "Skipping card - label exists: $cardLabel")
                                            return@forEach
                                        }
                                    }
                                    
                                    if (!labelExists) {
                                        // If the image URL already exists in our database, we can safely use it
                                        if (imageUrl != null && existingImageUrls.contains(imageUrl)) {
                                            Log.d("SettingsViewModel", "Card uses existing image URL: $imageUrl")
                                        }
                                        
                                        updates["cards/$cardId"] = cardData
                                        newItemsCount++
                                        Log.d("SettingsViewModel", "Adding new card: $cardLabel")
                                    }
                                } catch (e: Exception) {
                                    Log.e("SettingsViewModel", "Error processing card $cardId", e)
                                }
                            }

                            Log.d("SettingsViewModel", "Found $newItemsCount new items to import")
                            if (updates.isEmpty()) {
                                Log.d("SettingsViewModel", "No new items to import")
                                showOperationResult(true, "No new items to import")
                                return@addOnSuccessListener
                            }

                            // Apply the updates
                            Log.d("SettingsViewModel", "Applying updates to database")
                            Firebase.database.reference.child("users").child(userId)
                                .updateChildren(updates)
                                .addOnSuccessListener {
                                    Log.d("SettingsViewModel", "Import successful")
                                    loadUserSettings()
                                    showOperationResult(true, "Successfully imported $newItemsCount new items")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("SettingsViewModel", "Import failed", e)
                                    showOperationResult(false, "Failed to import data: ${e.message}")
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("SettingsViewModel", "Failed to get current user data", e)
                            showOperationResult(false, "Failed to get current user data")
                        }
                } catch (e: Exception) {
                    Log.e("SettingsViewModel", "Failed to parse JSON", e)
                    showOperationResult(false, "Invalid JSON format")
                }
            } ?: run {
                Log.e("SettingsViewModel", "Failed to read import file")
                showOperationResult(false, "Failed to read import file")
            }
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Import failed", e)
            showOperationResult(false, "Failed to import data: ${e.message}")
        }
    }

    fun importFromUserId(context: Context, sourceUserId: String) {
        val currentUserId = AuthenticationManager.getCurrentUserId() ?: return
        try {
            // First check if the source user allows data sharing
            Firebase.database.reference.child("users").child(sourceUserId).child("settings").child("allowDataSharing")
                .get()
                .addOnSuccessListener { snapshot ->
                    val allowsSharing = snapshot.getValue(Boolean::class.java) ?: false
                    
                    if (!allowsSharing) {
                        showOperationResult(false, "This user has not enabled data sharing")
                        return@addOnSuccessListener
                    }

                    // If sharing is allowed, get both users' data
                    Firebase.database.reference.child("users").child(sourceUserId)
                        .get()
                        .addOnSuccessListener { sourceSnapshot ->
                            if (!sourceSnapshot.exists()) {
                                showOperationResult(false, "User not found")
                                return@addOnSuccessListener
                            }

                            // Get current user's data
                            Firebase.database.reference.child("users").child(currentUserId)
                                .get()
                                .addOnSuccessListener { currentSnapshot ->
                                    val updates = mutableMapOf<String, Any>()
                                    var newItemsCount = 0

                                    // Process folders
                                    sourceSnapshot.child("folders").children.forEach { sourceFolder ->
                                        val folderId = sourceFolder.key ?: return@forEach
                                        if (!currentSnapshot.child("folders").child(folderId).exists()) {
                                            updates["folders/$folderId"] = sourceFolder.value ?: return@forEach
                                            newItemsCount++
                                        }
                                    }

                                    // Process cards
                                    sourceSnapshot.child("cards").children.forEach { sourceCard ->
                                        val cardId = sourceCard.key ?: return@forEach
                                        if (!currentSnapshot.child("cards").child(cardId).exists()) {
                                            updates["cards/$cardId"] = sourceCard.value ?: return@forEach
                                            newItemsCount++
                                        }
                                    }

                                    if (updates.isEmpty()) {
                                        showOperationResult(true, "No new items to import")
                                        return@addOnSuccessListener
                                    }

                                    // Apply the updates
                                    Firebase.database.reference.child("users").child(currentUserId)
                                        .updateChildren(updates)
                                        .addOnSuccessListener {
                                            Log.d("SettingsViewModel", "Import from user ID successful")
                                            loadUserSettings()
                                            showOperationResult(true, "Successfully imported $newItemsCount new items")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("SettingsViewModel", "Import from user ID failed", e)
                                            showOperationResult(false, "Failed to import data: ${e.message}")
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.e("SettingsViewModel", "Failed to get current user data", e)
                                    showOperationResult(false, "Failed to get current user data")
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("SettingsViewModel", "Failed to get source user data", e)
                            showOperationResult(false, "Failed to get source user data. Please check if the User ID is correct.")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("SettingsViewModel", "Failed to check sharing permission", e)
                    showOperationResult(false, "Failed to check sharing permission. Please check if the User ID is correct.")
                }
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Import from user ID failed", e)
            showOperationResult(false, "Failed to import data: ${e.message}")
        }
    }

    fun getUserDisplayId(): String? {
        return AuthenticationManager.getCurrentUserId()
    }
}