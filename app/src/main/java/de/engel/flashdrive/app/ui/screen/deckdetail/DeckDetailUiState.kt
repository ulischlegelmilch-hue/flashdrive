package de.engel.flashdrive.app.ui.screen.deckdetail

import de.engel.flashdrive.core.model.Deck
import de.engel.flashdrive.core.model.Flashcard

/**
 * UI state for the Deck Detail screen.
 */
data class DeckDetailUiState(
    val deck: Deck? = null,
    val cards: List<Flashcard> = emptyList(),
    val dueCardsCount: Int = 0,
    val isLoading: Boolean = true,
)
