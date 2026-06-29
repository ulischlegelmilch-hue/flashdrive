package de.engel.flashdrive.app.ui.screen.deckdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.engel.flashdrive.core.data.repository.DeckRepository
import de.engel.flashdrive.core.data.repository.FlashcardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Deck Detail screen.
 * Observes a single deck and its flashcards from the repositories.
 */
@HiltViewModel
class DeckDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val deckRepository: DeckRepository,
    private val flashcardRepository: FlashcardRepository,
) : ViewModel() {

    private val deckId: Long = savedStateHandle["deckId"] ?: -1L

    private val _uiState = MutableStateFlow(DeckDetailUiState())
    val uiState: StateFlow<DeckDetailUiState> = _uiState.asStateFlow()

    init {
        observeDeckAndCards()
    }

    private fun observeDeckAndCards() {
        viewModelScope.launch {
            combine(
                deckRepository.observeDeckById(deckId),
                flashcardRepository.getCardsByDeck(deckId),
            ) { deck, cards ->
                DeckDetailUiState(
                    deck = deck,
                    cards = cards,
                    dueCardsCount = cards.count { card ->
                        card.nextReviewAt != null && card.nextReviewAt <= System.currentTimeMillis()
                    },
                    isLoading = false,
                )
            }.collect { state ->
                _uiState.update { state }
            }
        }
    }

    /**
     * Deletes a flashcard by its ID.
     */
    fun deleteCard(cardId: Long) {
        viewModelScope.launch {
            flashcardRepository.deleteCardById(cardId)
        }
    }
}
