package de.engel.flashdrive.feature.study.ui

import de.engel.flashdrive.core.model.Flashcard

/**
 * Immutable UI state for the Study screen.
 *
 * @param cards Remaining flashcards queued for review in this session.
 * @param currentCard The card currently being shown (null when session is finished or loading).
 * @param isFlipped Whether the current card is showing its back side.
 * @param sessionProgress Fraction (0f..1f) of the session completed.
 * @param isFinished Whether the study session has been completed.
 * @param isLoading Whether the initial card load is in progress.
 * @param isSubmitting Whether a grade submission is currently being processed.
 * @param deckName Display name of the deck being studied.
 * @param cardsStudied Number of cards answered so far in this session.
 * @param correctCount Number of cards answered correctly (grade >= 3).
 */
data class StudyUiState(
    val cards: List<Flashcard> = emptyList(),
    val currentCard: Flashcard? = null,
    val isFlipped: Boolean = false,
    val sessionProgress: Float = 0f,
    val isFinished: Boolean = false,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val deckName: String = "",
    val cardsStudied: Int = 0,
    val correctCount: Int = 0,
) {
    /** Number of cards remaining (including the current one). */
    val cardsRemaining: Int get() = cards.size + (if (currentCard != null && !isFinished) 1 else 0)

    /** Incorrect count derived from total studied minus correct. */
    val incorrectCount: Int get() = (cardsStudied - correctCount).coerceAtLeast(0)

    /** Accuracy as a float 0f..1f. */
    val accuracy: Float
        get() = if (cardsStudied > 0) correctCount.toFloat() / cardsStudied else 0f
}
