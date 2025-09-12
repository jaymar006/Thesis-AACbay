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
        val currentPassword = getCurrentDeveloperPassword()
        val isValid = pin == currentPassword
        Log.d("DeveloperViewModel", "PIN validation result: $isValid (current password: $currentPassword)")
        return isValid
    }

    fun resetTutorial() {
        // Reset the tutorial state in SharedPreferences
        context.getSharedPreferences("AACBAY_PREFS", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_first_launch", true)
            .apply()
    }

    fun changeDeveloperPassword(newPassword: String) {
        Log.d("DeveloperViewModel", "Changing developer password")
        context.getSharedPreferences("AACBAY_PREFS", Context.MODE_PRIVATE)
            .edit()
            .putString("developer_password", newPassword)
            .apply()
        Log.d("DeveloperViewModel", "Developer password changed successfully")
    }

    fun getCurrentDeveloperPassword(): String {
        return context.getSharedPreferences("AACBAY_PREFS", Context.MODE_PRIVATE)
            .getString("developer_password", "000000") ?: "000000"
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("DeveloperViewModel", "Cleared")
    }
} 