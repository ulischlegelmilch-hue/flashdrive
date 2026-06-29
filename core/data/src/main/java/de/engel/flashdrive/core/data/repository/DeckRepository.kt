package de.engel.flashdrive.core.data.repository

import de.engel.flashdrive.core.model.Deck
import kotlinx.coroutines.flow.Flow

data class DeckWithCardCount(
    val deck: Deck,
    val cardCount: Int,
)

interface DeckRepository {

    suspend fun insertDeck(deck: Deck): Long

    suspend fun updateDeck(deck: Deck)

    suspend fun getDeckById(deckId: Long): Deck?

    fun observeDeckById(deckId: Long): Flow<Deck?>

    fun getAllDecks(isArchived: Boolean = false): Flow<List<Deck>>

    fun getAllDecksByName(): Flow<List<Deck>>

    fun searchDecks(query: String): Flow<List<Deck>>

    suspend fun updateCardCount(deckId: Long, count: Int)

    suspend fun updateLastStudied(deckId: Long, timestamp: Long)

    suspend fun getAllDecksSnapshot(): List<Deck>

    suspend fun deleteDeckById(deckId: Long)

    suspend fun getDeckWithCardCount(deckId: Long): DeckWithCardCount?
}
