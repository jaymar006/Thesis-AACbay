package com.example.ripdenver.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeveloperViewModel @Inject constructor() : ViewModel() {
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

    override fun onCleared() {
        super.onCleared()
        Log.d("DeveloperViewModel", "Cleared")
    }
} 