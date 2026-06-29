package de.engel.flashdrive.core.domain.usecase

import de.engel.flashdrive.core.model.Deck
import de.engel.flashdrive.core.model.Flashcard

/**
 * Retrieves all flashcards that are due for review (nextReviewAt <= now),
 * grouped by their parent deck and sorted alphabetically by deck name.
 *
 * Model differences note: Flashcard uses [Flashcard.nextReviewAt] for scheduling,
 * [Flashcard.deckId] for deck association, and Deck uses [Deck.name] for title.
 */
class GetDueCardsUseCase {

    /**
     * Groups due flashcards by their deck.
     *
     * @param cards All flashcards in the system.
     * @param decks All decks (used for ordering and naming).
     * @param now Current time in millis. Cards with nextReviewAt <= now are due.
     *            Cards with nextReviewAt == null are treated as new and always due.
     * @return List of pairs (Deck, List<Flashcard>) sorted alphabetically by deck name.
     */
    operator fun invoke(
        cards: List<Flashcard>,
        decks: List<Deck>,
        now: Long = System.currentTimeMillis()
    ): List<Pair<Deck, List<Flashcard>>> {
        val cardsByDeck = cards.groupBy { it.deckId }

        return decks
            .sortedBy { it.name.lowercase() }
            .mapNotNull { deck ->
                val dueCards = cardsByDeck[deck.id]
                    ?.filter { card ->
                        val t = card.nextReviewAt
                        t == null || t <= now
                    }
                    .orEmpty()

                if (dueCards.isNotEmpty()) {
                    deck to dueCards
                } else {
                    null
                }
            }
    }
}
