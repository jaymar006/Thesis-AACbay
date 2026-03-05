package com.example.ripdenver.di

import android.content.Context
import com.example.ripdenver.database.AACbayDatabase
import com.example.ripdenver.database.dao.CardDao
import com.example.ripdenver.database.dao.FolderDao
import com.example.ripdenver.database.dao.NgramDao
import com.example.ripdenver.database.dao.UserSettingsDao
import com.example.ripdenver.repository.LocalDataRepository
import com.example.ripdenver.services.DataSyncService
import com.example.ripdenver.services.DefaultContentService
import com.example.ripdenver.utils.ConnectivityObserver
import com.example.ripdenver.utils.NetworkConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAACbayDatabase(@ApplicationContext context: Context): AACbayDatabase {
        return AACbayDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideCardDao(database: AACbayDatabase): CardDao {
        return database.cardDao()
    }

    @Provides
    fun provideFolderDao(database: AACbayDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    fun provideNgramDao(database: AACbayDatabase): NgramDao {
        return database.ngramDao()
    }

    @Provides
    fun provideUserSettingsDao(database: AACbayDatabase): UserSettingsDao {
        return database.userSettingsDao()
    }

    @Provides
    @Singleton
    fun provideLocalDataRepository(
        cardDao: CardDao,
        folderDao: FolderDao,
        ngramDao: NgramDao,
        userSettingsDao: UserSettingsDao
    ): LocalDataRepository {
        return LocalDataRepository(cardDao, folderDao, ngramDao, userSettingsDao)
    }

    @Provides
    @Singleton
    fun provideDataSyncService(
        localDataRepository: LocalDataRepository
    ): DataSyncService {
        return DataSyncService(localDataRepository)
    }

    @Provides
    @Singleton
    fun provideDefaultContentService(
        localDataRepository: LocalDataRepository
    ): DefaultContentService {
        return DefaultContentService(localDataRepository)
    }

    @Provides
    @Singleton
    fun provideConnectivityObserver(
        impl: NetworkConnectivityObserver
    ): ConnectivityObserver = impl
}
