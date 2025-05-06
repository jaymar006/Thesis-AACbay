package com.example.ripdenver.viewmodels

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun initializeSpeechRecognizer(context: Context) {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fil-PH")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                // Add these flags for continuous recognition
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 0)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            }
            setupRecognitionListener()
        }
    }

    fun setupRecognitionListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isRecording.value = true
                // Clear existing text when starting new recording
                _recognizedText.value = ""
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                // Don't set recording to false here to allow continuous recording
                startRecording() // Restart listening immediately
            }

            override fun onError(error: Int) {
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> {
                        // Restart listening if no match is found
                        if (_isRecording.value) {
                            startRecording()
                        }
                    }
                    SpeechRecognizer.ERROR_NETWORK -> _recognizedText.value = "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> _recognizedText.value = "Network timeout"
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val newText = matches[0] ?: ""
                    // Append new text with a space
                    _recognizedText.value = "${_recognizedText.value} $newText".trim()
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
                    _recognizedText.value = "${_recognizedText.value} $partialText".trim()
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }
    fun toggleRecording() {
        if (_isRecording.value) {
            stopRecording()
        } else {
            _recognizedText.value = "" // Clear text when starting new recording
            startRecording()
        }
    }

    fun stopRecording() {
        _isRecording.value = false
        speechRecognizer?.stopListening()
    }

    fun startRecording() {
        if (!_isRecording.value) {
            _isRecording.value = true
        }
        recognizerIntent?.let { intent ->
            speechRecognizer?.startListening(intent)
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
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}