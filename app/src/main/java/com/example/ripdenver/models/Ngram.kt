package com.example.ripdenver.models

data class Ngram(
    val userId: String = "",         // Links to User
    val sequence: List<String> = emptyList(), // Card IDs in sequence
    val frequency: Int = 1,          // How often this sequence occurs
    val lastUsed: Long = System.currentTimeMillis(),

    // For Firebase querying
    val sequenceHash: String = sequence.joinToString("_")
) {
    // Helper function to update frequency
    fun increment(): Ngram = this.copy(
        frequency = frequency + 1,
        lastUsed = System.currentTimeMillis()
    )
}