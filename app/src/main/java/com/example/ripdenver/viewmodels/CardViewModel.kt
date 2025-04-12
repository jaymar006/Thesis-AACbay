package com.example.ripdenver.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.Card
import com.example.ripdenver.utils.ImageUploader
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class CardViewModel : ViewModel() {
    private val database = Firebase.database.reference

    fun saveCard(
        context: Context,  // Add context parameter
        card: Card,
        imageUri: Uri? = null,
        onProgress: (Float) -> Unit = {},
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Upload image if provided
                val finalCard = if (imageUri != null) {
                    val imagePath = ImageUploader.uploadCardImage(
                        context = context,
                        uri = imageUri,
                        onProgress = onProgress
                    )
                    card.copy(imagePath = imagePath)
                } else {
                    card
                }

                // Save card data
                database.child("cards").child(finalCard.id).setValue(finalCard)
                onComplete()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}