package de.engel.flashdrive.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.engel.flashdrive.core.database.entity.FlashcardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<FlashcardEntity>)

    @Update
    suspend fun updateCard(card: FlashcardEntity)

    @Update
    suspend fun updateCards(cards: List<FlashcardEntity>)

    @Query("SELECT * FROM flashcards WHERE card_id = :cardId")
    suspend fun getCardById(cardId: Long): FlashcardEntity?

    @Query("SELECT * FROM flashcards WHERE card_id = :cardId")
    fun observeCardById(cardId: Long): Flow<FlashcardEntity?>

    @Query("SELECT * FROM flashcards WHERE deck_id = :deckId ORDER BY created_at ASC")
    fun getCardsByDeck(deckId: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deck_id = :deckId AND due_date <= :now ORDER BY due_date ASC")
    fun getDueCards(deckId: Long, now: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deck_id = :deckId AND due_date <= :now ORDER BY due_date ASC LIMIT :limit")
    suspend fun getDueCardsSync(deckId: Long, now: Long, limit: Int = 20): List<FlashcardEntity>

    @Query("SELECT * FROM flashcards WHERE due_date <= :now ORDER BY due_date ASC LIMIT :limit")
    suspend fun getAllDueCards(now: Long, limit: Int = 50): List<FlashcardEntity>

    @Query("SELECT COUNT(*) FROM flashcards WHERE deck_id = :deckId")
    suspend fun getCardCountForDeck(deckId: Long): Int

    @Query("SELECT COUNT(*) FROM flashcards WHERE deck_id = :deckId AND due_date <= :now")
    suspend fun getDueCardCount(deckId: Long, now: Long): Int

    @Query("UPDATE flashcards SET interval_days = :interval, ease_factor = :easeFactor, repetitions = :repetitions, due_date = :dueDate, last_reviewed_at = :reviewedAt, updated_at = :updatedAt WHERE card_id = :cardId")
    suspend fun updateScheduling(
        cardId: Long,
        interval: Int,
        easeFactor: Double,
        repetitions: Int,
        dueDate: Long,
        reviewedAt: Long,
        updatedAt: Long,
    )

    @Query("DELETE FROM flashcards WHERE card_id = :cardId")
    suspend fun deleteCardById(cardId: Long)

    @Query("DELETE FROM flashcards WHERE deck_id = :deckId")
    suspend fun deleteCardsByDeck(deckId: Long)

    @Query("SELECT * FROM flashcards WHERE front_content LIKE '%' || :query || '%' OR back_content LIKE '%' || :query || '%'")
    fun searchCards(query: String): Flow<List<FlashcardEntity>>
}
