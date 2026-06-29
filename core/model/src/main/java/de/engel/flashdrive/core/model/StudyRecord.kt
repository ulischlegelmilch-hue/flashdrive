package de.engel.flashdrive.core.model

/**
 * Records the result of a single flashcard review session.
 */
data class StudyRecord(
    val id: Long = 0,
    val flashcardId: Long,
    val deckId: Long,
    val sessionId: Long,
    val quality: ReviewQuality,
    val responseTimeMs: Long? = null,
    val reviewedAt: Long = System.currentTimeMillis(),
    val intervalDays: Int = 0,
    val easeFactor: Float = 2.5f,
)

/**
 * Quality rating for a flashcard review (based on SM-2 scale).
 * 0 = complete blackout
 * 1 = incorrect, but remembered upon seeing answer
 * 2 = incorrect, but answer seemed easy to recall
 * 3 = correct with difficulty
 * 4 = correct with some hesitation
 * 5 = perfect response
 */
enum class ReviewQuality(val value: Int) {
    BLACKOUT(0),
    INCORRECT_REMEMBERED(1),
    INCORRECT_EASY(2),
    CORRECT_DIFFICULT(3),
    CORRECT_HESITATION(4),
    PERFECT(5)
}
