package com.example.ripdenver.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TTSManager private constructor(context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private val speechRateSlower = 1f // Adjust for clearer pronunciation

    init {
        Log.d("TTSManager", "Initializing TTS")
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d("TTSManager", "TTS initialization successful")

                // Set Filipino locale with specific country variant
                val filipinoLocale = Locale("fil", "PH")
                val result = textToSpeech?.setLanguage(filipinoLocale)

                when (result) {
                    TextToSpeech.LANG_MISSING_DATA -> {
                        Log.e("TTSManager", "Language data missing")
                        // Fallback to English but adjust for Filipino pronunciation
                        textToSpeech?.language = Locale.US
                        textToSpeech?.setPitch(1.1f) // Slightly higher pitch
                        textToSpeech?.setSpeechRate(speechRateSlower)
                    }
                    TextToSpeech.LANG_NOT_SUPPORTED -> {
                        Log.w("TTSManager", "Filipino not supported, optimizing English")
                        textToSpeech?.language = Locale.US
                        textToSpeech?.setPitch(1.1f)
                        textToSpeech?.setSpeechRate(speechRateSlower)
                    }
                    else -> {
                        Log.d("TTSManager", "Filipino language set successfully")
                        textToSpeech?.setSpeechRate(speechRateSlower)
                    }
                }
                isInitialized = true
            }
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            // Add pauses between syllables for clearer pronunciation
            val processedText = text.replace(
                "([aeiouAEIOU])", "$1 "
            ).trim()

            Log.d("TTSManager", "Speaking processed text: $processedText")
            textToSpeech?.speak(processedText, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun speakQueued(text: String) {
        if (isInitialized) {
            val processedText = text.replace(
                "([aeiouAEIOU])", "$1 "
            ).trim()

            Log.d("TTSManager", "Queuing processed text: $processedText")
            textToSpeech?.speak(processedText, TextToSpeech.QUEUE_ADD, null, null)
        }
    }

    fun shutdown() {
        Log.d("TTSManager", "Shutting down TTS")
        textToSpeech?.shutdown()
    }

    companion object {
        @Volatile private var INSTANCE: TTSManager? = null

        fun getInstance(context: Context): TTSManager {
            Log.d("TTSManager", "Getting TTSManager instance")
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TTSManager(context).also { INSTANCE = it }
            }
        }
    }
}