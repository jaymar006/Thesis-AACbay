package com.example.ripdenver.database.dao

import androidx.room.*
import com.example.ripdenver.database.entities.NgramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NgramDao {
    @Query("SELECT * FROM ngrams WHERE userId = :userId ORDER BY frequency DESC, lastUsed DESC")
    fun getNgramsByUser(userId: String): Flow<List<NgramEntity>>

    @Query("SELECT * FROM ngrams WHERE userId = :userId AND sequenceHash = :sequenceHash")
    suspend fun getNgramBySequence(userId: String, sequenceHash: String): NgramEntity?

    @Query("SELECT * FROM ngrams WHERE userId = :userId ORDER BY frequency DESC LIMIT :limit")
    fun getTopNgrams(userId: String, limit: Int = 10): Flow<List<NgramEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNgram(ngram: NgramEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNgrams(ngrams: List<NgramEntity>)

    @Update
    suspend fun updateNgram(ngram: NgramEntity)

    @Delete
    suspend fun deleteNgram(ngram: NgramEntity)

    @Query("DELETE FROM ngrams WHERE userId = :userId")
    suspend fun deleteNgramsByUser(userId: String)

    @Query("DELETE FROM ngrams WHERE userId = :userId AND sequenceHash = :sequenceHash")
    suspend fun deleteNgramBySequence(userId: String, sequenceHash: String)
}

