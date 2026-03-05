package com.example.ripdenver.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val userId: String,
    val columnCount: Int,
    val showPredictions: Boolean,
    val allowDataSharing: Boolean,
    val boardImageSize: String,
    val containerImageSize: String,
    val boardTextSize: String,
    val containerTextSize: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

