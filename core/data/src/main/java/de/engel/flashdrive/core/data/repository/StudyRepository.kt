package de.engel.flashdrive.core.data.repository

import de.engel.flashdrive.core.model.StudyRecord
import de.engel.flashdrive.core.model.StudySession
import kotlinx.coroutines.flow.Flow

interface StudyRepository {

    // ── StudyRecord operations ─────────────────────────────────────────────

    suspend fun insertRecord(record: StudyRecord): Long

    suspend fun insertRecords(records: List<StudyRecord>)

    suspend fun getRecordById(recordId: Long): StudyRecord?

    fun getRecordsForSession(sessionId: Long): Flow<List<StudyRecord>>

    fun getRecordsForCard(cardId: Long): Flow<List<StudyRecord>>

    fun getRecordsInRange(startTime: Long, endTime: Long): Flow<List<StudyRecord>>

    suspend fun getAverageGradeForCard(cardId: Long): Double?

    suspend fun getRecordCountForSession(sessionId: Long): Int

    suspend fun getTotalDurationForSession(sessionId: Long): Long?

    suspend fun deleteRecordById(recordId: Long)

    // ── StudySession operations ────────────────────────────────────────────

    suspend fun insertSession(session: StudySession): Long

    suspend fun updateSession(session: StudySession)

    suspend fun getSessionById(sessionId: Long): StudySession?

    fun observeSessionById(sessionId: Long): Flow<StudySession?>

    fun getAllSessions(): Flow<List<StudySession>>

    fun getSessionsForDeck(deckId: Long): Flow<List<StudySession>>

    fun getSessionsInRange(startTime: Long, endTime: Long): Flow<List<StudySession>>

    suspend fun closeSession(sessionId: Long, endedAt: Long, cardsReviewed: Int)

    suspend fun deleteSessionById(sessionId: Long)

    suspend fun deleteSessionsByDeck(deckId: Long)
}
