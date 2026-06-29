package de.engel.flashdrive.core.model

/**
 * Represents the synchronization status of data with a remote backend.
 */
data class SyncStatus(
    val id: Long = 0,
    val entityType: EntityType,
    val entityId: Long,
    val state: SyncState,
    val lastSyncedAt: Long? = null,
    val lastModifiedAt: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

/**
 * Type of entity being synced.
 */
enum class EntityType {
    FLASHCARD,
    DECK,
    STUDY_RECORD,
    STUDY_SESSION
}

/**
 * Current synchronization state of an entity.
 */
enum class SyncState {
    SYNCED,
    PENDING_UPLOAD,
    PENDING_DOWNLOAD,
    CONFLICT,
    ERROR
}
