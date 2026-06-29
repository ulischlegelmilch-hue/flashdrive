package de.engel.flashdrive.core.domain.usecase

import de.engel.flashdrive.core.model.Difficulty
import de.engel.flashdrive.core.model.Flashcard

/**
 * Statistics for a single deck.
 */
data class DeckStats(
    val deckId: Long,
    val deckName: String,
    val totalCards: Int,
    val dueCards: Int,
    val masteredCards: Int,
    val avgEaseFactor: Float
)

/**
 * Computes statistics for each deck based on its flashcards.
 */
class GetDeckStatsUseCase {

    /**
     * Calculates per-deck statistics.
     *
     * @param cards All flashcards.
     * @param deckNames Map of deckId -> deck name.
     * @param now Current time in millis for determining due cards.
     * @return List of DeckStats, one per deck that has at least one card.
     */
    operator fun invoke(
        cards: List<Flashcard>,
        deckNames: Map<Long, String>,
        now: Long = System.currentTimeMillis()
    ): List<DeckStats> {
        val grouped = cards.groupBy { it.deckId }

        return grouped.map { (deckId, deckCards) ->
            val total = deckCards.size
            val due = deckCards.count { val t = it.nextReviewAt; t == null || t <= now }
            val mastered = deckCards.count { it.difficulty == Difficulty.MASTERED }
            val avgEf = if (total > 0) deckCards.map { it.easeFactor }.average().toFloat() else 0f

            DeckStats(
                deckId = deckId,
                deckName = deckNames[deckId] ?: "",
                totalCards = total,
                dueCards = due,
                masteredCards = mastered,
                avgEaseFactor = avgEf
            )
        }
    }
}
