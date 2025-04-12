package com.example.ripdenver.models

data class Folder(
    val id: String = "",
    val name: String = "",
    val color: String = "",
    val imagePath: String = "",
    val createdAt: Long = System.currentTimeMillis()
)