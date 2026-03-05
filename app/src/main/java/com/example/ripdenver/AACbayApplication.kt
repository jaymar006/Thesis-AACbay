package com.example.ripdenver

import android.app.Application
import android.util.Log
import com.example.ripdenver.services.DatabaseInitializationService
import com.example.ripdenver.utils.CrashHandler
import com.example.ripdenver.utils.TTSManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    
    @Inject
    lateinit var databaseInitializationService: DatabaseInitializationService
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AACbayApplication onCreate")
        
        ttsManager = TTSManager.getInstance(this)
        
        // Initialize database with default content
        databaseInitializationService.initializeDatabase(applicationScope)
        
        // Crash handler will be automatically initialized by Hilt injection
        Log.d(TAG, "Application initialized with crash logging and database")
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "AACbayApplication onTerminate")
        ttsManager.shutdown()
    }
}