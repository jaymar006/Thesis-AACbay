package com.example.ripdenver.models

// Add this to your models package or ViewModel
data class ArasaacPictogram(
    val _id: Int,
    val keywords: List<Keyword>,
    // Add other fields you need from the API response
) {
    data class Keyword(
        val keyword: String,
        val meaning: String?,
        val lcase: Boolean
    )

    // Helper function to get image URL
    fun getImageUrl(resolution: Int = 300): String {
        return "https://static.arasaac.org/pictograms/$_id/${_id}_${resolution}.png"
    }
}