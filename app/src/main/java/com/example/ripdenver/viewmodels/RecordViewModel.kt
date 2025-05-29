package com.example.ripdenver.viewmodels

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor() : ViewModel() {
    private val _isRecording = mutableStateOf(false)
    val isRecording: State<Boolean> = _isRecording

    private val _recognizedText = mutableStateOf("")
    val recognizedText: State<String> = _recognizedText

    private val _fontSize = mutableStateOf(18.sp)
    val fontSize: State<TextUnit> = _fontSize

    private val _fontColor = mutableStateOf(Color.Black)
    val fontColor: State<Color> = _fontColor

    private val _fontFamily = mutableStateOf(FontFamily.Default)
    val fontFamily: State<FontFamily> = _fontFamily

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private var isInitialized = false
    private var retryCount = 0
    private val maxRetries = 3

    fun initializeSpeechRecognizer(context: Context) {
        if (!isInitialized) {
            try {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fil-PH")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    // Improved continuous recognition settings
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 0)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                }
                setupRecognitionListener()
                isInitialized = true
                Log.d("RecordViewModel", "Speech recognizer initialized successfully")
            } catch (e: Exception) {
                Log.e("RecordViewModel", "Failed to initialize speech recognizer", e)
            }
        }
    }

    private fun setupRecognitionListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isRecording.value = true
                retryCount = 0
                Log.d("RecordViewModel", "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("RecordViewModel", "Beginning of speech")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Optional: Can be used to show audio level
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d("RecordViewModel", "End of speech")
                if (_isRecording.value) {
                    // Add a small delay before restarting to prevent rapid cycling
                    Thread.sleep(100)
                    startRecording()
                }
            }

            override fun onError(error: Int) {
                Log.e("RecordViewModel", "Recognition error: $error")
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> {
                        if (retryCount < maxRetries) {
                            retryCount++
                            Log.d("RecordViewModel", "Retrying recognition (attempt $retryCount)")
                            if (_isRecording.value) {
                                startRecording()
                            }
                        } else {
                            Log.d("RecordViewModel", "Max retries reached, stopping recognition")
                            stopRecording()
                        }
                    }
                    SpeechRecognizer.ERROR_NETWORK -> {
                        _recognizedText.value += " [Network error]"
                        stopRecording()
                    }
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                        _recognizedText.value += " [Network timeout]"
                        stopRecording()
                    }
                    SpeechRecognizer.ERROR_AUDIO -> {
                        _recognizedText.value += " [Audio error]"
                        stopRecording()
                    }
                    SpeechRecognizer.ERROR_SERVER -> {
                        _recognizedText.value += " [Server error]"
                        stopRecording()
                    }
                    SpeechRecognizer.ERROR_CLIENT -> {
                        _recognizedText.value += " [Client error]"
                        stopRecording()
                    }
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        if (_isRecording.value) {
                            startRecording()
                        }
                    }
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        _recognizedText.value += " [Permission error]"
                        stopRecording()
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val newText = matches[0] ?: ""
                    if (newText.isNotEmpty()) {
                        // Only append if there's actual text
                        _recognizedText.value = if (_recognizedText.value.isEmpty()) {
                            newText
                        } else {
                            "${_recognizedText.value} $newText"
                        }.trim()
                        Log.d("RecordViewModel", "New text added: $newText")
                    }
                }
                
                // Continue listening if still recording
                if (_isRecording.value) {
                    startRecording()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val partialText = matches[0] ?: ""
                    if (partialText.isNotEmpty()) {
                        // Only update if there's actual text
                        _recognizedText.value = if (_recognizedText.value.isEmpty()) {
                            partialText
                        } else {
                            "${_recognizedText.value} $partialText"
                        }.trim()
                        Log.d("RecordViewModel", "Partial text updated: $partialText")
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("RecordViewModel", "Event received: $eventType")
            }
        })
    }

    fun toggleRecording() {
        if (_isRecording.value) {
            stopRecording()
        } else {
            _recognizedText.value = "" // Clear text when starting new recording
            retryCount = 0
            startRecording()
        }
    }

    fun stopRecording() {
        _isRecording.value = false
        try {
            speechRecognizer?.stopListening()
            Log.d("RecordViewModel", "Recording stopped")
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Error stopping recording", e)
        }
    }

    fun startRecording() {
        if (!_isRecording.value) {
            _isRecording.value = true
        }
        try {
            recognizerIntent?.let { intent ->
                speechRecognizer?.startListening(intent)
                Log.d("RecordViewModel", "Recording started")
            }
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Error starting recording", e)
            stopRecording()
        }
    }

    fun setFontSize(size: TextUnit) {
        _fontSize.value = size
    }

    fun setFontColor(color: Color) {
        _fontColor.value = color
    }

    fun setFontFamily(family: FontFamily) {
        _fontFamily.value = when (family) {
            FontFamily.Default -> FontFamily.Default
            FontFamily.Serif -> FontFamily.Serif
            FontFamily.SansSerif -> FontFamily.SansSerif
            FontFamily.Monospace -> FontFamily.Monospace
            else -> FontFamily.Default
        }
    }

    fun clearRecognizedText() {
        _recognizedText.value = ""
    }

    fun setRecognizedText(text: String) {
        _recognizedText.value = text
    }

    override fun onCleared() {
        super.onCleared()
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            isInitialized = false
            Log.d("RecordViewModel", "Speech recognizer destroyed")
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Error destroying speech recognizer", e)
        }
    }
}