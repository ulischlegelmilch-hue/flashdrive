package de.engel.flashdrive.feature.import.util

import com.github.doyaaaaaken.kotlincsv.client.CsvReader
import com.github.doyaaaaaken.kotlincsv.dsl.CsvReaderDsl
import de.engel.flashdrive.feature.import.ui.ParsedCard

/**
 * Robust CSV parser using the kotlin-csv library.
 *
 * Supports:
 *  - Configurable delimiter (default ;)
 *  - Quoted fields with escaped quotes ("")
 *  - Header detection
 *  - Comment lines starting with #
 *  - Empty line skipping
 */
object CsvParser {

    /**
     * Parses raw CSV content into rows of string fields.
     *
     * @param content The raw CSV string.
     * @param delimiter The column separator (default ';').
     * @param hasHeader Whether the first non-empty row is a header.
     * @return A pair of (headers, data rows). Headers may be empty if hasHeader is false.
     */
    fun parse(
        content: String,
        delimiter: Char = ';',
        hasHeader: Boolean = true,
    ): Pair<List<String>, List<List<String>>> {
        if (content.isBlank()) return Pair(emptyList(), emptyList())

        val allRows: List<List<String>> = CsvReader {
            this.delimiter = delimiter
            this.escapeChar = '"'
            this.quoteChar = '"'
            this.skipEmptyLine = true
            this.autoRenameDuplicateHeaders = true
        }.readAll(content)

        // Filter out comment lines.
        val cleanRows = allRows.filter { row ->
            row.isNotEmpty() && row.any { it.isNotBlank() } &&
                row.first().trim().let { !it.startsWith("#") }
        }

        if (cleanRows.isEmpty()) return Pair(emptyList(), emptyList())

        return if (hasHeader) {
            val headers = cleanRows.first()
            val data = cleanRows.drop(1)
            Pair(headers, data)
        } else {
            Pair(emptyList(), cleanRows)
        }
    }

    /**
     * Maps raw rows to [ParsedCard] instances using column indices.
     *
     * @param rows Data rows (without header).
     * @param frontColumn Index of the front column.
     * @param backColumn Index of the back column.
     * @param tagsColumn Index of the tags column (-1 to ignore).
     * @return List of valid parsed cards.
     */
    fun mapToCards(
        rows: List<List<String>>,
        frontColumn: Int,
        backColumn: Int,
        tagsColumn: Int = -1,
    ): List<ParsedCard> {
        return rows.mapNotNull { row ->
            val front = row.getOrNull(frontColumn)?.trim() ?: return@mapNotNull null
            val back = row.getOrNull(backColumn)?.trim() ?: return@mapNotNull null
            if (front.isBlank() || back.isBlank()) return@mapNotNull null

            val tags = if (tagsColumn >= 0) (row.getOrNull(tagsColumn)?.trim() ?: "") else ""
            ParsedCard(front = front, back = back, tags = tags)
        }
    }
}
