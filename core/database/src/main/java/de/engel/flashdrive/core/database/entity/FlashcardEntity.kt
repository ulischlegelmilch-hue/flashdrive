package de.engel.flashdrive.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["deck_id"],
            childColumns = ["deck_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["deck_id"]),
        Index(value = ["due_date"]),
    ],
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "card_id")
    val cardId: Long = 0,

    @ColumnInfo(name = "deck_id")
    val deckId: Long,

    @ColumnInfo(name = "front_content")
    val frontContent: String,

    @ColumnInfo(name = "back_content")
    val backContent: String,

    @ColumnInfo(name = "front_media_uri")
    val frontMediaUri: String? = null,

    @ColumnInfo(name = "back_media_uri")
    val backMediaUri: String? = null,

    @ColumnInfo(name = "difficulty")
    val difficulty: Int = 0,

    @ColumnInfo(name = "interval_days")
    val intervalDays: Int = 0,

    @ColumnInfo(name = "ease_factor")
    val easeFactor: Double = 2.5,

    @ColumnInfo(name = "repetitions")
    val repetitions: Int = 0,

    @ColumnInfo(name = "due_date")
    val dueDate: Long,

    @ColumnInfo(name = "last_reviewed_at")
    val lastReviewedAt: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "tags")
    val tags: String? = null,
)
