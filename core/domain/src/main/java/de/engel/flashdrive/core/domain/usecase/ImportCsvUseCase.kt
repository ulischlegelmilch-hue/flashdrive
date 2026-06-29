package de.engel.flashdrive.core.domain.usecase

import de.engel.flashdrive.core.model.Flashcard
import java.util.UUID

/**
 * Parses CSV content into a list of Flashcards.
 *
 * Supported formats (semicolon-separated, quoted fields):
 *   "front";"back"
 *   "front";"back";"tags"
 *
 * Lines starting with '#' or empty lines are skipped.
 */
class ImportCsvUseCase {

    /**
     * Parses a CSV string into flashcards.
     *
     * @param csv The raw CSV content.
     * @param deckId The deck ID to assign to all imported cards.
     * @param now Current timestamp in millis for createdAt/updatedAt.
     * @return List of parsed Flashcards.
     */
    operator fun invoke(
        csv: String,
        deckId: Long,
        now: Long = System.currentTimeMillis()
    ): List<Flashcard> {
        return csv.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .mapNotNull { line -> parseLine(line, deckId, now) }
            .toList()
    }

    private fun parseLine(line: String, deckId: Long, now: Long): Flashcard? {
        val fields = parseFields(line)
        if (fields.size < 2) return null

        val front = fields[0].trim()
        val back = fields[1].trim()

        if (front.isEmpty() || back.isEmpty()) return null

        return Flashcard(
            id = generateId(),
            deckId = deckId,
            front = front,
            back = back,
            hint = null,
            difficulty = de.engel.flashdrive.core.model.Difficulty.NEW,
            lastReviewedAt = null,
            nextReviewAt = null,
            repetitionCount = 0,
            easeFactor = 2.5f,
            createdAt = now,
            updatedAt = now
        )
    }

    /**
     * Parses a semicolon-separated line with quoted fields.
     * Example: "hello";"world" -> ["hello", "world"]
     */
    private fun parseFields(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' && !inQuotes -> inQuotes = true
                ch == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++ // skip escaped quote
                    } else {
                        inQuotes = false
                    }
                }
                ch == ';' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
            i++
        }
        result.add(current.toString())
        return result
    }

    private fun generateId(): Long {
        return UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
    }
}
