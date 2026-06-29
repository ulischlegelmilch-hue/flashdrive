package de.engel.flashdrive.core.data.repository.impl

import de.engel.flashdrive.core.data.repository.DeckRepository
import de.engel.flashdrive.core.data.repository.DeckWithCardCount
import de.engel.flashdrive.core.database.dao.DeckDao
import de.engel.flashdrive.core.database.dao.FlashcardDao
import de.engel.flashdrive.core.database.entity.DeckEntity
import de.engel.flashdrive.core.model.Deck
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeckRepositoryImpl @Inject constructor(
    private val deckDao: DeckDao,
    private val flashcardDao: FlashcardDao,
) : DeckRepository {

    override suspend fun insertDeck(deck: Deck): Long {
        return deckDao.insertDeck(deck.toEntity())
    }

    override suspend fun updateDeck(deck: Deck) {
        deckDao.updateDeck(deck.toEntity())
    }

    override suspend fun getDeckById(deckId: Long): Deck? {
        return deckDao.getDeckById(deckId)?.toModel()
    }

    override fun observeDeckById(deckId: Long): Flow<Deck?> {
        return deckDao.observeDeckById(deckId).map { it?.toModel() }
    }

    override fun getAllDecks(isArchived: Boolean): Flow<List<Deck>> {
        return deckDao.getAllDecks(isArchived).map { list -> list.map { it.toModel() } }
    }

    override fun getAllDecksByName(): Flow<List<Deck>> {
        return deckDao.getAllDecksByName().map { list -> list.map { it.toModel() } }
    }

    override fun searchDecks(query: String): Flow<List<Deck>> {
        return deckDao.searchDecks(query).map { list -> list.map { it.toModel() } }
    }

    override suspend fun updateCardCount(deckId: Long, count: Int) {
        deckDao.updateCardCount(deckId, count)
    }

    override suspend fun updateLastStudied(deckId: Long, timestamp: Long) {
        deckDao.updateLastStudied(deckId, timestamp)
    }

    override suspend fun getAllDecksSnapshot(): List<Deck> {
        return deckDao.getAllDecksSnapshot().map { it.toModel() }
    }

    override suspend fun deleteDeckById(deckId: Long) {
        // Delete cards first, then deck (foreign key safety)
        flashcardDao.deleteCardsByDeck(deckId)
        deckDao.deleteDeckById(deckId)
    }

    override suspend fun getDeckWithCardCount(deckId: Long): DeckWithCardCount? {
        val entity = deckDao.getDeckById(deckId) ?: return null
        val count = flashcardDao.getCardCountForDeck(deckId)
        return DeckWithCardCount(
            deck = entity.toModel(),
            cardCount = count,
        )
    }

    // ── Mappers ────────────────────────────────────────────────────────────

    private fun Deck.toEntity(): DeckEntity = DeckEntity(
        deckId = id,
        name = name,
        description = description,
        color = color,
        icon = icon,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastStudiedAt = null,
        cardCount = cardCount,
        isArchived = isArchived,
    )

    private fun DeckEntity.toModel(): Deck = Deck(
        id = deckId,
        name = name,
        description = description,
        color = color,
        icon = icon,
        cardCount = cardCount,
        newCardsCount = 0,
        dueCardsCount = 0,
        completionRate = if (cardCount > 0) (cardCount - newCardsCount).toFloat() / cardCount else 0f,
        isArchived = isArchived,
        isFavorite = false,
        sortOrder = 0,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
