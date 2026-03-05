package com.example.ripdenver.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ripdenver.models.Card

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey
    val id: String,
    val label: String,
    val vocalization: String,
    val color: String,
    val cloudinaryUrl: String,
    val cloudinaryPublicId: String,
    val folderId: String,
    val usageCount: Int,
    val lastUsed: Long,
    val order: Int,
    val isDefault: Boolean = false, // Track if this is a default card
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val pendingSync: Boolean = false
) {
    fun toCard(): Card {
        return Card(
            id = id,
            label = label,
            vocalization = vocalization,
            color = color,
            cloudinaryUrl = cloudinaryUrl,
            cloudinaryPublicId = cloudinaryPublicId,
            folderId = folderId,
            usageCount = usageCount,
            lastUsed = lastUsed,
            order = order
        )
    }

    companion object {
        fun fromCard(card: Card, isDefault: Boolean = false): CardEntity {
            return CardEntity(
                id = card.id,
                label = card.label,
                vocalization = card.vocalization,
                color = card.color,
                cloudinaryUrl = card.cloudinaryUrl,
                cloudinaryPublicId = card.cloudinaryPublicId,
                folderId = card.folderId,
                usageCount = card.usageCount,
                lastUsed = card.lastUsed,
                order = card.order,
                isDefault = isDefault
            )
        }
    }
}

