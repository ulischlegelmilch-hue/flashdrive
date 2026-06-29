package de.engel.flashdrive.feature.import.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.engel.flashdrive.core.data.repository.DeckRepository
import de.engel.flashdrive.core.data.repository.FlashcardRepository
import de.engel.flashdrive.core.model.Difficulty
import de.engel.flashdrive.core.model.Flashcard
import de.engel.flashdrive.feature.import.util.AnkiParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Immutable UI state for the Anki import flow.
 */
data class AnkiImportUiState(
    val parsedCards: List<ParsedCard> = emptyList(),
    val isImporting: Boolean = false,
    val importResult: String? = null,
    val errorMessage: String? = null,
    val selectedDeckId: Long = -1L,
    val fileName: String = "",
    val isLoading: Boolean = false,
)

/**
 * ViewModel for the Anki .apkg import screen.
 */
@HiltViewModel
class AnkiImportViewModel @Inject constructor(
    private val flashcardRepository: FlashcardRepository,
    private val deckRepository: DeckRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnkiImportUiState())
    val uiState: StateFlow<AnkiImportUiState> = _uiState.asStateFlow()

    /**
     * Loads and parses an .apkg file from the given URI.
     */
    fun loadFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val fileName = AnkiParser.getFileName(context, uri)
                val cards = withContext(Dispatchers.IO) {
                    AnkiParser.parseApkg(context, uri)
                }
                _uiState.update {
                    it.copy(
                        parsedCards = cards,
                        fileName = fileName,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Fehler beim Lesen der Datei: ${e.localizedMessage}",
                    )
                }
            }
        }
    }

    /**
     * Sets the target deck for the import.
     */
    fun setSelectedDeckId(deckId: Long) {
        _uiState.update { it.copy(selectedDeckId = deckId) }
    }

    /**
     * Executes the import: converts parsed cards to Flashcard entities and persists them.
     */
    fun executeImport() {
        val state = _uiState.value
        if (state.selectedDeckId <= 0L) {
            _uiState.update { it.copy(errorMessage = "Bitte ein Deck auswählen.") }
            return
        }
        if (state.parsedCards.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Keine Karten zum Import vorhanden.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, errorMessage = null) }
            try {
                val now = System.currentTimeMillis()
                val cards = state.parsedCards.map { parsed ->
                    Flashcard(
                        deckId = state.selectedDeckId,
                        front = parsed.front,
                        back = parsed.back,
                        hint = if (parsed.tags.isNotBlank()) parsed.tags else null,
                        difficulty = Difficulty.NEW,
                        createdAt = now,
                        updatedAt = now,
                    )
                }
                withContext(Dispatchers.IO) {
                    flashcardRepository.insertCards(cards)
                }

                // Update deck card count.
                val count = withContext(Dispatchers.IO) {
                    flashcardRepository.getCardCountForDeck(state.selectedDeckId)
                }
                val deck = withContext(Dispatchers.IO) {
                    deckRepository.getDeckById(state.selectedDeckId)
                }
                if (deck != null) {
                    withContext(Dispatchers.IO) {
                        deckRepository.updateCardCount(state.selectedDeckId, count)
                    }
                }

                _uiState.update {
                    it.copy(
                        isImporting = false,
                        importResult = "${cards.size} Karten erfolgreich importiert.",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        errorMessage = "Import fehlgeschlagen: ${e.localizedMessage}",
                    )
                }
            }
        }
    }

    /**
     * Clears the import result message.
     */
    fun clearResult() {
        _uiState.update { it.copy(importResult = null) }
    }
}
