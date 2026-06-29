package de.engel.flashdrive.core.data.repository.impl

import de.engel.flashdrive.core.data.repository.FlashcardRepository
import de.engel.flashdrive.core.database.dao.FlashcardDao
import de.engel.flashdrive.core.database.entity.FlashcardEntity
import de.engel.flashdrive.core.model.Difficulty
import de.engel.flashdrive.core.model.Flashcard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlashcardRepositoryImpl @Inject constructor(
    private val flashcardDao: FlashcardDao,
) : FlashcardRepository {

    override suspend fun insertCard(card: Flashcard): Long {
        return flashcardDao.insertCard(card.toEntity())
    }

    override suspend fun insertCards(cards: List<Flashcard>) {
        flashcardDao.insertCards(cards.map { it.toEntity() })
    }

    override suspend fun updateCard(card: Flashcard) {
        flashcardDao.updateCard(card.toEntity())
    }

    override suspend fun updateCards(cards: List<Flashcard>) {
        flashcardDao.updateCards(cards.map { it.toEntity() })
    }

    override suspend fun getCardById(cardId: Long): Flashcard? {
        return flashcardDao.getCardById(cardId)?.toModel()
    }

    override fun observeCardById(cardId: Long): Flow<Flashcard?> {
        return flashcardDao.observeCardById(cardId).map { it?.toModel() }
    }

    override fun getCardsByDeck(deckId: Long): Flow<List<Flashcard>> {
        return flashcardDao.getCardsByDeck(deckId).map { list -> list.map { it.toModel() } }
    }

    override fun getDueCards(deckId: Long, now: Long): Flow<List<Flashcard>> {
        return flashcardDao.getDueCards(deckId, now).map { list -> list.map { it.toModel() } }
    }

    override suspend fun getDueCardsSync(deckId: Long, now: Long, limit: Int): List<Flashcard> {
        return flashcardDao.getDueCardsSync(deckId, now, limit).map { it.toModel() }
    }

    override suspend fun getAllDueCards(now: Long, limit: Int): List<Flashcard> {
        return flashcardDao.getAllDueCards(now, limit).map { it.toModel() }
    }

    override suspend fun getCardCountForDeck(deckId: Long): Int {
        return flashcardDao.getCardCountForDeck(deckId)
    }

    override suspend fun getDueCardCount(deckId: Long, now: Long): Int {
        return flashcardDao.getDueCardCount(deckId, now)
    }

    override suspend fun updateScheduling(
        cardId: Long,
        interval: Int,
        easeFactor: Double,
        repetitions: Int,
        dueDate: Long,
        reviewedAt: Long,
        updatedAt: Long,
    ) {
        flashcardDao.updateScheduling(
            cardId = cardId,
            interval = interval,
            easeFactor = easeFactor,
            repetitions = repetitions,
            dueDate = dueDate,
            reviewedAt = reviewedAt,
            updatedAt = updatedAt,
        )
    }

    override suspend fun deleteCardById(cardId: Long) {
        flashcardDao.deleteCardById(cardId)
    }

    override suspend fun deleteCardsByDeck(deckId: Long) {
        flashcardDao.deleteCardsByDeck(deckId)
    }

    override fun searchCards(query: String): Flow<List<Flashcard>> {
        return flashcardDao.searchCards(query).map { list -> list.map { it.toModel() } }
    }

    // ── Mappers ────────────────────────────────────────────────────────────

    private fun Flashcard.toEntity(): FlashcardEntity = FlashcardEntity(
        cardId = id,
        deckId = deckId,
        frontContent = front,
        backContent = back,
        frontMediaUri = null,
        backMediaUri = null,
        difficulty = difficulty.ordinal,
        intervalDays = intervalDays,
        easeFactor = easeFactor.toDouble(),
        repetitions = repetitionCount,
        dueDate = nextReviewAt ?: 0L,
        lastReviewedAt = lastReviewedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        tags = null,
    )

    private fun FlashcardEntity.toModel(): Flashcard = Flashcard(
        id = cardId,
        deckId = deckId,
        front = frontContent,
        back = backContent,
        hint = null,
        difficulty = Difficulty.entries.getOrElse(difficulty) { Difficulty.NEW },
        lastReviewedAt = lastReviewedAt,
        nextReviewAt = dueDate,
        repetitionCount = repetitions,
        intervalDays = intervalDays,
        easeFactor = easeFactor.toFloat(),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
