package com.example.ripdenver.models

data class Card(
    val id: String = "",
    val label: String = "",
    val vocalization: String = "",
    val color: String = "#FF0000",
    val imagePath: String = "", // Stores Firebase Storage path
    val folderId: String = "",
    var usageCount: Int = 0,
    val lastUsed: Long = 0
)