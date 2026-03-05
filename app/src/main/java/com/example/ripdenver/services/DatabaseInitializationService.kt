package com.example.ripdenver.services

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseInitializationService @Inject constructor(
    private val defaultContentService: DefaultContentService
) {
    
    fun initializeDatabase(coroutineScope: CoroutineScope) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                Log.d("DatabaseInit", "Starting database initialization")
                
                // Check if default content is already populated
                val isPopulated = defaultContentService.isDefaultContentPopulated()
                
                if (!isPopulated) {
                    Log.d("DatabaseInit", "Default content not found, populating...")
                    defaultContentService.populateDefaultContent()
                    Log.d("DatabaseInit", "Default content populated successfully")
                } else {
                    Log.d("DatabaseInit", "Default content already exists, skipping population")
                }
                
                Log.d("DatabaseInit", "Database initialization completed")
            } catch (e: Exception) {
                Log.e("DatabaseInit", "Error during database initialization", e)
            }
        }
    }
}

