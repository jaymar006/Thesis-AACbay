package com.example.ripdenver.utils

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID

object ImageUploader {
    private val storage = FirebaseStorage.getInstance()
    private val cardsRef = storage.reference.child("cards")

    suspend fun uploadCardImage(
        context: Context,
        uri: Uri,
        onProgress: (percentage: Float) -> Unit = {}
    ): String {
        val fileExtension = context.contentResolver.getType(uri)?.split("/")?.last() ?: "jpg"
        val filename = "${UUID.randomUUID()}.$fileExtension"
        val ref = cardsRef.child(filename)

        val uploadTask = ref.putFile(uri)

        uploadTask.addOnProgressListener { snapshot ->
            val progress = (100f * snapshot.bytesTransferred) / snapshot.totalByteCount
            onProgress(progress)
        }

        return uploadTask.await().storage.path
    }

    fun getImageReference(path: String): StorageReference {
        return FirebaseStorage.getInstance().getReference(path)
    }
}