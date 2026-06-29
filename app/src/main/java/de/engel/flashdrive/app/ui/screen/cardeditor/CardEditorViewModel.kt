package de.engel.flashdrive.app.ui.screen.cardeditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.engel.flashdrive.core.data.repository.FlashcardRepository
import de.engel.flashdrive.core.model.Flashcard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Card Editor screen.
 * Loads an existing card (if cardId provided) or prepares a new card form.
 * Handles save (insert or update) and delete operations.
 */
@HiltViewModel
class CardEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val flashcardRepository: FlashcardRepository,
) : ViewModel() {

    private val deckId: Long = savedStateHandle["deckId"] ?: -1L
    private val cardId: Long? = (savedStateHandle["cardId"] as? Long)?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(CardEditorUiState(cardId = cardId, deckId = deckId))
    val uiState: StateFlow<CardEditorUiState> = _uiState.asStateFlow()

    init {
        if (cardId != null) {
            loadCard(cardId)
        }
    }

    private fun loadCard(id: Long) {
        _uiState.update { it.copy(isLoading = true, isNewCard = false) }
        viewModelScope.launch {
            val card = flashcardRepository.getCardById(id)
            if (card != null) {
                _uiState.update {
                    it.copy(
                        front = card.front,
                        back = card.back,
                        hint = card.hint.orEmpty(),
                        isLoading = false,
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateFront(value: String) {
        _uiState.update { it.copy(front = value, frontError = null) }
    }

    fun updateBack(value: String) {
        _uiState.update { it.copy(back = value, backError = null) }
    }

    fun updateHint(value: String) {
        _uiState.update { it.copy(hint = value) }
    }

    fun save(onDone: () -> Unit) {
        val state = _uiState.value
        if (state.deckId < 0) {
            _uiState.update {
                it.copy(frontError = "Ungültiges Deck")
            }
            return
        }

        val front = state.front.trim()
        val back = state.back.trim()

        var hasError = false
        if (front.isBlank()) {
            _uiState.update { it.copy(frontError = "Vorderseite darf nicht leer sein") }
            hasError = true
        }
        if (back.isBlank()) {
            _uiState.update { it.copy(backError = "Rückseite darf nicht leer sein") }
            hasError = true
        }
        if (hasError) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val currentCardId = state.cardId

            if (currentCardId != null && currentCardId > 0) {
                // Update existing card
                val existing = flashcardRepository.getCardById(currentCardId)
                if (existing != null) {
                    val updated = existing.copy(
                        front = front,
                        back = back,
                        hint = state.hint.trim().ifEmpty { null },
                        updatedAt = now
                    )
                    flashcardRepository.updateCard(updated)
                }
            } else {
                // Insert new card
                val newCard = Flashcard(
                    deckId = state.deckId,
                    front = front,
                    back = back,
                    hint = state.hint.trim().ifEmpty { null },
                    createdAt = now,
                    updatedAt = now,
                )
                flashcardRepository.insertCard(newCard)
            }
            _uiState.update { it.copy(isSaving = false) }
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = cardId ?: return
        viewModelScope.launch {
            flashcardRepository.deleteCardById(id)
            onDone()
        }
    }
}
