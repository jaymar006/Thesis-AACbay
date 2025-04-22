package com.example.ripdenver.viewmodels

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.ArasaacPictogram
import com.example.ripdenver.models.Card
import com.example.ripdenver.state.EditCardState
import com.example.ripdenver.utils.CloudinaryManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

class EditCardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditCardState())
    val uiState: StateFlow<EditCardState> = _uiState.asStateFlow()

    private val _pictograms = mutableStateOf<List<ArasaacPictogram>>(emptyList())
    val pictograms: List<ArasaacPictogram> get() = _pictograms.value

    var isLoadingPictograms by mutableStateOf(false)
        private set

    private var _selectedImageUrl = MutableStateFlow<String?>(null)
    val selectedImageUrl: StateFlow<String?> = _selectedImageUrl.asStateFlow()

    private var searchJob: Job? = null
    private val searchDelay = 500L // 500ms delay for debouncing

    // ArasaacApiService interface
    interface ArasaacApiService {
        @GET("v1/pictograms/{language}/bestsearch/{searchText}")
        suspend fun searchPictograms(
            @Path("language") language: String = "en",
            @Path("searchText") searchText: String
        ): List<ArasaacPictogram>

        @GET("v1/pictograms/all/{language}")
        suspend fun getAllPictograms(
            @Path("language") language: String = "en"
        ): List<ArasaacPictogram>
    }

    // Arasaac API client
    private val arasaacApi: ArasaacApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.arasaac.org/") // Changed base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ArasaacApiService::class.java)
    }

    fun loadCardData(cardId: String) {
        viewModelScope.launch {
            try {
                val cardSnapshot = Firebase.database.reference
                    .child("cards")
                    .child(cardId)
                    .get()
                    .await()

                val card = cardSnapshot.getValue(Card::class.java)
                card?.let {
                    _uiState.value = EditCardState(
                        cardId = it.id,
                        cardLabel = it.label,
                        cardVocalization = it.vocalization,
                        cardColor = it.color,
                        cardImagePath = Pair(it.cloudinaryUrl, it.cloudinaryPublicId),
                        folderId = it.folderId
                    )
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
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

    fun searchPictograms(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            isLoadingPictograms = true
            try {
                delay(searchDelay) // Debouncing delay
                _pictograms.value = if (query.isBlank()) {
                    arasaacApi.getAllPictograms()
                } else {
                    arasaacApi.searchPictograms(searchText = query)
                }
            } catch (e: Exception) {
                _pictograms.value = emptyList()
            } finally {
                isLoadingPictograms = false
            }
        }
    }

    fun loadAllPictograms() {
        viewModelScope.launch {
            isLoadingPictograms = true
            try {
                _pictograms.value = arasaacApi.getAllPictograms()
            } catch (e: Exception) {
                _pictograms.value = emptyList()
            } finally {
                isLoadingPictograms = false
            }
        }
    }

    suspend fun updateCard(context: Context, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                // Upload image if a new one was selected
                val urlAndId = _selectedImageUrl.value?.let { url ->
                    uploadArasaacImage(context, url)
                } ?: uiState.value.cardImagePath

                val card = uiState.value.run {
                    Card(
                        id = cardId,
                        label = cardLabel,
                        vocalization = cardVocalization,
                        color = cardColor,
                        cloudinaryUrl = urlAndId.first,
                        cloudinaryPublicId = urlAndId.second,
                        folderId = folderId,
                        usageCount = 0,
                        lastUsed = System.currentTimeMillis()
                    )
                }

                Firebase.database.reference
                    .child("cards")
                    .child(card.id)
                    .setValue(card)
                    .await()

                // Clear selected image URL
                _selectedImageUrl.value = null
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun uploadImageAndGetUrl(context: Context, uri: Uri): Pair<String, String> {
        return CloudinaryManager.uploadImage(context, uri)
    }

    private suspend fun downloadImage(context: Context, imageUrl: String): File {
        return withContext(Dispatchers.IO) {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            val file = File(context.cacheDir, "temp_image.jpg")
            file.outputStream().use { output ->
                connection.inputStream.use { input ->
                    input.copyTo(output)
                }
            }
            file
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

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}