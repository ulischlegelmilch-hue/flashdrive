package de.engel.flashdrive.core.database.mapper

import de.engel.flashdrive.core.database.entity.DeckEntity
import de.engel.flashdrive.core.database.entity.FlashcardEntity
import de.engel.flashdrive.core.database.entity.StudyRecordEntity
import de.engel.flashdrive.core.database.entity.StudySessionEntity

// ── Deck mappers ────────────────────────────────────────────────────────────

fun DeckEntity.toMap(): Map<String, Any?> = mapOf(
    "deck_id" to deckId,
    "name" to name,
    "description" to description,
    "color" to color,
    "icon" to icon,
    "created_at" to createdAt,
    "updated_at" to updatedAt,
    "last_studied_at" to lastStudiedAt,
    "card_count" to cardCount,
    "is_archived" to isArchived,
)

// ── Flashcard mappers ────────────────────────────────────────────────────────

fun FlashcardEntity.toMap(): Map<String, Any?> = mapOf(
    "card_id" to cardId,
    "deck_id" to deckId,
    "front_content" to frontContent,
    "back_content" to backContent,
    "front_media_uri" to frontMediaUri,
    "back_media_uri" to backMediaUri,
    "difficulty" to difficulty,
    "interval_days" to intervalDays,
    "ease_factor" to easeFactor,
    "repetitions" to repetitions,
    "due_date" to dueDate,
    "last_reviewed_at" to lastReviewedAt,
    "created_at" to createdAt,
    "updated_at" to updatedAt,
    "tags" to tags,
)

// ── StudySession mappers ─────────────────────────────────────────────────────

fun StudySessionEntity.toMap(): Map<String, Any?> = mapOf(
    "session_id" to sessionId,
    "deck_id" to deckId,
    "started_at" to startedAt,
    "ended_at" to endedAt,
    "cards_reviewed" to cardsReviewed,
    "mode" to mode,
)

// ── StudyRecord mappers ──────────────────────────────────────────────────────

fun StudyRecordEntity.toMap(): Map<String, Any?> = mapOf(
    "record_id" to recordId,
    "card_id" to cardId,
    "session_id" to sessionId,
    "grade" to grade,
    "reviewed_at" to reviewedAt,
    "duration_ms" to durationMs,
    "previous_interval" to previousInterval,
    "new_interval" to newInterval,
)
