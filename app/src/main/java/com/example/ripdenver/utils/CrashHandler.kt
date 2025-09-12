package com.example.ripdenver.utils

import android.content.Context
import android.util.Log
import com.example.ripdenver.viewmodels.CrashLogsViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashHandler @Inject constructor(
    @ApplicationContext private val context: Context
) : Thread.UncaughtExceptionHandler {
    
    companion object {
        private const val TAG = "CrashHandler"
    }
    
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    
    init {
        Log.d(TAG, "CrashHandler initialized")
        Thread.setDefaultUncaughtExceptionHandler(this)
    }
    
    override fun uncaughtException(thread: Thread, exception: Throwable) {
        Log.e(TAG, "Uncaught exception detected", exception)
        
        try {
            // Log the crash to our crash logging system
            val crashLogsViewModel = CrashLogsViewModel()
            crashLogsViewModel.addCrashLog(context, exception)
            Log.d(TAG, "Crash logged successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log crash", e)
        }
        
        // Call the default handler to show the crash dialog
        defaultHandler?.uncaughtException(thread, exception)
    }
}
