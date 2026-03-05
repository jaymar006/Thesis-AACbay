package com.example.ripdenver.repository

import com.example.ripdenver.database.dao.CardDao
import com.example.ripdenver.database.dao.FolderDao
import com.example.ripdenver.database.dao.NgramDao
import com.example.ripdenver.database.dao.UserSettingsDao
import com.example.ripdenver.database.entities.CardEntity
import com.example.ripdenver.database.entities.FolderEntity
import com.example.ripdenver.database.entities.NgramEntity
import com.example.ripdenver.database.entities.UserSettingsEntity
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.models.Ngram
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataRepository @Inject constructor(
    private val cardDao: CardDao,
    private val folderDao: FolderDao,
    private val ngramDao: NgramDao,
    private val userSettingsDao: UserSettingsDao
) {
    
    // Card operations
    fun getAllCards(): Flow<List<Card>> = cardDao.getAllCards().map { entities ->
        entities.map { it.toCard() }
    }

    fun getCardsByFolder(folderId: String): Flow<List<Card>> = cardDao.getCardsByFolder(folderId).map { entities ->
        entities.map { it.toCard() }
    }

    fun getRootCards(): Flow<List<Card>> = cardDao.getRootCards().map { entities ->
        entities.map { it.toCard() }
    }

    suspend fun getCardById(cardId: String): Card? = cardDao.getCardById(cardId)?.toCard()

    suspend fun getCardEntityById(cardId: String): CardEntity? = cardDao.getCardById(cardId)

    suspend fun insertCard(card: Card, isDefault: Boolean = false) {
        cardDao.insertCard(CardEntity.fromCard(card, isDefault))
    }

    suspend fun insertCards(cards: List<Card>, isDefault: Boolean = false) {
        cardDao.insertCards(cards.map { CardEntity.fromCard(it, isDefault) })
    }

    suspend fun updateCard(card: Card) {
        cardDao.updateCard(CardEntity.fromCard(card))
    }

    suspend fun deleteCard(card: Card) {
        cardDao.deleteCard(CardEntity.fromCard(card))
    }

    suspend fun deleteCardById(cardId: String) {
        cardDao.deleteCardById(cardId)
    }

    suspend fun deleteCardsByFolder(folderId: String) {
        cardDao.deleteCardsByFolder(folderId)
    }

    suspend fun incrementUsageCount(cardId: String) {
        cardDao.incrementUsageCount(cardId)
    }

    fun searchCards(query: String): Flow<List<Card>> = cardDao.searchCards(query).map { entities ->
        entities.map { it.toCard() }
    }

    suspend fun getPendingCardsForSync(): List<CardEntity> = cardDao.getPendingCardsForSync()

    suspend fun markCardSynced(cardId: String) {
        cardDao.markCardSynced(cardId)
    }

    /**
     * Insert or update a user card locally and mark it as pending sync.
     * This is used for local-first mutations; remote sync is handled by DataSyncService.
     */
    suspend fun upsertUserCard(card: Card) {
        val existing = cardDao.getCardById(card.id)
        val now = System.currentTimeMillis()

        val entity = if (existing != null) {
            existing.copy(
                label = card.label,
                vocalization = card.vocalization,
                color = card.color,
                cloudinaryUrl = card.cloudinaryUrl,
                cloudinaryPublicId = card.cloudinaryPublicId,
                folderId = card.folderId,
                usageCount = card.usageCount,
                lastUsed = card.lastUsed,
                order = card.order,
                updatedAt = now,
                isDeleted = false,
                pendingSync = true
            )
        } else {
            CardEntity.fromCard(card, isDefault = false).copy(
                updatedAt = now,
                isDeleted = false,
                pendingSync = true
            )
        }

        cardDao.insertCard(entity)
    }

    /**
     * Soft-delete a card locally and mark it as pending sync.
     */
    suspend fun softDeleteCard(cardId: String) {
        val existing = cardDao.getCardById(cardId) ?: return
        val now = System.currentTimeMillis()

        val entity = existing.copy(
            updatedAt = now,
            isDeleted = true,
            pendingSync = true
        )

        cardDao.updateCard(entity)
    }

    /**
     * Update the order of a card locally and mark it as pending sync.
     */
    suspend fun updateCardOrder(cardId: String, order: Int) {
        val existing = cardDao.getCardById(cardId) ?: return
        val now = System.currentTimeMillis()

        val entity = existing.copy(
            order = order,
            updatedAt = now,
            pendingSync = true
        )

        cardDao.updateCard(entity)
    }

    // Folder operations
    fun getAllFolders(): Flow<List<Folder>> = folderDao.getAllFolders().map { entities ->
        entities.map { it.toFolder() }
    }

    suspend fun getFolderById(folderId: String): Folder? = folderDao.getFolderById(folderId)?.toFolder()

    suspend fun getFolderEntityById(folderId: String): FolderEntity? = folderDao.getFolderById(folderId)

    suspend fun insertFolder(folder: Folder, isDefault: Boolean = false) {
        folderDao.insertFolder(FolderEntity.fromFolder(folder, isDefault))
    }

    suspend fun insertFolders(folders: List<Folder>, isDefault: Boolean = false) {
        folderDao.insertFolders(folders.map { FolderEntity.fromFolder(it, isDefault) })
    }

    suspend fun updateFolder(folder: Folder) {
        folderDao.updateFolder(FolderEntity.fromFolder(folder))
    }

    suspend fun deleteFolder(folder: Folder) {
        folderDao.deleteFolder(FolderEntity.fromFolder(folder))
    }

    suspend fun deleteFolderById(folderId: String) {
        folderDao.deleteFolderById(folderId)
    }

    fun searchFolders(query: String): Flow<List<Folder>> = folderDao.searchFolders(query).map { entities ->
        entities.map { it.toFolder() }
    }

    suspend fun getPendingFoldersForSync(): List<FolderEntity> = folderDao.getPendingFoldersForSync()

    suspend fun markFolderSynced(folderId: String) {
        folderDao.markFolderSynced(folderId)
    }

    /**
     * Insert or update a user folder locally and mark it as pending sync.
     */
    suspend fun upsertUserFolder(folder: Folder) {
        val existing = folderDao.getFolderById(folder.id)
        val now = System.currentTimeMillis()

        val entity = if (existing != null) {
            existing.copy(
                name = folder.name,
                color = folder.color,
                order = folder.order,
                updatedAt = now,
                isDeleted = false,
                pendingSync = true
            )
        } else {
            FolderEntity.fromFolder(folder, isDefault = false).copy(
                updatedAt = now,
                isDeleted = false,
                pendingSync = true
            )
        }

        folderDao.insertFolder(entity)
    }

    /**
     * Soft-delete a folder locally and mark it as pending sync.
     */
    suspend fun softDeleteFolder(folderId: String) {
        val existing = folderDao.getFolderById(folderId) ?: return
        val now = System.currentTimeMillis()

        val entity = existing.copy(
            updatedAt = now,
            isDeleted = true,
            pendingSync = true
        )

        folderDao.updateFolder(entity)
    }

    /**
     * Update the order of a folder locally and mark it as pending sync.
     */
    suspend fun updateFolderOrder(folderId: String, order: Int) {
        val existing = folderDao.getFolderById(folderId) ?: return
        val now = System.currentTimeMillis()

        val entity = existing.copy(
            order = order,
            updatedAt = now,
            pendingSync = true
        )

        folderDao.updateFolder(entity)
    }

    // Ngram operations
    fun getNgramsByUser(userId: String): Flow<List<Ngram>> = ngramDao.getNgramsByUser(userId).map { entities ->
        entities.map { it.toNgram() }
    }

    suspend fun getNgramBySequence(userId: String, sequenceHash: String): Ngram? = 
        ngramDao.getNgramBySequence(userId, sequenceHash)?.toNgram()

    fun getTopNgrams(userId: String, limit: Int = 10): Flow<List<Ngram>> = 
        ngramDao.getTopNgrams(userId, limit).map { entities ->
            entities.map { it.toNgram() }
        }

    suspend fun insertNgram(ngram: Ngram) {
        ngramDao.insertNgram(NgramEntity.fromNgram(ngram))
    }

    suspend fun insertNgrams(ngrams: List<Ngram>) {
        ngramDao.insertNgrams(ngrams.map { NgramEntity.fromNgram(it) })
    }

    suspend fun updateNgram(ngram: Ngram) {
        ngramDao.updateNgram(NgramEntity.fromNgram(ngram))
    }

    suspend fun deleteNgram(ngram: Ngram) {
        ngramDao.deleteNgram(NgramEntity.fromNgram(ngram))
    }

    suspend fun deleteNgramsByUser(userId: String) {
        ngramDao.deleteNgramsByUser(userId)
    }

    // User Settings operations
    suspend fun getSettingsByUser(userId: String): UserSettingsEntity? = 
        userSettingsDao.getSettingsByUser(userId)

    fun getSettingsByUserFlow(userId: String): Flow<UserSettingsEntity?> = 
        userSettingsDao.getSettingsByUserFlow(userId)

    suspend fun insertSettings(settings: UserSettingsEntity) {
        userSettingsDao.insertSettings(settings)
    }

    suspend fun updateSettings(settings: UserSettingsEntity) {
        userSettingsDao.updateSettings(settings)
    }

    suspend fun deleteSettingsByUser(userId: String) {
        userSettingsDao.deleteSettingsByUser(userId)
    }

    // Methods to delete only user data (not default data)
    suspend fun deleteUserCards() {
        cardDao.deleteUserCards()
    }

    suspend fun deleteUserFolders() {
        folderDao.deleteUserFolders()
    }
}
