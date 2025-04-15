package com.example.ripdenver.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.ArasaacPictogram
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.state.AddModuleState
import com.example.ripdenver.utils.CloudinaryManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
                    id = UUID.randomUUID().toString(), // Generate a unique ID
                    label = cardLabel,
                    vocalization = cardVocalization,
                    color = cardColor,
                    cloudinaryUrl = cardImagePath, // Use the updated image path
                    folderId = "", // Set folderId if applicable
                    usageCount = 0, // Default usage count
                    lastUsed = System.currentTimeMillis() // Set the current timestamp
                )
            }

            // Save the entire Card object to Firebase
            Firebase.database.reference.child("cards").child(card.id)
                .setValue(card)
                .await()

            onComplete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    suspend fun saveImageToFirebase(card: Card) {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("cards").child(card.id) // Use the card's ID as the key
        ref.setValue(card) // Save the entire Card object
    }

    private suspend fun downloadImage(context: Context, imageUrl: String): File {
        return withContext(Dispatchers.IO) {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            // Use context.cacheDir to resolve the cache directory
            val file = File(context.cacheDir, "temp_image.jpg")
            file.outputStream().use { output ->
                connection.inputStream.use { input ->
                    input.copyTo(output)
                }
            }
            file
        }
    }

    suspend fun uploadArasaacImage(context: Context, imageUrl: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Download the image
                val file = downloadImage(context, imageUrl)

                // Upload the file to Cloudinary
                val cloudinaryUrl = CloudinaryManager.uploadImage(context, Uri.fromFile(file))
                cloudinaryUrl ?: throw Exception("Cloudinary upload failed: returned null or empty URL")
            } catch (e: Exception) {
                e.printStackTrace()
                throw Exception("Failed to upload ARASAAC image: ${e.message}")
            }
        }
    }

    // Function to handle symbol selection
    fun handleSymbolSelection(
        context: Context,
        imageUrl: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Upload the image to Cloudinary and update UI state
                val cloudinaryUrl = uploadArasaacImage(context, imageUrl)
                updateCardImage(cloudinaryUrl)

                // Only notify success with the cloudinary URL
                onSuccess(cloudinaryUrl)
            } catch (e: Exception) {
                onError("Failed to upload symbol: ${e.message}")
            }
        }
    }
}
