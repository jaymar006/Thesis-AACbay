package com.example.ripdenver.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ripdenver.models.Card
import com.example.ripdenver.utils.CloudinaryManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class CardViewModel : ViewModel() {
    private val database = Firebase.database.reference

    fun saveCard(
        context: Context,
        card: Card,
        imageUri: Uri? = null,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val finalCard = if (imageUri != null) {
                    val (url, publicId) = CloudinaryManager.uploadImage(context, imageUri)
                    card.copy(
                        cloudinaryUrl = url,
                        cloudinaryPublicId = publicId
                    )
                } else {
                    card
                }

                database.child("cards").child(finalCard.id)
                    .setValue(finalCard)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            onComplete()
                        } else {
                            onError(Exception(it.exception?.message ?: "Unknown error"))
                        }
                    }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}
