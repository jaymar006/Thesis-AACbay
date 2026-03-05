package com.example.ripdenver.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ripdenver.models.Folder

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val color: String,
    val createdAt: Long,
    val order: Int,
    val isDefault: Boolean = false, // Track if this is a default folder
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val pendingSync: Boolean = false
) {
    fun toFolder(): Folder {
        return Folder(
            id = id,
            name = name,
            color = color,
            createdAt = createdAt,
            order = order
        )
    }

    companion object {
        fun fromFolder(folder: Folder, isDefault: Boolean = false): FolderEntity {
            return FolderEntity(
                id = folder.id,
                name = folder.name,
                color = folder.color,
                createdAt = folder.createdAt,
                order = folder.order,
                isDefault = isDefault
            )
        }
    }
}

