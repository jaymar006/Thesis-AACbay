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
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _columnCount = mutableStateOf(6)
    val columnCount: State<Int> = _columnCount


    val appVersion = "1.0.0" // Replace with actual version



    fun incrementColumns() {
        if (_columnCount.value < 12) {
            _columnCount.value++
        }
    }

    fun decrementColumns() {
        if (_columnCount.value > 2) {
            _columnCount.value--
        }
    }


    fun saveSettings() {
        viewModelScope.launch {
            Firebase.database.reference.child("settings").updateChildren(
                mapOf(
                    "columnCount" to _columnCount.value
                )
            )
        }
    }

    init {
        Firebase.database.reference.child("settings").get()
            .addOnSuccessListener { snapshot ->
                snapshot.child("columnCount").getValue(Int::class.java)?.let {
                    _columnCount.value = it
                }
            }
    }

    fun exportDatabase(context: Context) {
        Firebase.database.reference.get()
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
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val json = inputStream?.bufferedReader().use { it?.readText() }

            // Import to Firebase
            json?.let {
                Firebase.database.reference.setValue(it)
                    .addOnSuccessListener {
                        Log.d("SettingsViewModel", "Import successful")
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