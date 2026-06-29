package de.engel.flashdrive.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.engel.flashdrive.core.database.entity.StudyRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: StudyRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<StudyRecordEntity>)

    @Query("SELECT * FROM study_records WHERE record_id = :recordId")
    suspend fun getRecordById(recordId: Long): StudyRecordEntity?

    @Query("SELECT * FROM study_records WHERE session_id = :sessionId ORDER BY reviewed_at ASC")
    fun getRecordsForSession(sessionId: Long): Flow<List<StudyRecordEntity>>

    @Query("SELECT * FROM study_records WHERE card_id = :cardId ORDER BY reviewed_at DESC")
    fun getRecordsForCard(cardId: Long): Flow<List<StudyRecordEntity>>

    @Query("SELECT * FROM study_records WHERE reviewed_at BETWEEN :startTime AND :endTime")
    fun getRecordsInRange(startTime: Long, endTime: Long): Flow<List<StudyRecordEntity>>

    @Query("SELECT AVG(grade) FROM study_records WHERE card_id = :cardId")
    suspend fun getAverageGradeForCard(cardId: Long): Double?

    @Query("SELECT COUNT(*) FROM study_records WHERE session_id = :sessionId")
    suspend fun getRecordCountForSession(sessionId: Long): Int

    @Query("SELECT SUM(duration_ms) FROM study_records WHERE session_id = :sessionId")
    suspend fun getTotalDurationForSession(sessionId: Long): Long?

    @Query("DELETE FROM study_records WHERE record_id = :recordId")
    suspend fun deleteRecordById(recordId: Long)
}
