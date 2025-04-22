package com.example.ripdenver

import android.app.Application
import com.example.ripdenver.utils.TTSManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AACbayApplication : Application() {
    lateinit var ttsManager: TTSManager
        private set

    override fun onCreate() {
        super.onCreate()
        ttsManager = TTSManager.getInstance(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        ttsManager.shutdown()
    }
}