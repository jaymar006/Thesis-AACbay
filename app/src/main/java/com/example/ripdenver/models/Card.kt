package com.example.ripdenver.models

data class Card(
    val id: String = "",
    val label: String = "",
    val vocalization: String = "",
    val color: String = "#FF0000",
    val cloudinaryUrl: String = "", // Renamed from imagePath
    val cloudinaryPublicId: String = "",
    val folderId: String = "",
    var usageCount: Int = 0,
    val lastUsed: Long = 0
)