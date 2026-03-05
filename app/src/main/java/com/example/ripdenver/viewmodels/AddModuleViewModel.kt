package com.example.ripdenver.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.repository.LocalDataRepository
import com.example.ripdenver.models.ArasaacPictogram
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.state.AddModuleState
import com.example.ripdenver.utils.CloudinaryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddModuleViewModel @Inject constructor(
    private val localDataRepository: LocalDataRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddModuleState())
    val uiState: StateFlow<AddModuleState> = _uiState.asStateFlow()

    private val _pictograms = mutableStateOf<List<ArasaacPictogram>>(emptyList())
    // Expose as public immutable state
    val pictograms: List<ArasaacPictogram> get() = _pictograms.value

    private val _isLoadingPictograms = mutableStateOf(false)
    val isLoadingPictograms: Boolean get() = _isLoadingPictograms.value

    private var searchJob: kotlinx.coroutines.Job? = null
    private val searchDelay = 500L // 500ms delay for debouncing

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

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

        @GET("v1/pictograms/{language}/bestsearch/{searchText}")
        suspend fun searchPictograms(
            @Path("language") language: String = "en",
            @Path("searchText") searchText: String
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


    fun searchPictograms(query: String) {
        // Cancel previous search job if it exists
        searchJob?.cancel()

        // Start new search job
        searchJob = viewModelScope.launch {
            _isLoadingPictograms.value = true
            try {
                delay(searchDelay) // Add debouncing delay
                _pictograms.value = if (query.isBlank()) {
                    arasaacApi.getAllPictograms()
                } else {
                    arasaacApi.searchPictograms(searchText = query)
                }
            } catch (e: Exception) {
                _pictograms.value = emptyList()
            } finally {
                _isLoadingPictograms.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun setFolderId(folderId: String) {
        _uiState.value = _uiState.value.copy(folderId = folderId)
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

    fun updateCardImage(urlAndId: Pair<String, String>) {
        _uiState.value = _uiState.value.copy(cardImagePath = urlAndId)
    }

    fun updateFolderLabel(label: String) {
        _uiState.value = _uiState.value.copy(folderLabel = label)
    }

    fun updateFolderColor(color: String) {
        _uiState.value = _uiState.value.copy(folderColor = color)
    }

    fun saveFolder() = viewModelScope.launch {
        try {
            val folder = Folder(
                id = UUID.randomUUID().toString(),
                name = uiState.value.folderLabel,
                color = uiState.value.folderColor,
                createdAt = System.currentTimeMillis()
            )

            // Write to local database first and mark for sync
            localDataRepository.upsertUserFolder(folder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun uploadImageAndGetUrl(context: Context, uri: Uri): Pair<String, String> {
        return CloudinaryManager.uploadImage(context, uri)
    }

    fun saveCard(context: Context, onComplete: () -> Unit) = viewModelScope.launch {
        try {
            // Handle image upload first
            val (imageUrl, publicId) = selectedImageUri.value?.let { uri ->
                // Upload gallery image to Cloudinary
                CloudinaryManager.uploadImage(context, uri)
            } ?: uiState.value.cardImagePath // Use existing image path if no gallery image

            val card = uiState.value.run {
                Card(
                    id = UUID.randomUUID().toString(),
                    label = cardLabel,
                    vocalization = cardVocalization,
                    color = cardColor,
                    cloudinaryUrl = imageUrl,
                    cloudinaryPublicId = publicId,
                    folderId = folderId,
                    usageCount = 0,
                    lastUsed = System.currentTimeMillis()
                )
            }

            // Write to local database first and mark for sync
            localDataRepository.upsertUserCard(card)

            onComplete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    suspend fun saveImageToFirebase(card: Card) {
        // Kept for compatibility, but prefer saveCard() for new cards.
        localDataRepository.upsertUserCard(card)
    }

    private suspend fun downloadImage(context: Context, imageUrl: String): File {
        return withContext(Dispatchers.IO) {
            // Directory where we persist ARASAAC pictogram images for offline use
            val cacheDir = File(context.filesDir, "arasaac_cache").apply {
                if (!exists()) {
                    mkdirs()
                }
            }

            // Derive a stable file name from the URL so the same pictogram
            // is reused across sessions and available while offline.
            val fileName = Uri.parse(imageUrl).lastPathSegment ?: "pictogram_${UUID.randomUUID()}.png"
            val cachedFile = File(cacheDir, fileName)

            // If we've already downloaded this image before, return it directly
            if (cachedFile.exists() && cachedFile.length() > 0) {
                return@withContext cachedFile
            }

            // Otherwise download from the network and store it for future offline use
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            try {
                cachedFile.outputStream().use { output ->
                    connection.inputStream.use { input ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                // If something goes wrong while writing, make sure we don't keep a corrupt file
                if (cachedFile.exists()) {
                    cachedFile.delete()
                }
                throw e
            }

            cachedFile
        }
    }

    suspend fun uploadArasaacImage(context: Context, imageUrl: String): Pair<String, String> {
        return withContext(Dispatchers.IO) {
            try {
                // Download the image
                val file = downloadImage(context, imageUrl)

                // Upload the file to Cloudinary
                val (url, publicId) = CloudinaryManager.uploadImage(context, Uri.fromFile(file))
                if (url.isEmpty() || publicId.isEmpty()) {
                    throw Exception("Cloudinary upload failed: returned null or empty values")
                }
                Pair(url, publicId)
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
        onSuccess: (Pair<String, String>) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val urlAndId = uploadArasaacImage(context, imageUrl)
                updateCardImage(urlAndId)
                onSuccess(urlAndId)
            } catch (e: Exception) {
                onError("Failed to upload symbol: ${e.message}")
            }
        }
    }
}
