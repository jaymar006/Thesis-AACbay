package com.example.ripdenver.database.dao

import androidx.room.*
import com.example.ripdenver.database.entities.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE userId = :userId")
    suspend fun getSettingsByUser(userId: String): UserSettingsEntity?

    @Query("SELECT * FROM user_settings WHERE userId = :userId")
    fun getSettingsByUserFlow(userId: String): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettingsEntity)

    @Update
    suspend fun updateSettings(settings: UserSettingsEntity)

    @Delete
    suspend fun deleteSettings(settings: UserSettingsEntity)

    @Query("DELETE FROM user_settings WHERE userId = :userId")
    suspend fun deleteSettingsByUser(userId: String)
}

