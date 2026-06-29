package de.engel.flashdrive.core.model

/**
 * Represents a complete study session for a specific deck.
 */
data class StudySession(
    val id: Long = 0,
    val deckId: Long,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val cardsStudied: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val totalTimeMs: Long = 0,
    val isCompleted: Boolean = false
) {
    /**
     * Accuracy percentage for this session (0.0 to 1.0).
     */
    val accuracy: Float
        get() = if (cardsStudied > 0) correctCount.toFloat() / cardsStudied else 0f
}
