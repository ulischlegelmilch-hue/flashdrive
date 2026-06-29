package de.engel.flashdrive.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_records",
    foreignKeys = [
        ForeignKey(
            entity = FlashcardEntity::class,
            parentColumns = ["card_id"],
            childColumns = ["card_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = StudySessionEntity::class,
            parentColumns = ["session_id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["card_id"]),
        Index(value = ["deck_id"]),
        Index(value = ["session_id"]),
        Index(value = ["reviewed_at"]),
    ],
)
data class StudyRecordEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "record_id")
    val recordId: Long = 0,

    @ColumnInfo(name = "card_id")
    val cardId: Long,

    @ColumnInfo(name = "deck_id")
    val deckId: Long,

    @ColumnInfo(name = "session_id")
    val sessionId: Long,

    @ColumnInfo(name = "grade")
    val grade: Int,

    @ColumnInfo(name = "reviewed_at")
    val reviewedAt: Long,

    @ColumnInfo(name = "duration_ms")
    val durationMs: Long = 0,

    @ColumnInfo(name = "previous_interval")
    val previousInterval: Int = 0,

    @ColumnInfo(name = "new_interval")
    val newInterval: Int = 0,
)
