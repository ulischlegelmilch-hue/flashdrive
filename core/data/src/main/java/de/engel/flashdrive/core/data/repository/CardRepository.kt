package de.engel.flashdrive.core.data.repository

import de.engel.flashdrive.core.model.Flashcard
import de.engel.flashdrive.core.model.StudyRecord

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Composite repository wrapping FlashcardRepository and StudyRepository.
 * Implements CardRepository interface from domain layer.
 */
@Singleton
class CardRepository @Inject constructor(
    private val flashcardRepository: FlashcardRepository,
    private val studyRepository: StudyRepository
) : de.engel.flashdrive.core.domain.usecase.CardRepository {

    override suspend fun updateCard(card: Flashcard) {
        flashcardRepository.updateCard(card)
    }

    override suspend fun insertStudyRecord(record: StudyRecord) {
        studyRepository.insertStudyRecord(record)
    }

    suspend fun insertCard(card: Flashcard): Long = flashcardRepository.insertCard(card)

    suspend fun getCardById(id: Long) = flashcardRepository.getCardById(id)

    suspend fun getCardsByDeck(deckId: Long) = flashcardRepository.getCardsByDeck(deckId)

    suspend fun getDueCards(deckId: Long, now: Long) = flashcardRepository.getDueCards(deckId, now)

    suspend fun getDueCardsSync(deckId: Long, now: Long) = flashcardRepository.getDueCardsSync(deckId, now)

    suspend fun getAllDueCards(now: Long) = flashcardRepository.getAllDueCards(now)

    suspend fun getCardCountForDeck(deckId: Long): Int = flashcardRepository.getCardCountForDeck(deckId)

    suspend fun getDueCardCount(deckId: Long, now: Long): Int = flashcardRepository.getDueCardCount(deckId, now)

    suspend fun insertStudySession(session: de.engel.flashdrive.core.model.StudySession): Long =
        studyRepository.insertSession(session)

    suspend fun closeSession(sessionId: Long, endedAt: Long, cardsReviewed: Int) =
        studyRepository.closeSession(sessionId, endedAt, cardsReviewed)
}
