package de.engel.flashdrive.core.domain.usecase

import de.engel.flashdrive.core.domain.algorithm.Sm2Algorithm
import de.engel.flashdrive.core.model.Flashcard
import de.engel.flashdrive.core.model.ReviewQuality
import de.engel.flashdrive.core.model.StudyRecord
import javax.inject.Inject

/**
 * Repository interface for persisting card updates and study records.
 * Implemented in the data layer.
 */
interface CardRepository {
    suspend fun updateCard(card: Flashcard)
    suspend fun insertStudyRecord(record: StudyRecord)
}

/**
 * Submits a review for a flashcard: computes the new SM-2 state,
 * updates the card, and records the study event.
 */
class SubmitReviewUseCase @Inject constructor(
    private val repository: CardRepository
) {

    /**
     * Processes a review submission.
     *
     * @param card The flashcard being reviewed.
     * @param quality The quality rating given to the review.
     * @param sessionId The ID of the current study session.
     * @param now Current timestamp in millis.
     * @return The updated flashcard.
     */
    suspend operator fun invoke(
        card: Flashcard,
        quality: ReviewQuality,
        sessionId: Long,
        now: Long = System.currentTimeMillis()
    ): Flashcard {
        val updated = Sm2Algorithm.calculateNextReview(
            grade = quality.value,
            repetitionCount = card.repetitionCount,
            easeFactor = card.easeFactor,
            intervalDays = estimateIntervalDays(card),
            now = now
        )

        val newCard = card.copy(
            repetitionCount = updated.repetitionCount,
            easeFactor = updated.easeFactor,
            nextReviewAt = updated.nextReviewAt,
            difficulty = updated.difficulty,
            lastReviewedAt = now,
            updatedAt = now
        )

        val record = StudyRecord(
            flashcardId = card.id,
            deckId = card.deckId,
            sessionId = sessionId,
            quality = quality,
            reviewedAt = now
        )

        repository.updateCard(newCard)
        repository.insertStudyRecord(record)

        return newCard
    }

    /**
     * Estimates the current interval in days from the card's nextReviewAt and updatedAt.
     * Falls back to 1 day if data is unavailable.
     */
    private fun estimateIntervalDays(card: Flashcard): Int {
        val next = card.nextReviewAt ?: return 1
        val updated = card.updatedAt
        val diffMs = next - updated
        val days = diffMs / (24L * 60L * 60L * 1000L)
        return if (days >= 1) days.toInt() else 1
    }
}
