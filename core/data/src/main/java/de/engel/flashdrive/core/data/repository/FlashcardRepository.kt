package de.engel.flashdrive.core.data.repository

import de.engel.flashdrive.core.model.Flashcard
import kotlinx.coroutines.flow.Flow

interface FlashcardRepository {

    suspend fun insertCard(card: Flashcard): Long

    suspend fun insertCards(cards: List<Flashcard>)

    suspend fun updateCard(card: Flashcard)

    suspend fun updateCards(cards: List<Flashcard>)

    suspend fun getCardById(cardId: Long): Flashcard?

    fun observeCardById(cardId: Long): Flow<Flashcard?>

    fun getCardsByDeck(deckId: Long): Flow<List<Flashcard>>

    fun getDueCards(deckId: Long, now: Long): Flow<List<Flashcard>>

    suspend fun getDueCardsSync(deckId: Long, now: Long, limit: Int = 20): List<Flashcard>

    suspend fun getAllDueCards(now: Long, limit: Int = 50): List<Flashcard>

    suspend fun getCardCountForDeck(deckId: Long): Int

    suspend fun getDueCardCount(deckId: Long, now: Long): Int

    suspend fun updateScheduling(
        cardId: Long,
        interval: Int,
        easeFactor: Double,
        repetitions: Int,
        dueDate: Long,
        reviewedAt: Long,
        updatedAt: Long,
    )

    suspend fun deleteCardById(cardId: Long)

    suspend fun deleteCardsByDeck(deckId: Long)

    fun searchCards(query: String): Flow<List<Flashcard>>
}
