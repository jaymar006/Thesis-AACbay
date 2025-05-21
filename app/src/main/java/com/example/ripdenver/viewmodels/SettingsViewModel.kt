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

    val appVersion = "1.0.0" // Replace with actual version

    private val _showPredictions = mutableStateOf(true)
    val showPredictions: State<Boolean> = _showPredictions

    private var initialColumnCount = 6
    private var initialShowPredictions = true

    init {
        loadUserSettings()
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
            }
            .addOnFailureListener { e ->
                Log.e("SettingsViewModel", "Failed to load settings", e)
            }
    }

    private fun checkForChanges() {
        _hasUnsavedChanges.value = _columnCount.value != initialColumnCount ||
                _showPredictions.value != initialShowPredictions
    }

    fun togglePredictions(enabled: Boolean) {
        _showPredictions.value = enabled
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

    fun saveSettings() {
        viewModelScope.launch {
            val userId = AuthenticationManager.getCurrentUserId() ?: return@launch
            try {
                Firebase.database.reference.child("users").child(userId).child("settings")
                    .updateChildren(
                        mapOf(
                            "columnCount" to _columnCount.value,
                            "showPredictions" to _showPredictions.value
                        )
                    )
                    .addOnSuccessListener {
                        initialColumnCount = _columnCount.value
                        initialShowPredictions = _showPredictions.value
                        _hasUnsavedChanges.value = false
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
                val json = snapshot.value.toString()
                val file = File(context.cacheDir, "aacbay_backup.json")
                FileOutputStream(file).use {
                    it.write(json.toByteArray())
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Export Database"))
            }
            .addOnFailureListener {
                Log.e("SettingsViewModel", "Export failed", it)
            }
    }

    fun importDatabase(context: Context, uri: Uri) {
        val userId = AuthenticationManager.getCurrentUserId() ?: return
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader().use { it?.readText() }

            // Import to Firebase under user's path
            json?.let {
                Firebase.database.reference.child("users").child(userId).setValue(it)
                    .addOnSuccessListener {
                        Log.d("SettingsViewModel", "Import successful")
                        // Reload settings after import
                        loadUserSettings()
                    }
                    .addOnFailureListener { e ->
                        Log.e("SettingsViewModel", "Import failed", e)
                    }
            }
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Import failed", e)
        }
    }
}