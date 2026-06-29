package de.engel.flashdrive.feature.import.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import de.engel.flashdrive.feature.import.ui.ParsedCard
import java.io.File
import java.io.FileOutputStream
import java.sql.Connection
import java.sql.DriverManager
import java.util.zip.ZipInputStream

/**
 * Parser for Anki .apkg files.
 *
 * Anki .apkg files are ZIP archives containing:
 *  - media files
 *  - A SQLite database (collection.anki21 or collection.anki2) holding notes, cards, etc.
 *
 * This parser extracts the SQLite database, queries the notes table, and converts
 * each note into a [ParsedCard] using the first two fields (Front/Back).
 */
object AnkiParser {

    private const val ANKI21_DB = "collection.anki21"
    private const val ANKI2_DB = "collection.anki2"
    private const val NOTES_TABLE = "notes"
    private const val COL_FLD = "fld" // fields separated by \u001F (unit separator)
    private const val COL_TAGS = "tags"

    /**
     * Parses an .apkg file from a content URI and extracts parsed cards.
     *
     * @param context Android context for content resolver access.
     * @param uri The URI of the .apkg file (from SAF picker).
     * @return List of parsed cards.
     */
    fun parseApkg(context: Context, uri: Uri): List<ParsedCard> {
        val dbFile = extractDatabase(context, uri) ?: throw IllegalStateException(
            "Could not find Anki database inside .apkg"
        )
        return try {
            parseNotesFromDb(dbFile)
        } finally {
            dbFile.delete()
        }
    }

    /**
     * Parses an .apkg file from a direct file path.
     */
    fun parseApkgFromFile(path: String): List<ParsedCard> {
        val dbFile = extractDatabaseFromFile(path) ?: throw IllegalStateException(
            "Could not find Anki database inside .apkg"
        )
        return try {
            parseNotesFromDb(dbFile)
        } finally {
            dbFile.delete()
        }
    }

    private fun extractDatabase(context: Context, uri: Uri): File? {
        val tempFile = File(context.cacheDir, "anki_import_${System.currentTimeMillis()}.db")
        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val name = entry.name.substringAfterLast("/")
                    if (name == ANKI21_DB || name == ANKI2_DB) {
                        FileOutputStream(tempFile).use { fos ->
                            zis.copyTo(fos)
                        }
                        return tempFile
                    }
                    entry = zis.nextEntry
                }
            }
        }
        return null
    }

    private fun extractDatabaseFromFile(path: String): File? {
        val tempFile = File.createTempFile("anki_import_", ".db")
        ZipInputStream(File(path).inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val name = entry.name.substringAfterLast("/")
                if (name == ANKI21_DB || name == ANKI2_DB) {
                    FileOutputStream(tempFile).use { fos ->
                        zis.copyTo(fos)
                    }
                    return tempFile
                }
                entry = zis.nextEntry
            }
        }
        return null
    }

    private fun parseNotesFromDb(dbFile: File): List<ParsedCard> {
        val connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
        val cards = mutableListOf<ParsedCard>()

        connection.use { conn ->
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery("SELECT $COL_FLD, $COL_TAGS FROM $NOTES_TABLE")
            while (rs.next()) {
                val fld = rs.getString(COL_FLD) ?: continue
                val tags = rs.getString(COL_TAGS) ?: ""

                // Fields in Anki are separated by the unit separator character (0x1F).
                val fields = fld.split("\u001F")
                if (fields.size < 2) continue

                val front = fields[0].trim()
                val back = fields[1].trim()
                if (front.isBlank() || back.isBlank()) continue

                // Strip HTML tags for plain text display.
                val cleanFront = stripHtml(front)
                val cleanBack = stripHtml(back)
                val cleanTags = stripHtml(tags).trim()

                cards.add(ParsedCard(front = cleanFront, back = cleanBack, tags = cleanTags))
            }
            rs.close()
            stmt.close()
        }
        return cards
    }

    private fun stripHtml(input: String): String {
        return input
            .replace(Regex("<[^>]*>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .trim()
    }

    /**
     * Returns the display name of a file from its URI.
     */
    fun getFileName(context: Context, uri: Uri): String {
        var name = "unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex) ?: "unknown"
            }
        }
        return name
    }
}
