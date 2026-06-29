package de.engel.flashdrive.app.ui.screen.decklist

import de.engel.flashdrive.core.model.Deck

/**
 * UI state for the Deck List screen.
 */
data class DeckListUiState(
    val decks: List<Deck> = emptyList(),
    val isLoading: Boolean = true,
    val currentStreak: Int = 0,
)
