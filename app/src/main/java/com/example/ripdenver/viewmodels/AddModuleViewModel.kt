package com.example.ripdenver.viewmodels

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.ArasaacPictogram
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.state.AddModuleState
import com.example.ripdenver.utils.CloudinaryManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class AddModuleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AddModuleState())
    val uiState: StateFlow<AddModuleState> = _uiState.asStateFlow()

    private val _pictograms = mutableStateOf<List<ArasaacPictogram>>(emptyList())
    // Expose as public immutable state
    val pictograms: List<ArasaacPictogram> get() = _pictograms.value

    private val _isLoadingPictograms = mutableStateOf(false)
    val isLoadingPictograms: Boolean get() = _isLoadingPictograms.value

    // Add to AddModuleViewModel.kt
    data class ArasaacSymbol(
        val id: Int,
        val keyword: String,
        val imageUrl: String
    )

    // Add API service interface
    interface ArasaacApiService {
        @GET("v1/pictograms/all/{language}")
        suspend fun getAllPictograms(
            @Path("language") language: String = "en"
        ): List<ArasaacPictogram>
    }

    // Add to AddModuleViewModel
    private val arasaacApi: ArasaacApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.arasaac.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ArasaacApiService::class.java)
    }


    fun setPictograms(newPictograms: List<ArasaacPictogram>) {
        _pictograms.value = newPictograms
    }

    fun setLoadingPictograms(isLoading: Boolean) {
        _isLoadingPictograms.value = isLoading
    }

    // In your loadAllPictograms() function:
    fun loadAllPictograms() {
        viewModelScope.launch {
            _isLoadingPictograms.value = true  // Use the mutable backing property
            try {
                _pictograms.value = arasaacApi.getAllPictograms()  // Use the mutable backing property
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoadingPictograms.value = false  // Use the mutable backing property
            }
        }
    }

    suspend fun uploadArasaacImage(context: android.content.Context, imageUrl: String): String {
        return try {
            // Download the image first
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream

            // Create a temporary file
            val file = File.createTempFile("arasaac", ".png", context.cacheDir)
            file.outputStream().use { output ->
                input.copyTo(output)
            }

            // Upload to Cloudinary
            CloudinaryManager.uploadImage(context, Uri.fromFile(file))
        } catch (e: Exception) {
            throw Exception("Failed to upload ARASAAC image: ${e.message}")
        }
    }

    fun selectCardType(isCard: Boolean) {
        _uiState.value = _uiState.value.copy(isCardSelected = isCard)
    }

    fun updateCardLabel(label: String) {
        _uiState.value = _uiState.value.copy(cardLabel = label)
    }

    fun updateCardVocalization(text: String) {
        _uiState.value = _uiState.value.copy(cardVocalization = text)
    }

    fun updateCardColor(color: String) {
        _uiState.value = _uiState.value.copy(cardColor = color)
    }

    fun updateCardImage(path: String) {
        _uiState.value = _uiState.value.copy(cardImagePath = path)
    }

    fun updateFolderLabel(label: String) {
        _uiState.value = _uiState.value.copy(folderLabel = label)
    }

    fun updateFolderColor(color: String) {
        _uiState.value = _uiState.value.copy(folderColor = color)
    }

    fun updateFolderImage(path: String) {
        _uiState.value = _uiState.value.copy(folderImagePath = path)
    }

    suspend fun saveCard(imageUri: Uri?): Boolean {
        return try {
            val card = uiState.value.run {
                Card(
                    id = UUID.randomUUID().toString(), // Generate ID if not already set
                    label = cardLabel,
                    vocalization = cardVocalization,
                    color = cardColor,
                    cloudinaryUrl = if (imageUri != null) {
                        // This will be handled by the caller who has Context
                        "" // Temporary empty string
                    } else {
                        ""
                    }
                )
            }

            Firebase.database.reference.child("cards").child(card.id)
                .setValue(card)
                .await() // Wait for Firebase operation to complete

            true // Return success
        } catch (e: Exception) {
            e.printStackTrace()
            false // Return failure
        }
    }

    fun saveFolder() = viewModelScope.launch {
        // Implement actual save logic
        val newFolder = uiState.value.run {
            Folder(
                name = folderLabel,
                color = folderColor
            )
        }
        // TODO: Save to Firebase
    }

    suspend fun uploadImageAndGetUrl(context: android.content.Context, uri: Uri): String {
        return CloudinaryManager.uploadImage(context, uri)
    }

    fun saveCard(onComplete: () -> Unit) = viewModelScope.launch {
        try {
            val card = uiState.value.run {
                Card(
                    id = UUID.randomUUID().toString(), // Generate ID if not already set
                    label = cardLabel,
                    vocalization = cardVocalization,
                    color = cardColor,
                    cloudinaryUrl = cardImagePath // Use the already updated path
                )
            }

            Firebase.database.reference.child("cards").child(card.id)
                .setValue(card)
                .await()

            onComplete()
        } catch (e: Exception) {
            // Handle error
        }
    }
}
