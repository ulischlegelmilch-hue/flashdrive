package de.engel.flashdrive.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import de.engel.flashdrive.core.database.entity.DeckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity): Long

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Query("SELECT * FROM decks WHERE deck_id = :deckId")
    suspend fun getDeckById(deckId: Long): DeckEntity?

    @Query("SELECT * FROM decks WHERE deck_id = :deckId")
    fun observeDeckById(deckId: Long): Flow<DeckEntity?>

    @Query("SELECT * FROM decks WHERE is_archived = :isArchived ORDER BY updated_at DESC")
    fun getAllDecks(isArchived: Boolean = false): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks ORDER BY name ASC")
    fun getAllDecksByName(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchDecks(query: String): Flow<List<DeckEntity>>

    @Query("UPDATE decks SET card_count = :count WHERE deck_id = :deckId")
    suspend fun updateCardCount(deckId: Long, count: Int)

    @Query("UPDATE decks SET last_studied_at = :timestamp WHERE deck_id = :deckId")
    suspend fun updateLastStudied(deckId: Long, timestamp: Long)

    @Query("DELETE FROM decks WHERE deck_id = :deckId")
    suspend fun deleteDeckById(deckId: Long)

    @Query("SELECT * FROM decks")
    suspend fun getAllDecksSnapshot(): List<DeckEntity>
}
