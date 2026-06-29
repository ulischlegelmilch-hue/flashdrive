package de.engel.flashdrive.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.engel.flashdrive.core.database.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Update
    suspend fun updateSession(session: StudySessionEntity)

    @Query("SELECT * FROM study_sessions WHERE session_id = :sessionId")
    suspend fun getSessionById(sessionId: Long): StudySessionEntity?

    @Query("SELECT * FROM study_sessions WHERE session_id = :sessionId")
    fun observeSessionById(sessionId: Long): Flow<StudySessionEntity?>

    @Query("SELECT * FROM study_sessions ORDER BY started_at DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE deck_id = :deckId ORDER BY started_at DESC")
    fun getSessionsForDeck(deckId: Long): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE started_at BETWEEN :startTime AND :endTime ORDER BY started_at DESC")
    fun getSessionsInRange(startTime: Long, endTime: Long): Flow<List<StudySessionEntity>>

    @Query("UPDATE study_sessions SET ended_at = :endedAt, cards_reviewed = :cardsReviewed WHERE session_id = :sessionId")
    suspend fun closeSession(sessionId: Long, endedAt: Long, cardsReviewed: Int)

    @Query("DELETE FROM study_sessions WHERE session_id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    @Query("DELETE FROM study_sessions WHERE deck_id = :deckId")
    suspend fun deleteSessionsByDeck(deckId: Long)
}
