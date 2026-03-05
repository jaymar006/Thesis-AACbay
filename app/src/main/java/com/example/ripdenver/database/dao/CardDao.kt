package com.example.ripdenver.database.dao

import androidx.room.*
import com.example.ripdenver.database.entities.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE folderId = :folderId AND isDeleted = 0 ORDER BY `order` ASC")
    fun getCardsByFolder(folderId: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE isDeleted = 0 ORDER BY `order` ASC")
    fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :cardId")
    suspend fun getCardById(cardId: String): CardEntity?

    @Query("SELECT * FROM cards WHERE folderId = '' AND isDeleted = 0 ORDER BY `order` ASC")
    fun getRootCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE isDefault = 1 AND isDeleted = 0")
    fun getDefaultCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE isDefault = 0 AND isDeleted = 0")
    fun getUserCards(): Flow<List<CardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<CardEntity>)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("DELETE FROM cards WHERE id = :cardId")
    suspend fun deleteCardById(cardId: String)

    @Query("DELETE FROM cards WHERE folderId = :folderId")
    suspend fun deleteCardsByFolder(folderId: String)

    @Query("DELETE FROM cards WHERE isDefault = 0")
    suspend fun deleteUserCards()

    @Query("UPDATE cards SET usageCount = usageCount + 1, lastUsed = :timestamp WHERE id = :cardId")
    suspend fun incrementUsageCount(cardId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM cards WHERE isDeleted = 0 AND (label LIKE '%' || :query || '%' OR vocalization LIKE '%' || :query || '%')")
    fun searchCards(query: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE pendingSync = 1")
    suspend fun getPendingCardsForSync(): List<CardEntity>

    @Query("UPDATE cards SET pendingSync = 0 WHERE id = :cardId")
    suspend fun markCardSynced(cardId: String)
}

