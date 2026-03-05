package com.example.ripdenver.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ripdenver.models.Ngram

@Entity(tableName = "ngrams")
data class NgramEntity(
    @PrimaryKey
    val id: String, // We'll generate this as a combination of userId and sequenceHash
    val userId: String,
    val sequence: String, // Store as comma-separated string for Room
    val frequency: Int,
    val lastUsed: Long,
    val sequenceHash: String
) {
    fun toNgram(): Ngram {
        return Ngram(
            userId = userId,
            sequence = if (sequence.isEmpty()) emptyList() else sequence.split(","),
            frequency = frequency,
            lastUsed = lastUsed,
            sequenceHash = sequenceHash
        )
    }

    companion object {
        fun fromNgram(ngram: Ngram): NgramEntity {
            return NgramEntity(
                id = "${ngram.userId}_${ngram.sequenceHash}",
                userId = ngram.userId,
                sequence = ngram.sequence.joinToString(","),
                frequency = ngram.frequency,
                lastUsed = ngram.lastUsed,
                sequenceHash = ngram.sequenceHash
            )
        }
    }
}

