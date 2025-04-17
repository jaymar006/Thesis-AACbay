package com.example.ripdenver.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.ripdenver.BuildConfig.API_KEY
import com.example.ripdenver.BuildConfig.API_SECRET
import com.example.ripdenver.BuildConfig.CLOUD_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        return withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                try {
                    android.util.Log.d("Cloudinary", "Starting image deletion for: $publicId")

                    val cloudinary = MediaManager.get().getCloudinary()

                    val options = mapOf(
                        "resource_type" to "image",
                        "invalidate" to true
                    )

                    val result = cloudinary.uploader().destroy(publicId, options)
                    android.util.Log.d("Cloudinary", "Raw deletion result: $result")

                    val success = result?.get("result") == "ok"
                    android.util.Log.d("Cloudinary", "Deletion success: $success")

                    continuation.resume(success)
                } catch (e: Exception) {
                    android.util.Log.e("Cloudinary", "Error during deletion", e)
                    e.printStackTrace()
                    continuation.resume(false)
                }
            }
        }
    }
}