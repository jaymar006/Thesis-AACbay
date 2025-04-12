package com.example.ripdenver.models

data class User(
    val userId: String = "",        // Firebase-generated UID
    val name: String = "",          // User's display name
    val createdAt: Long = System.currentTimeMillis(),
    val lastLogin: Long = System.currentTimeMillis(),
    val settings: UserSettings = UserSettings() // Nested settings
) {
    // Nested settings class
    data class UserSettings(
        val ttsEnabled: Boolean = true,
        val ttsSpeed: Float = 1.0f,  // 0.5x - 2.0x
        val uiScale: Float = 1.0f,   // For accessibility
        val primaryColor: String = "#6200EE"
    )
}