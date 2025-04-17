package com.example.ripdenver.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.ripdenver.BuildConfig.API_KEY
import com.example.ripdenver.BuildConfig.API_SECRET
import com.example.ripdenver.BuildConfig.CLOUD_NAME
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object CloudinaryManager {

    fun initialize(context: Context) {
        val config = mapOf(
            "cloud_name" to CLOUD_NAME,
            "api_key" to API_KEY,
            "api_secret" to API_SECRET
        )
        MediaManager.init(context, config)
    }





    suspend fun uploadImage(context: Context, uri: Uri): Pair<String, String> {
        return suspendCoroutine { continuation ->
            MediaManager.get().upload(uri)
                .option("folder", "aac_cards")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                        val url = resultData["url"] as? String
                        val publicId = resultData["public_id"] as? String
                        if (url != null && publicId != null) {
                            continuation.resume(Pair(url, publicId))
                        } else {
                            continuation.resumeWithException(Exception("Missing URL or public ID"))
                        }
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resumeWithException(Exception("Cloudinary upload failed: ${error.description}"))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch(context)
        }
    }

    suspend fun deleteImage(publicId: String): Boolean {
        return suspendCoroutine { continuation ->
            try {
                val result = MediaManager.get().getCloudinary().uploader().destroy(
                    publicId,
                    mutableMapOf<Any?, Any?>()
                )
                continuation.resume(result != null)
            } catch (e: Exception) {
                continuation.resume(false)
            }
        }
    }
}