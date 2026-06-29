package de.engel.flashdrive.core.data.repository.impl

import de.engel.flashdrive.core.data.repository.StudyRepository
import de.engel.flashdrive.core.database.dao.StudyRecordDao
import de.engel.flashdrive.core.database.dao.StudySessionDao
import de.engel.flashdrive.core.database.entity.StudyRecordEntity
import de.engel.flashdrive.core.database.entity.StudySessionEntity
import de.engel.flashdrive.core.model.ReviewQuality
import de.engel.flashdrive.core.model.StudyRecord
import de.engel.flashdrive.core.model.StudySession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyRepositoryImpl @Inject constructor(
    private val studyRecordDao: StudyRecordDao,
    private val studySessionDao: StudySessionDao,
) : StudyRepository {

    // ── StudyRecord operations ─────────────────────────────────────────────

    override suspend fun insertRecord(record: StudyRecord): Long {
        return studyRecordDao.insertRecord(record.toEntity())
    }

    override suspend fun insertRecords(records: List<StudyRecord>) {
        studyRecordDao.insertRecords(records.map { it.toEntity() })
    }

    override suspend fun getRecordById(recordId: Long): StudyRecord? {
        return studyRecordDao.getRecordById(recordId)?.toModel()
    }

    override fun getRecordsForSession(sessionId: Long): Flow<List<StudyRecord>> {
        return studyRecordDao.getRecordsForSession(sessionId).map { list -> list.map { it.toModel() } }
    }

    override fun getRecordsForCard(cardId: Long): Flow<List<StudyRecord>> {
        return studyRecordDao.getRecordsForCard(cardId).map { list -> list.map { it.toModel() } }
    }

    override fun getRecordsInRange(startTime: Long, endTime: Long): Flow<List<StudyRecord>> {
        return studyRecordDao.getRecordsInRange(startTime, endTime).map { list -> list.map { it.toModel() } }
    }

    override suspend fun getAverageGradeForCard(cardId: Long): Double? {
        return studyRecordDao.getAverageGradeForCard(cardId)
    }

    override suspend fun getRecordCountForSession(sessionId: Long): Int {
        return studyRecordDao.getRecordCountForSession(sessionId)
    }

    override suspend fun getTotalDurationForSession(sessionId: Long): Long? {
        return studyRecordDao.getTotalDurationForSession(sessionId)
    }

    override suspend fun deleteRecordById(recordId: Long) {
        studyRecordDao.deleteRecordById(recordId)
    }

    // ── StudySession operations ────────────────────────────────────────────

    override suspend fun insertSession(session: StudySession): Long {
        return studySessionDao.insertSession(session.toEntity())
    }

    override suspend fun updateSession(session: StudySession) {
        studySessionDao.updateSession(session.toEntity())
    }

    override suspend fun getSessionById(sessionId: Long): StudySession? {
        return studySessionDao.getSessionById(sessionId)?.toModel()
    }

    override fun observeSessionById(sessionId: Long): Flow<StudySession?> {
        return studySessionDao.observeSessionById(sessionId).map { it?.toModel() }
    }

    override fun getAllSessions(): Flow<List<StudySession>> {
        return studySessionDao.getAllSessions().map { list -> list.map { it.toModel() } }
    }

    override fun getSessionsForDeck(deckId: Long): Flow<List<StudySession>> {
        return studySessionDao.getSessionsForDeck(deckId).map { list -> list.map { it.toModel() } }
    }

    override fun getSessionsInRange(startTime: Long, endTime: Long): Flow<List<StudySession>> {
        return studySessionDao.getSessionsInRange(startTime, endTime).map { list -> list.map { it.toModel() } }
    }

    override suspend fun closeSession(sessionId: Long, endedAt: Long, cardsReviewed: Int) {
        studySessionDao.closeSession(sessionId, endedAt, cardsReviewed)
    }

    override suspend fun deleteSessionById(sessionId: Long) {
        studySessionDao.deleteSessionById(sessionId)
    }

    override suspend fun deleteSessionsByDeck(deckId: Long) {
        studySessionDao.deleteSessionsByDeck(deckId)
    }

    // ── Mappers ────────────────────────────────────────────────────────────

    private fun StudyRecord.toEntity(): StudyRecordEntity = StudyRecordEntity(
        recordId = id,
        cardId = flashcardId,
        deckId = deckId,
        sessionId = sessionId,
        grade = quality.value,
        reviewedAt = reviewedAt,
        durationMs = responseTimeMs ?: 0L,
        previousInterval = 0,
        newInterval = 0,
    )

    private fun StudyRecordEntity.toModel(): StudyRecord = StudyRecord(
        id = recordId,
        flashcardId = cardId,
        deckId = deckId,
        sessionId = sessionId,
        quality = ReviewQuality.entries.find { it.value == grade } ?: ReviewQuality.CORRECT_DIFFICULT,
        responseTimeMs = durationMs,
        reviewedAt = reviewedAt,
    )

    private fun StudySession.toEntity(): StudySessionEntity = StudySessionEntity(
        sessionId = id,
        deckId = deckId,
        startedAt = startedAt,
        endedAt = endedAt,
        cardsReviewed = cardsStudied,
        mode = if (isCompleted) "review" else "learning",
    )

    private fun StudySessionEntity.toModel(): StudySession = StudySession(
        id = sessionId,
        deckId = deckId,
        startedAt = startedAt,
        endedAt = endedAt,
        cardsStudied = cardsReviewed,
        correctCount = 0,
        incorrectCount = 0,
        totalTimeMs = 0L,
        isCompleted = endedAt != null,
    )
}
