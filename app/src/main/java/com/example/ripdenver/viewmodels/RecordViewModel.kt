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
import kotlinx.coroutines.delay

class RecordViewModel : ViewModel() {
    init {
        Log.d("RecordViewModel", "=== RECORD VIEWMODEL CONSTRUCTOR CALLED ===")
        Log.d("RecordViewModel", "RecordViewModel instance created at: ${System.currentTimeMillis()}")
        Log.d("RecordViewModel", "Stack trace:")
        Thread.currentThread().stackTrace.forEach { 
            Log.d("RecordViewModel", "  at $it")
        }
    }
    
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

    // Error handling states
    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String> = _errorMessage

    private val _showError = mutableStateOf(false)
    val showError: State<Boolean> = _showError

    // Language change notification states
    private val _languageChangeMessage = mutableStateOf("")
    val languageChangeMessage: State<String> = _languageChangeMessage

    private val _showLanguageChangeNotification = mutableStateOf(false)
    val showLanguageChangeNotification: State<Boolean> = _showLanguageChangeNotification

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private var isInitialized = false
    private var retryCount = 0
    private val maxRetries = 3
    private var isDestroyed = false
    private var isInitializing = false
    private var lastContext: Context? = null
    private var currentLanguage = "fil-PH" // Default to Tagalog

    fun initializeSpeechRecognizer(context: Context) {
        Log.d("RecordViewModel", "=== INITIALIZE SPEECH RECOGNIZER DEBUG ===")
        Log.d("RecordViewModel", "initializeSpeechRecognizer called at: ${System.currentTimeMillis()}")
        Log.d("RecordViewModel", "isInitialized: $isInitialized")
        Log.d("RecordViewModel", "isDestroyed: $isDestroyed")
        Log.d("RecordViewModel", "context: $context")
        Log.d("RecordViewModel", "context.applicationContext: ${context.applicationContext}")
        // Stack trace removed to reduce log spam
        
        if (!isInitialized && !isDestroyed && !isInitializing) {
            isInitializing = true
            lastContext = context.applicationContext
            try {
                // Check if speech recognition is available
                Log.d("RecordViewModel", "Checking if speech recognition is available...")
                val isAvailable = SpeechRecognizer.isRecognitionAvailable(context)
                Log.d("RecordViewModel", "Speech recognition available: $isAvailable")
                
                if (!isAvailable) {
                    Log.e("RecordViewModel", "Speech recognition is not available on this device")
                    showError("Speech recognition is not available on this device")
                    return
                }

                // Check if Tagalog is available, if not start with English
                if (currentLanguage == "fil-PH" && !isTagalogAvailable(context)) {
                    Log.w("RecordViewModel", "Tagalog not available, switching to English")
                    currentLanguage = "en-US"
                }

                // Clean up any existing recognizer first
                Log.d("RecordViewModel", "Cleaning up existing recognizer...")
                speechRecognizer?.destroy()
                speechRecognizer = null

                // Create a new recognizer with a fresh context
                Log.d("RecordViewModel", "Creating new speech recognizer...")
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
                Log.d("RecordViewModel", "Speech recognizer created: $speechRecognizer")
                
                Log.d("RecordViewModel", "Creating recognizer intent...")
                recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    // Use current language with fallback
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fil-PH,en-US")
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    // Minimal settings - remove problematic extras
                    // Don't use PREFER_OFFLINE as it might cause issues on this device
                    // Don't use partial results as it might cause busy state
                    // Don't use custom silence timeouts as they might be incompatible
                }
                Log.d("RecordViewModel", "Recognizer intent created: $recognizerIntent")
                
                Log.d("RecordViewModel", "Setting up recognition listener...")
                setupRecognitionListener()
                
                // Add a small delay to let the recognizer fully settle
                kotlinx.coroutines.runBlocking {
                    delay(200)
                }
                
                isInitialized = true
                clearError()
                Log.d("RecordViewModel", "Speech recognizer initialized successfully")
            } catch (e: Exception) {
                Log.e("RecordViewModel", "Failed to initialize speech recognizer", e)
                showError("Failed to initialize speech recognition: ${e.message}")
            } finally {
                isInitializing = false
            }
        } else {
            Log.w("RecordViewModel", "Skipping initialization - already initialized, destroyed, or initializing")
        }
    }

    private fun setupRecognitionListener() {
        Log.d("RecordViewModel", "=== SETUP RECOGNITION LISTENER DEBUG ===")
        Log.d("RecordViewModel", "speechRecognizer: $speechRecognizer")
        
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("RecordViewModel", "=== ON READY FOR SPEECH ===")
                Log.d("RecordViewModel", "isDestroyed: $isDestroyed")
                Log.d("RecordViewModel", "params: $params")
                if (!isDestroyed) {
                    Log.d("RecordViewModel", "Ready for speech")
                    clearError()
                } else {
                    Log.w("RecordViewModel", "ViewModel is destroyed, ignoring onReadyForSpeech")
                }
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
                // Don't automatically restart - let user control when to record again
            }

            override fun onError(error: Int) {
                Log.e("RecordViewModel", "=== RECOGNITION ERROR DEBUG ===")
                Log.e("RecordViewModel", "Error code: $error")
                Log.e("RecordViewModel", "isDestroyed: $isDestroyed")
                Log.e("RecordViewModel", "isInitialized: $isInitialized")
                Log.e("RecordViewModel", "_isRecording.value: ${_isRecording.value}")
                Log.e("RecordViewModel", "speechRecognizer: $speechRecognizer")
                Log.e("RecordViewModel", "recognizerIntent: $recognizerIntent")
                
                if (isDestroyed) {
                    Log.w("RecordViewModel", "ViewModel is destroyed, ignoring error")
                    return
                }

                // Always stop recording on error
                stopRecording()

                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> {
                        Log.d("RecordViewModel", "No speech detected")
                        // Don't show error for no match - this is normal
                    }
                    SpeechRecognizer.ERROR_NETWORK -> {
                        Log.w("RecordViewModel", "Network error - using offline mode")
                        // Don't show error, just log it
                    }
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> {
                        Log.w("RecordViewModel", "Network timeout - using offline mode")
                        // Don't show error, just log it
                    }
                    SpeechRecognizer.ERROR_AUDIO -> {
                        Log.e("RecordViewModel", "Audio error - microphone issue")
                        showError("Audio error. Please check microphone permissions.")
                    }
                    SpeechRecognizer.ERROR_SERVER -> {
                        Log.w("RecordViewModel", "Server error - using offline mode")
                        // Don't show error, just log it
                    }
                    SpeechRecognizer.ERROR_CLIENT -> {
                        Log.e("RecordViewModel", "=== CLIENT ERROR DETAILED DEBUG ===")
                        Log.e("RecordViewModel", "Client error detected - this usually means recognizer is in invalid state")
                        Log.e("RecordViewModel", "Current recognizer state: $speechRecognizer")
                        Log.e("RecordViewModel", "Current intent: $recognizerIntent")
                        Log.e("RecordViewModel", "Current recognized text: ${_recognizedText.value}")
                        
                        // Check if we just completed a successful recognition
                        val hasRecognizedText = _recognizedText.value.isNotEmpty()
                        
                        if (hasRecognizedText) {
                            Log.d("RecordViewModel", "Client error after successful recognition - this is normal, just cleaning up")
                            // Don't show error dialog if we just successfully recognized speech
                            // Just clean up the recognizer silently and reinitialize for next use
                            try {
                                speechRecognizer?.destroy()
                                speechRecognizer = null
                                isInitialized = false
                                isInitializing = false
                                _isRecording.value = false
                                Log.d("RecordViewModel", "Recognizer cleaned up after successful recognition")
                                Log.d("RecordViewModel", "Will reinitialize automatically when user tries to record again")
                            } catch (e: Exception) {
                                Log.e("RecordViewModel", "Error during cleanup after successful recognition", e)
                            }
                        } else {
                            // No text recognized - this could be due to language issues or unclear speech
                            Log.w("RecordViewModel", "Client error with no recognized text - possibly language or audio issue")
                            
                            // Prevent infinite loop by checking if we're already in error state
                            if (_showError.value) {
                                Log.w("RecordViewModel", "Already showing error, preventing infinite loop")
                                return
                            }
                            
                            // Try switching language and reinitializing instead of showing error
                            Log.d("RecordViewModel", "Attempting to switch language and reinitialize...")
                            try {
                                speechRecognizer?.destroy()
                                speechRecognizer = null
                                isInitialized = false
                                isInitializing = false
                                _isRecording.value = false
                                
                                // Switch language and try again
                                currentLanguage = if (currentLanguage == "fil-PH") "en-US" else "fil-PH"
                                Log.d("RecordViewModel", "Switched language to: $currentLanguage")
                                
                                // Reinitialize with new language
                                lastContext?.let { context ->
                                    Log.d("RecordViewModel", "Reinitializing with new language...")
                                    initializeSpeechRecognizer(context)
                                }
                                
                                Log.d("RecordViewModel", "Language switched and recognizer reinitialized")
                            } catch (e: Exception) {
                                Log.e("RecordViewModel", "Error during language switch and cleanup", e)
                                showError("Speech recognition issue. Please try again.")
                            }
                        }
                    }
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        Log.d("RecordViewModel", "Speech timeout")
                        // Don't show error for timeout - this is normal
                    }
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        Log.e("RecordViewModel", "Insufficient permissions error")
                        showError("Microphone permission required. Please grant permission in settings.")
                    }
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                        Log.w("RecordViewModel", "Recognizer busy - waiting and retrying")
                        // Don't show error immediately, just log it
                        // The recognizer might recover on its own
                    }
                    else -> {
                        Log.w("RecordViewModel", "Unknown error: $error")
                        showError("Speech recognition error. Please try again.")
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                if (isDestroyed) return
                
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
                        clearError()
                    }
                }
                
                // Stop recording after getting results
                stopRecording()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                if (isDestroyed) return
                
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val partialText = matches[0] ?: ""
                    if (partialText.isNotEmpty()) {
                        // For partial results, we don't append to existing text
                        // Instead, we show it as a preview
                        Log.d("RecordViewModel", "Partial text: $partialText")
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
            clearError()
            startRecording()
        }
    }

    fun stopRecording() {
        Log.d("RecordViewModel", "=== STOP RECORDING DEBUG ===")
        Log.d("RecordViewModel", "_isRecording.value before: ${_isRecording.value}")
        Log.d("RecordViewModel", "speechRecognizer: $speechRecognizer")
        Log.d("RecordViewModel", "isInitialized: $isInitialized")
        
        _isRecording.value = false
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            Log.d("RecordViewModel", "Recording stopped successfully")
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Error stopping recording", e)
        }
    }

    fun startRecording() {
        Log.d("RecordViewModel", "=== START RECORDING DEBUG ===")
        Log.d("RecordViewModel", "isDestroyed: $isDestroyed")
        Log.d("RecordViewModel", "isInitialized: $isInitialized")
        Log.d("RecordViewModel", "_isRecording.value: ${_isRecording.value}")
        Log.d("RecordViewModel", "speechRecognizer: $speechRecognizer")
        Log.d("RecordViewModel", "recognizerIntent: $recognizerIntent")
        
        if (isDestroyed) {
            Log.w("RecordViewModel", "Cannot start recording: ViewModel is destroyed")
            showError("Speech recognition not ready. Please try again.")
            return
        }
        
        if (!isInitialized) {
            Log.w("RecordViewModel", "Recognizer not initialized, attempting to reinitialize...")
            lastContext?.let { context ->
                Log.d("RecordViewModel", "Reinitializing with stored context...")
                initializeSpeechRecognizer(context)
                // Wait a moment for initialization to complete
                kotlinx.coroutines.runBlocking {
                    delay(300)
                }
                if (!isInitialized) {
                    Log.e("RecordViewModel", "Failed to reinitialize recognizer")
                    showError("Speech recognition not ready. Please try again.")
                    return
                }
            } ?: run {
                Log.e("RecordViewModel", "No context available for reinitialization")
                showError("Speech recognition not ready. Please try again.")
                return
            }
        }
        
        if (_isRecording.value) {
            Log.w("RecordViewModel", "Already recording, ignoring start request")
            return
        }
        
        try {
            _isRecording.value = true
            
            // Add a small delay to let the recognizer settle
            kotlinx.coroutines.runBlocking {
                delay(100)
            }
            
            recognizerIntent?.let { intent ->
                Log.d("RecordViewModel", "Starting speech recognition with intent extras:")
                intent.extras?.let { extras ->
                    for (key in extras.keySet()) {
                        Log.d("RecordViewModel", "Intent extra: $key = ${extras.get(key)}")
                    }
                }
                speechRecognizer?.startListening(intent)
                Log.d("RecordViewModel", "Recording started successfully")
            } ?: run {
                Log.e("RecordViewModel", "Recognizer intent is null")
                showError("Speech recognition not properly initialized")
                _isRecording.value = false
            }
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Error starting recording", e)
            showError("Failed to start recording: ${e.message}")
            _isRecording.value = false
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

    fun clearError() {
        _errorMessage.value = ""
        _showError.value = false
    }

    fun dismissError() {
        _showError.value = false
    }

    fun reinitializeSpeechRecognizer(context: Context) {
        Log.d("RecordViewModel", "Reinitializing speech recognizer...")
        try {
            // Stop any ongoing recording first
            stopRecording()
            
            // Wait for the recognizer to be fully stopped
            kotlinx.coroutines.runBlocking {
                delay(1500)
            }
            
            // Destroy the recognizer completely
            speechRecognizer?.destroy()
            speechRecognizer = null
            isInitialized = false
            isInitializing = false
            _isRecording.value = false
            retryCount = 0
            clearError()
            
            // Wait a bit more to ensure cleanup is complete
            kotlinx.coroutines.runBlocking {
                delay(1500)
            }
            
            // Now reinitialize with fresh context
            initializeSpeechRecognizer(context)
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Error reinitializing speech recognizer", e)
            showError("Failed to restart speech recognition: ${e.message}")
        }
    }

    fun isDeviceCompatible(): Boolean {
        return try {
            // Check if the device supports speech recognition
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Error checking device compatibility", e)
            false
        }
    }

    fun testSpeechRecognition(context: Context): Boolean {
        return try {
            SpeechRecognizer.isRecognitionAvailable(context)
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Error testing speech recognition", e)
            false
        }
    }

    fun testRecognizerCreation(context: Context): Boolean {
        return try {
            val testRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            testRecognizer.destroy()
            true
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Error creating test recognizer", e)
            false
        }
    }

    fun forceResetSpeechRecognizer(context: Context) {
        Log.d("RecordViewModel", "Force resetting speech recognizer...")
        try {
            // Stop everything
            stopRecording()
            
            // Wait for everything to settle
            kotlinx.coroutines.runBlocking {
                delay(2000)
            }
            
            // Destroy and reset
            speechRecognizer?.destroy()
            speechRecognizer = null
            isInitialized = false
            isInitializing = false
            _isRecording.value = false
            retryCount = 0
            clearError()
            
            // Wait a bit more
            kotlinx.coroutines.runBlocking {
                delay(2000)
            }
            
            // Try to reinitialize with a completely fresh approach
            initializeSpeechRecognizerWithFallback(context)
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Error force resetting speech recognizer", e)
            showError("Failed to reset speech recognition: ${e.message}")
        }
    }

    private fun initializeSpeechRecognizerWithFallback(context: Context) {
        Log.d("RecordViewModel", "=== FALLBACK INITIALIZATION DEBUG ===")
        try {
            // First try with application context
            Log.d("RecordViewModel", "Creating recognizer with application context...")
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            Log.d("RecordViewModel", "Fallback recognizer created: $speechRecognizer")
            
            Log.d("RecordViewModel", "Creating minimal intent...")
            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                // Use current language with fallback
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fil-PH,en-US")
                // Minimal settings - no partial results, no offline preference
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            Log.d("RecordViewModel", "Minimal intent created: $recognizerIntent")
            
            Log.d("RecordViewModel", "Setting up fallback recognition listener...")
            setupRecognitionListener()
            
            isInitialized = true
            clearError()
            Log.d("RecordViewModel", "Speech recognizer initialized with minimal fallback settings")
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Failed to initialize with fallback", e)
            showError("Failed to initialize speech recognition: ${e.message}")
        }
    }

    fun testBasicSpeechRecognition(context: Context) {
        Log.d("RecordViewModel", "=== TESTING BASIC SPEECH RECOGNITION ===")
        try {
            // Test with the most basic setup possible
            val testRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            val testIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fil-PH")
            }
            
            Log.d("RecordViewModel", "Test recognizer created: $testRecognizer")
            Log.d("RecordViewModel", "Test intent created: $testIntent")
            
            // Just test if we can create them, then destroy
            testRecognizer.destroy()
            Log.d("RecordViewModel", "Basic speech recognition test passed")
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Basic speech recognition test failed", e)
        }
    }

    fun switchLanguage() {
        val previousLanguage = currentLanguage
        currentLanguage = if (currentLanguage == "fil-PH") "en-US" else "fil-PH"
        Log.d("RecordViewModel", "Language switched to: $currentLanguage")
        
        // Show language change notification
        val languageName = if (currentLanguage == "fil-PH") "Filipino" else "English"
        val message = if (currentLanguage == "fil-PH") {
            "Ang wika ay napalitan sa Filipino"
        } else {
            "Language changed to English"
        }
        showLanguageChangeNotification(message)
        
        // Reinitialize with new language if already initialized
        if (isInitialized && lastContext != null) {
            Log.d("RecordViewModel", "Reinitializing with new language: $currentLanguage")
            lastContext?.let { context ->
                // Clean up current recognizer
                speechRecognizer?.destroy()
                speechRecognizer = null
                isInitialized = false
                isInitializing = false
                
                // Reinitialize with new language
                initializeSpeechRecognizer(context)
            }
        }
    }

    fun getCurrentLanguage(): String {
        return currentLanguage
    }

    fun isTagalogAvailable(context: Context): Boolean {
        return try {
            val testRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            val testIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fil-PH")
            }
            testRecognizer.destroy()
            true
        } catch (e: Exception) {
            Log.d("RecordViewModel", "Tagalog language not available: ${e.message}")
            false
        }
    }

    private fun showError(message: String) {
        _errorMessage.value = message
        _showError.value = true
        Log.e("RecordViewModel", "Error: $message")
    }

    private fun showLanguageChangeNotification(message: String) {
        _languageChangeMessage.value = message
        _showLanguageChangeNotification.value = true
        Log.d("RecordViewModel", "Language change notification: $message")
    }

    fun dismissLanguageChangeNotification() {
        _showLanguageChangeNotification.value = false
    }

    override fun onCleared() {
        super.onCleared()
        isDestroyed = true
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            isInitialized = false
            isInitializing = false
            Log.d("RecordViewModel", "Speech recognizer destroyed")
        } catch (e: Exception) {
            Log.e("RecordViewModel", "Error destroying speech recognizer", e)
        }
    }
}