package com.example.ripdenver.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.ripdenver.BuildConfig.API_KEY
import com.example.ripdenver.BuildConfig.API_SECRET
import com.example.ripdenver.BuildConfig.CLOUD_NAME
import java.io.File
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

    suspend fun uploadImageToCloudinary(context: Context, file: File): String? {
        return try {
            CloudinaryManager.uploadImage(context, Uri.fromFile(file))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadImage(context: Context, uri: Uri): String {
        return suspendCoroutine { continuation ->
            MediaManager.get().upload(uri)
                .option("folder", "aac_cards")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}
                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String, resultData: Map<Any?, Any?>) {
                        (resultData["url"] as? String)?.let {
                            continuation.resume(it)
                        } ?: continuation.resumeWithException(Exception("No URL returned"))
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resumeWithException(Exception(error.description))
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch(context)
        }
    }
}