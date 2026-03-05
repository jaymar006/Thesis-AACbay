package com.example.ripdenver.services

import android.util.Log
import com.example.ripdenver.database.entities.UserSettingsEntity
import com.example.ripdenver.models.Card
import com.example.ripdenver.models.Folder
import com.example.ripdenver.models.Ngram
import com.example.ripdenver.repository.LocalDataRepository
import com.example.ripdenver.utils.AuthenticationManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncService @Inject constructor(
    private val localDataRepository: LocalDataRepository
) {

    private val database = Firebase.database.reference

    private data class RemoteCardState(
        val card: Card,
        val updatedAt: Long,
        val isDeleted: Boolean
    )

    private data class RemoteFolderState(
        val folder: Folder,
        val updatedAt: Long,
        val isDeleted: Boolean
    )

    suspend fun syncAllUserData(userId: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            Log.d("DataSyncService", "Starting full sync for user: $userId")

            val cardsSynced = syncCards(userId)
            val foldersSynced = syncFolders(userId)
            val settingsSynced = syncSettings(userId)
            val ngramsSynced = syncNgrams(userId)
            
            val syncResult = SyncResult(
                cardsSynced = cardsSynced,
                foldersSynced = foldersSynced,
                settingsSynced = settingsSynced,
                ngramsSynced = ngramsSynced
            )
            
            Log.d("DataSyncService", "Sync completed: $syncResult")
            syncResult
        } catch (e: Exception) {
            Log.e("DataSyncService", "Error during sync", e)
            SyncResult(isError = true, errorMessage = e.message ?: "Unknown error")
        }
    }

    private suspend fun syncCards(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            var uploadedCount = 0
            var downloadedCount = 0

            // Pull remote state once so we can do proper last-write-wins merging
            val snapshot = database.child("users").child(userId).child("cards").get().await()
            val remoteStates = mutableMapOf<String, RemoteCardState>()

            snapshot.children.forEach { child ->
                val remoteCard = child.getValue(Card::class.java) ?: return@forEach
                val remoteUpdatedAt =
                    child.child("updatedAt").getValue(Long::class.java) ?: 0L
                val remoteIsDeleted =
                    child.child("isDeleted").getValue(Boolean::class.java) ?: false

                remoteStates[remoteCard.id] = RemoteCardState(
                    card = remoteCard,
                    updatedAt = remoteUpdatedAt,
                    isDeleted = remoteIsDeleted
                )
            }

            // 1) Resolve all local pending changes against remote using last-write-wins
            val pendingLocalCards = localDataRepository.getPendingCardsForSync()
            for (entity in pendingLocalCards) {
                val cardRef = database
                    .child("users")
                    .child(userId)
                    .child("cards")
                    .child(entity.id)

                val remoteState = remoteStates.remove(entity.id)
                val localUpdatedAt = entity.updatedAt
                val remoteUpdatedAt = remoteState?.updatedAt ?: 0L
                val remoteIsDeleted = remoteState?.isDeleted ?: false

                if (entity.isDeleted) {
                    // Local says this card is deleted
                    if (localUpdatedAt >= remoteUpdatedAt) {
                        // Local delete is newer → propagate deletion remotely
                        cardRef.removeValue().await()
                        localDataRepository.markCardSynced(entity.id)
                        uploadedCount++
                    } else {
                        // Remote is newer → respect remote state
                        if (remoteState != null) {
                            if (remoteIsDeleted) {
                                // Remote also deleted → hard delete locally
                                localDataRepository.deleteCardById(entity.id)
                            } else {
                                // Remote restored/updated the card → keep remote
                                localDataRepository.updateCard(remoteState.card)
                            }
                            localDataRepository.markCardSynced(entity.id)
                            downloadedCount++
                        } else {
                            // No remote record but remote timestamp newer (edge case) → just mark synced locally
                            localDataRepository.markCardSynced(entity.id)
                        }
                    }
                } else {
                    // Local has a non-deleted card
                    if (remoteState == null || localUpdatedAt > remoteUpdatedAt) {
                        // Local wins → upsert to Firebase with metadata
                        val card = entity.toCard()
                        val payload = mapOf(
                            "id" to card.id,
                            "label" to card.label,
                            "vocalization" to card.vocalization,
                            "color" to card.color,
                            "cloudinaryUrl" to card.cloudinaryUrl,
                            "cloudinaryPublicId" to card.cloudinaryPublicId,
                            "folderId" to card.folderId,
                            "usageCount" to card.usageCount,
                            "lastUsed" to card.lastUsed,
                            "order" to card.order,
                            "updatedAt" to localUpdatedAt,
                            "isDeleted" to false
                        )

                        cardRef.updateChildren(payload).await()
                        localDataRepository.markCardSynced(entity.id)
                        uploadedCount++
                    } else {
                        // Remote wins or is equal and exists
                        if (remoteIsDeleted) {
                            // Remote deleted the card → delete locally
                            localDataRepository.deleteCardById(entity.id)
                        } else {
                            // Remote updated the card → update local
                            localDataRepository.updateCard(remoteState.card)
                        }
                        localDataRepository.markCardSynced(entity.id)
                        downloadedCount++
                    }
                }
            }

            // 2) Handle remaining remote items (not touched by pending local changes)
            remoteStates.values.forEach { remoteState ->
                val localEntity = localDataRepository.getCardEntityById(remoteState.card.id)
                when {
                    localEntity == null && !remoteState.isDeleted -> {
                        // New remote card → insert locally
                        localDataRepository.insertCard(remoteState.card, isDefault = false)
                        downloadedCount++
                    }
                    localEntity != null && !localEntity.pendingSync -> {
                        // Existing local card with no pending changes
                        when {
                            remoteState.isDeleted && !localEntity.isDeleted -> {
                                // Remote deleted → delete locally
                                localDataRepository.deleteCardById(localEntity.id)
                                downloadedCount++
                            }
                            !remoteState.isDeleted && localEntity.updatedAt < remoteState.updatedAt -> {
                                // Remote newer → update local
                                localDataRepository.updateCard(remoteState.card)
                                downloadedCount++
                            }
                        }
                    }
                }
            }

            val total = uploadedCount + downloadedCount
            Log.d(
                "DataSyncService",
                "Synced $total cards (uploaded=$uploadedCount, downloaded=$downloadedCount)"
            )
            total
        } catch (e: Exception) {
            Log.e("DataSyncService", "Error syncing cards", e)
            0
        }
    }

    private suspend fun syncFolders(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            var uploadedCount = 0
            var downloadedCount = 0

            // Pull remote state once so we can do proper last-write-wins merging
            val snapshot = database.child("users").child(userId).child("folders").get().await()
            val remoteStates = mutableMapOf<String, RemoteFolderState>()

            snapshot.children.forEach { child ->
                val remoteFolder = child.getValue(Folder::class.java) ?: return@forEach
                val remoteUpdatedAt =
                    child.child("updatedAt").getValue(Long::class.java) ?: 0L
                val remoteIsDeleted =
                    child.child("isDeleted").getValue(Boolean::class.java) ?: false

                remoteStates[remoteFolder.id] = RemoteFolderState(
                    folder = remoteFolder,
                    updatedAt = remoteUpdatedAt,
                    isDeleted = remoteIsDeleted
                )
            }

            // 1) Resolve all local pending changes against remote using last-write-wins
            val pendingLocalFolders = localDataRepository.getPendingFoldersForSync()
            for (entity in pendingLocalFolders) {
                val folderRef = database
                    .child("users")
                    .child(userId)
                    .child("folders")
                    .child(entity.id)

                val remoteState = remoteStates.remove(entity.id)
                val localUpdatedAt = entity.updatedAt
                val remoteUpdatedAt = remoteState?.updatedAt ?: 0L
                val remoteIsDeleted = remoteState?.isDeleted ?: false

                if (entity.isDeleted) {
                    // Local says this folder is deleted
                    if (localUpdatedAt >= remoteUpdatedAt) {
                        // Local delete is newer → propagate deletion remotely
                        folderRef.removeValue().await()
                        localDataRepository.markFolderSynced(entity.id)
                        uploadedCount++
                    } else {
                        // Remote is newer → respect remote state
                        if (remoteState != null) {
                            if (remoteIsDeleted) {
                                // Remote also deleted → hard delete locally
                                localDataRepository.deleteFolderById(entity.id)
                            } else {
                                // Remote restored/updated the folder → keep remote
                                localDataRepository.updateFolder(remoteState.folder)
                            }
                            localDataRepository.markFolderSynced(entity.id)
                            downloadedCount++
                        } else {
                            // No remote record but remote timestamp newer (edge case) → just mark synced locally
                            localDataRepository.markFolderSynced(entity.id)
                        }
                    }
                } else {
                    // Local has a non-deleted folder
                    if (remoteState == null || localUpdatedAt > remoteUpdatedAt) {
                        // Local wins → upsert to Firebase with metadata
                        val folder = entity.toFolder()
                        val payload = mapOf(
                            "id" to folder.id,
                            "name" to folder.name,
                            "color" to folder.color,
                            "createdAt" to folder.createdAt,
                            "order" to folder.order,
                            "updatedAt" to localUpdatedAt,
                            "isDeleted" to false
                        )

                        folderRef.updateChildren(payload).await()
                        localDataRepository.markFolderSynced(entity.id)
                        uploadedCount++
                    } else {
                        // Remote wins or is equal and exists
                        if (remoteIsDeleted) {
                            // Remote deleted the folder → delete locally
                            localDataRepository.deleteFolderById(entity.id)
                        } else {
                            // Remote updated the folder → update local
                            localDataRepository.updateFolder(remoteState.folder)
                        }
                        localDataRepository.markFolderSynced(entity.id)
                        downloadedCount++
                    }
                }
            }

            // 2) Handle remaining remote items (not touched by pending local changes)
            remoteStates.values.forEach { remoteState ->
                val localEntity = localDataRepository.getFolderEntityById(remoteState.folder.id)
                when {
                    localEntity == null && !remoteState.isDeleted -> {
                        // New remote folder → insert locally
                        localDataRepository.insertFolder(remoteState.folder, isDefault = false)
                        downloadedCount++
                    }
                    localEntity != null && !localEntity.pendingSync -> {
                        // Existing local folder with no pending changes
                        when {
                            remoteState.isDeleted && !localEntity.isDeleted -> {
                                // Remote deleted → delete locally
                                localDataRepository.deleteFolderById(localEntity.id)
                                downloadedCount++
                            }
                            !remoteState.isDeleted && localEntity.updatedAt < remoteState.updatedAt -> {
                                // Remote newer → update local
                                localDataRepository.updateFolder(remoteState.folder)
                                downloadedCount++
                            }
                        }
                    }
                }
            }

            val total = uploadedCount + downloadedCount
            Log.d(
                "DataSyncService",
                "Synced $total folders (uploaded=$uploadedCount, downloaded=$downloadedCount)"
            )
            total
        } catch (e: Exception) {
            Log.e("DataSyncService", "Error syncing folders", e)
            0
        }
    }

    private suspend fun syncSettings(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val snapshot = database.child("users").child(userId).child("settings").get().await()

            if (snapshot.exists()) {
                val settings = UserSettingsEntity(
                    userId = userId,
                    columnCount = snapshot.child("columnCount").getValue(Int::class.java) ?: 6,
                    showPredictions = snapshot.child("showPredictions").getValue(Boolean::class.java) ?: true,
                    allowDataSharing = snapshot.child("allowDataSharing").getValue(Boolean::class.java) ?: false,
                    boardImageSize = snapshot.child("boardImageSize").getValue(String::class.java) ?: "katamtaman",
                    containerImageSize = snapshot.child("containerImageSize").getValue(String::class.java) ?: "katamtaman",
                    boardTextSize = snapshot.child("boardTextSize").getValue(String::class.java) ?: "katamtaman",
                    containerTextSize = snapshot.child("containerTextSize").getValue(String::class.java) ?: "katamtaman"
                )
                
                localDataRepository.insertSettings(settings)
                Log.d("DataSyncService", "Synced settings")
                true
            } else {
                Log.d("DataSyncService", "No settings found for user")
                false
            }
        } catch (e: Exception) {
            Log.e("DataSyncService", "Error syncing settings", e)
            false
        }
    }

    private suspend fun syncNgrams(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            // Load current local ngrams for this user
            val localNgrams = localDataRepository.getNgramsByUser(userId).first()
            val localByHash = localNgrams.associateBy { it.sequenceHash }.toMutableMap()

            // Load remote ngrams from Firebase
            val snapshot = database.child("users").child(userId).child("ngrams").get().await()
            val remoteNgrams = snapshot.children.mapNotNull { it.getValue(Ngram::class.java) }
            val remoteByHash = remoteNgrams.associateBy { it.sequenceHash }.toMutableMap()

            var mergedCount = 0

            // Merge where both local and remote have an entry for the same sequence
            localByHash.forEach { (hash, local) ->
                val remote = remoteByHash.remove(hash)
                if (remote != null) {
                    // Use lastUsed as our "updatedAt" for last-write-wins
                    val winner = if (local.lastUsed >= remote.lastUsed) local else remote
                    localDataRepository.insertNgram(winner)
                    mergedCount++
                }
            }

            // Any remaining remote-only ngrams → insert locally
            remoteByHash.values.forEach { remote ->
                localDataRepository.insertNgram(remote)
                mergedCount++
            }

            Log.d("DataSyncService", "Synced/merged $mergedCount ngrams")
            mergedCount
        } catch (e: Exception) {
            Log.e("DataSyncService", "Error syncing ngrams", e)
            0
        }
    }

    suspend fun isUserDataAvailableOffline(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if we have any user cards or folders (not just default ones)
            val cards = localDataRepository.getAllCards().first()
            val folders = localDataRepository.getAllFolders().first()
            
            // Check if we have user-specific data (not just default content)
            val hasUserCards = cards.any { !isDefaultCard(it) }
            val hasUserFolders = folders.any { !isDefaultFolder(it) }
            val hasSettings = localDataRepository.getSettingsByUser(userId) != null
            
            val hasUserData = hasUserCards || hasUserFolders || hasSettings
            
            Log.d("DataSyncService", "User data available offline: $hasUserData (cards: $hasUserCards, folders: $hasUserFolders, settings: $hasSettings)")
            hasUserData
        } catch (e: Exception) {
            Log.e("DataSyncService", "Error checking offline data availability", e)
            false
        }
    }

    private fun isDefaultCard(card: Card): Boolean {
        // Check if this card matches any default card
        return com.example.ripdenver.utils.DefaultContent.defaultCards.any { defaultCard ->
            defaultCard["label"] == card.label &&
            defaultCard["vocalization"] == card.vocalization &&
            defaultCard["color"] == card.color &&
            defaultCard["folderId"] == card.folderId
        }
    }

    private fun isDefaultFolder(folder: Folder): Boolean {
        // Check if this folder matches any default folder
        return com.example.ripdenver.utils.DefaultContent.defaultFolders.any { defaultFolder ->
            defaultFolder["id"] == folder.id &&
            defaultFolder["name"] == folder.name &&
            defaultFolder["color"] == folder.color
        }
    }

    data class SyncResult(
        val cardsSynced: Int = 0,
        val foldersSynced: Int = 0,
        val settingsSynced: Boolean = false,
        val ngramsSynced: Int = 0,
        val isError: Boolean = false,
        val errorMessage: String? = null
    ) {
        val totalItemsSynced: Int
            get() = cardsSynced + foldersSynced + ngramsSynced + if (settingsSynced) 1 else 0
    }
}
