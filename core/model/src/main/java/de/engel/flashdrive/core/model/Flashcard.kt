package de.engel.flashdrive.core.model

/**
 * Represents a single flashcard with front and back content.
 */
data class Flashcard(
    val id: Long = 0,
    val deckId: Long,
    val front: String,
    val back: String,
    val hint: String? = null,
    val difficulty: Difficulty = Difficulty.NEW,
    val lastReviewedAt: Long? = null,
    val nextReviewAt: Long? = null,
    val repetitionCount: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Float = 2.5f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Difficulty level of a flashcard based on spaced repetition state.
 */
enum class Difficulty {
    NEW,
    LEARNING,
    REVIEW,
    MASTERED
}
