package de.engel.flashdrive.feature.import.ui

/**
 * Immutable UI state for the CSV import flow.
 *
 * @param parsedCards List of flashcards parsed from the CSV file (preview).
 * @param frontColumn Index of the column mapped to the card front (-1 if unset).
 * @param backColumn Index of the column mapped to the card back (-1 if unset).
 * @param tagsColumn Index of the column mapped to tags (-1 if unused).
 * @param hasHeader Whether the first row is a header row.
 * @param isImporting Whether an import operation is currently running.
 * @param importResult Result message shown after import completes.
 * @param errorMessage Error message if parsing or import fails.
 * @param selectedDeckId Target deck for the import.
 * @param rawHeaders Column headers detected from the CSV (empty if no header).
 */
data class CsvImportUiState(
    val parsedCards: List<ParsedCard> = emptyList(),
    val frontColumn: Int = -1,
    val backColumn: Int = -1,
    val tagsColumn: Int = -1,
    val hasHeader: Boolean = true,
    val isImporting: Boolean = false,
    val importResult: String? = null,
    val errorMessage: String? = null,
    val selectedDeckId: Long = -1L,
    val rawHeaders: List<String> = emptyList(),
)

/**
 * Represents a single parsed card before it is persisted.
 */
data class ParsedCard(
    val front: String,
    val back: String,
    val tags: String = "",
)
