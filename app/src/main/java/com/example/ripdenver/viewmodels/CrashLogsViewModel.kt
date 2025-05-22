package com.example.ripdenver.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CrashLogsViewModel @Inject constructor() : ViewModel() {
    // TODO: Implement actual crash log collection
    // For now, we'll use dummy data
    private val dummyCrashLogs = listOf(
        CrashLog(
            timestamp = "2024-03-20 10:30:15",
            type = "NullPointerException",
            message = "Attempt to invoke virtual method 'java.lang.String.toString()' on a null object reference",
            stackTrace = "at com.example.ripdenver.MainActivity.onCreate(MainActivity.kt:45)\n" +
                    "at android.app.Activity.performCreate(Activity.java:1234)\n" +
                    "at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:5678)"
        ),
        CrashLog(
            timestamp = "2024-03-19 15:45:22",
            type = "IllegalStateException",
            message = "Cannot access database on the main thread",
            stackTrace = "at com.example.ripdenver.data.DatabaseManager.query(DatabaseManager.kt:78)\n" +
                    "at com.example.ripdenver.ui.screens.MainScreen.loadData(MainScreen.kt:123)\n" +
                    "at com.example.ripdenver.ui.screens.MainScreen.onCreate(MainScreen.kt:45)"
        )
    )

    fun getCrashLogs(): List<CrashLog> = dummyCrashLogs
}

data class CrashLog(
    val timestamp: String,
    val type: String,
    val message: String,
    val stackTrace: String
) 