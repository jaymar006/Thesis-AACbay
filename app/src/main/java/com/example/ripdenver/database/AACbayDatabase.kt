package com.example.ripdenver.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.ripdenver.database.dao.CardDao
import com.example.ripdenver.database.dao.FolderDao
import com.example.ripdenver.database.dao.NgramDao
import com.example.ripdenver.database.dao.UserSettingsDao
import com.example.ripdenver.database.entities.CardEntity
import com.example.ripdenver.database.entities.FolderEntity
import com.example.ripdenver.database.entities.NgramEntity
import com.example.ripdenver.database.entities.UserSettingsEntity

@Database(
    entities = [
        CardEntity::class,
        FolderEntity::class,
        NgramEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AACbayDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun folderDao(): FolderDao
    abstract fun ngramDao(): NgramDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AACbayDatabase? = null

        fun getDatabase(context: Context): AACbayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AACbayDatabase::class.java,
                    "aacbay_database"
                )
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

