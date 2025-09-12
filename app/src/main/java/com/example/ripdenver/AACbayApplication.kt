package com.example.ripdenver

import android.app.Application
import android.util.Log
import com.example.ripdenver.utils.CrashHandler
import com.example.ripdenver.utils.TTSManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AACbayApplication : Application() {
    
    companion object {
        private const val TAG = "AACbayApplication"
    }
    
    lateinit var ttsManager: TTSManager
        private set
    
    @Inject
    lateinit var crashHandler: CrashHandler

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AACbayApplication onCreate")
        
        ttsManager = TTSManager.getInstance(this)
        
        // Crash handler will be automatically initialized by Hilt injection
        Log.d(TAG, "Application initialized with crash logging")
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "AACbayApplication onTerminate")
        ttsManager.shutdown()
    }
}