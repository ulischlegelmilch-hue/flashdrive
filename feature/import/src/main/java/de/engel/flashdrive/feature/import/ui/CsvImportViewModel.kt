package de.engel.flashdrive.feature.import.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.engel.flashdrive.core.data.repository.DeckRepository
import de.engel.flashdrive.core.data.repository.FlashcardRepository
import de.engel.flashdrive.core.model.Flashcard
import de.engel.flashdrive.feature.import.util.CsvParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for the CSV import screen.
 *
 * Handles file reading, column mapping, preview, and persisting parsed cards
 * into the target deck.
 */
@HiltViewModel
class CsvImportViewModel @Inject constructor(
    private val flashcardRepository: FlashcardRepository,
    private val deckRepository: DeckRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CsvImportUiState())
    val uiState: StateFlow<CsvImportUiState> = _uiState.asStateFlow()

    private var rawHeaders: List<String> = emptyList()
    private var rawRows: List<List<String>> = emptyList()

    /**
     * Reads the CSV file content from the given URI and parses it.
     */
    fun loadFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(errorMessage = null) }
                val content = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()
                    ?.readText()
                    ?: throw IllegalStateException("Cannot read file")

                val (headers, rows) = withContext(Dispatchers.IO) {
                    CsvParser.parse(content, delimiter = ';', hasHeader = _uiState.value.hasHeader)
                }

                rawHeaders = headers
                rawRows = rows

                // Auto-detect column mapping from headers.
                val frontCol = headers.indexOfFirst {
                    it.contains("front", ignoreCase = true) ||
                        it.contains("question", ignoreCase = true) ||
                        it.contains("vorderseite", ignoreCase = true)
                }.takeIf { it >= 0 } ?: 0

                val backCol = headers.indexOfFirst {
                    it.contains("back", ignoreCase = true) ||
                        it.contains("answer", ignoreCase = true) ||
                        it.contains("rückseite", ignoreCase = true) ||
                        it.contains("hinten", ignoreCase = true)
                }.takeIf { it >= 0 } ?: if (headers.size >= 2) 1 else 0

                val tagsCol = headers.indexOfFirst {
                    it.contains("tag", ignoreCase = true) ||
                        it.contains("label", ignoreCase = true)
                }

                val parsed = withContext(Dispatchers.IO) {
                    CsvParser.mapToCards(
                        rows = rows,
                        frontColumn = frontCol,
                        backColumn = backCol,
                        tagsColumn = tagsCol,
                    )
                }

                _uiState.update {
                    it.copy(
                        parsedCards = parsed,
                        frontColumn = frontCol,
                        backColumn = backCol,
                        tagsColumn = tagsCol,
                        rawHeaders = headers,
                        errorMessage = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Fehler beim Lesen der Datei: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Updates the column mapping and re-parses the preview.
     */
    fun updateColumnMapping(frontColumn: Int, backColumn: Int, tagsColumn: Int) {
        val parsed = CsvParser.mapToCards(
            rows = rawRows,
            frontColumn = frontColumn,
            backColumn = backColumn,
            tagsColumn = tagsColumn,
        )
        _uiState.update {
            it.copy(
                parsedCards = parsed,
                frontColumn = frontColumn,
                backColumn = backColumn,
                tagsColumn = tagsColumn,
            )
        }
    }

    /**
     * Toggles whether the first row is treated as a header.
     */
    fun toggleHeader(hasHeader: Boolean) {
        viewModelScope.launch {
            try {
                // Re-parse with new header setting.
                // We need to re-read the file, but we can simulate by adjusting rawRows.
                // For simplicity, we just re-run the parse on stored data.
                val allRows = if (hasHeader && rawHeaders.isNotEmpty()) {
                    rawHeaders.map { listOf(it) } + rawRows
                } else {
                    rawRows
                }
                val (newHeaders, newRows) = if (hasHeader && allRows.isNotEmpty()) {
                    Pair(allRows.first(), allRows.drop(1))
                } else {
                    Pair(emptyList(), allRows)
                }

                rawHeaders = newHeaders
                rawRows = newRows

                val parsed = CsvParser.mapToCards(
                    rows = newRows,
                    frontColumn = _uiState.value.frontColumn,
                    backColumn = _uiState.value.backColumn,
                    tagsColumn = _uiState.value.tagsColumn,
                )

                _uiState.update {
                    it.copy(
                        hasHeader = hasHeader,
                        rawHeaders = newHeaders,
                        parsedCards = parsed,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Fehler: ${e.localizedMessage}")
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
                        difficulty = de.engel.flashdrive.core.model.Difficulty.NEW,
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
