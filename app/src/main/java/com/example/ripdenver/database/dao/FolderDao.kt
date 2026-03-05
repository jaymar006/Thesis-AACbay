package com.example.ripdenver.database.dao

import androidx.room.*
import com.example.ripdenver.database.entities.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE isDeleted = 0 ORDER BY `order` ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE isDefault = 1 AND isDeleted = 0")
    fun getDefaultFolders(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE isDefault = 0 AND isDeleted = 0")
    fun getUserFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: String)

    @Query("DELETE FROM folders WHERE isDefault = 0")
    suspend fun deleteUserFolders()

    @Query("SELECT * FROM folders WHERE isDeleted = 0 AND name LIKE '%' || :query || '%'")
    fun searchFolders(query: String): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE pendingSync = 1")
    suspend fun getPendingFoldersForSync(): List<FolderEntity>

    @Query("UPDATE folders SET pendingSync = 0 WHERE id = :folderId")
    suspend fun markFolderSynced(folderId: String)
}

