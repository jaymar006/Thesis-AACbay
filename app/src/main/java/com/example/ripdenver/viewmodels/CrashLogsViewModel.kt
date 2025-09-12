package com.example.ripdenver.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CrashLogsViewModel : ViewModel() {
    
    companion object {
        private const val TAG = "CrashLogsViewModel"
        private const val CRASH_LOG_DIR = "crash_logs"
        private const val MAX_CRASH_LOGS = 50 // Keep only last 50 crash logs
    }
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    init {
        Log.d(TAG, "CrashLogsViewModel initialized")
    }

    fun getCrashLogs(context: Context): List<CrashLog> {
        Log.d(TAG, "Getting crash logs from storage")
        return try {
            val crashLogs = loadCrashLogsFromStorage(context)
            Log.d(TAG, "Loaded ${crashLogs.size} crash logs")
            crashLogs
        } catch (e: Exception) {
            Log.e(TAG, "Error loading crash logs", e)
            emptyList()
        }
    }
    
    private fun loadCrashLogsFromStorage(context: Context): List<CrashLog> {
        val crashLogDir = File(context.filesDir, CRASH_LOG_DIR)
        if (!crashLogDir.exists()) {
            Log.d(TAG, "Crash log directory does not exist, creating it")
            crashLogDir.mkdirs()
            return emptyList()
        }
        
        val crashLogFiles = crashLogDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
        Log.d(TAG, "Found ${crashLogFiles.size} crash log files")
        
        return crashLogFiles.take(MAX_CRASH_LOGS).mapNotNull { file ->
            try {
                val content = file.readText()
                parseCrashLog(content)
            } catch (e: Exception) {
                Log.e(TAG, "Error reading crash log file: ${file.name}", e)
                null
            }
        }
    }
    
    private fun parseCrashLog(content: String): CrashLog? {
        return try {
            val lines = content.split("\n")
            if (lines.size < 4) return null
            
            val timestamp = lines[0].substringAfter("TIMESTAMP: ").trim()
            val type = lines[1].substringAfter("TYPE: ").trim()
            val message = lines[2].substringAfter("MESSAGE: ").trim()
            val stackTrace = lines.drop(3).joinToString("\n")
            
            CrashLog(timestamp, type, message, stackTrace)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing crash log", e)
            null
        }
    }
    
    fun clearCrashLogs(context: Context) {
        Log.d(TAG, "Clearing all crash logs")
        try {
            val crashLogDir = File(context.filesDir, CRASH_LOG_DIR)
            if (crashLogDir.exists()) {
                crashLogDir.listFiles()?.forEach { it.delete() }
                Log.d(TAG, "All crash logs cleared")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing crash logs", e)
        }
    }
    
    fun addCrashLog(context: Context, exception: Throwable) {
        Log.d(TAG, "Adding crash log for exception: ${exception.javaClass.simpleName}")
        try {
            val crashLog = CrashLog(
                timestamp = dateFormat.format(Date()),
                type = exception.javaClass.simpleName,
                message = exception.message ?: "Unknown error",
                stackTrace = Log.getStackTraceString(exception)
            )
            saveCrashLogToStorage(context, crashLog)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding crash log", e)
        }
    }
    
    fun addTestCrashLog(context: Context) {
        Log.d(TAG, "Adding test crash log")
        try {
            val testException = RuntimeException("Test crash log - this is for testing purposes only")
            addCrashLog(context, testException)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding test crash log", e)
        }
    }
    
    private fun saveCrashLogToStorage(context: Context, crashLog: CrashLog) {
        try {
            val crashLogDir = File(context.filesDir, CRASH_LOG_DIR)
            if (!crashLogDir.exists()) {
                crashLogDir.mkdirs()
            }
            
            val fileName = "crash_${System.currentTimeMillis()}.log"
            val file = File(crashLogDir, fileName)
            
            val content = buildString {
                appendLine("TIMESTAMP: ${crashLog.timestamp}")
                appendLine("TYPE: ${crashLog.type}")
                appendLine("MESSAGE: ${crashLog.message}")
                appendLine(crashLog.stackTrace)
            }
            
            file.writeText(content)
            Log.d(TAG, "Crash log saved to: ${file.absolutePath}")
            
            // Clean up old logs if we exceed the limit
            cleanupOldLogs(crashLogDir)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving crash log to storage", e)
        }
    }
    
    private fun cleanupOldLogs(crashLogDir: File) {
        try {
            val files = crashLogDir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
            if (files.size > MAX_CRASH_LOGS) {
                val filesToDelete = files.drop(MAX_CRASH_LOGS)
                filesToDelete.forEach { it.delete() }
                Log.d(TAG, "Cleaned up ${filesToDelete.size} old crash logs")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up old logs", e)
        }
    }
}

data class CrashLog(
    val timestamp: String,
    val type: String,
    val message: String,
    val stackTrace: String
) 