package com.example.ripdenver.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class DeveloperViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    init {
        Log.d("DeveloperViewModel", "Initialized")
    }

    // Add developer-specific functionality here
    fun validatePin(pin: String): Boolean {
        Log.d("DeveloperViewModel", "Validating PIN: $pin")
        val isValid = pin == "000000"
        Log.d("DeveloperViewModel", "PIN validation result: $isValid")
        return isValid
    }

    fun resetTutorial() {
        // Reset the tutorial state in SharedPreferences
        context.getSharedPreferences("AACBAY_PREFS", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_first_launch", true)
            .apply()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("DeveloperViewModel", "Cleared")
    }
} 