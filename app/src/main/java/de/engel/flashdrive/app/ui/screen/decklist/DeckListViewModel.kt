package de.engel.flashdrive.app.ui.screen.decklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.engel.flashdrive.core.data.repository.DeckRepository
import de.engel.flashdrive.core.model.Deck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Deck List screen.
 * Observes all decks from [DeckRepository] and exposes them as [DeckListUiState].
 */
@HiltViewModel
class DeckListViewModel @Inject constructor(
    private val deckRepository: DeckRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeckListUiState())
    val uiState: StateFlow<DeckListUiState> = _uiState.asStateFlow()

    init {
        observeDecks()
    }

    private fun observeDecks() {
        viewModelScope.launch {
            deckRepository.getAllDecks().collect { decks ->
                _uiState.update { currentState ->
                    currentState.copy(
                        decks = decks,
                        isLoading = false,
                    )
                }
            }
        }
    }

    /**
     * Creates a new deck with the given name.
     */
    fun createDeck(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            val deck = Deck(
                name = name.trim(),
            )
            deckRepository.insertDeck(deck)
        }
    }

    /**
     * Deletes the deck with the given ID.
     */
    fun deleteDeck(deckId: Long) {
        viewModelScope.launch {
            deckRepository.deleteDeckById(deckId)
        }
    }
}
