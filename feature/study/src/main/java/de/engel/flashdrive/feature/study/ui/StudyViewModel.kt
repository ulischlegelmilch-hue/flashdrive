package de.engel.flashdrive.feature.study.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.engel.flashdrive.core.data.repository.DeckRepository
import de.engel.flashdrive.core.data.repository.FlashcardRepository
import de.engel.flashdrive.core.data.repository.CardRepository
import de.engel.flashdrive.core.domain.algorithm.Sm2Algorithm
import de.engel.flashdrive.core.model.Flashcard
import de.engel.flashdrive.core.model.ReviewQuality
import de.engel.flashdrive.core.model.StudyRecord
import de.engel.flashdrive.core.model.StudySession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Study screen.
 *
 * Loads all due cards for the given deck, presents them one at a time, accepts
 * grade submissions, computes the new SM-2 state, persists the result, and tracks
 * session statistics.
 */
@HiltViewModel
class StudyViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val flashcardRepository: FlashcardRepository,
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
) : ViewModel() {

    private val deckId: Long = savedStateHandle["deckId"] ?: -1L

    private val _uiState = MutableStateFlow(StudyUiState())
    val uiState: StateFlow<StudyUiState> = _uiState.asStateFlow()

    /** Queue of cards still to be reviewed. */
    private val cardQueue = ArrayDeque<Flashcard>()

    private var sessionId: Long = 0L
    private var sessionStartTime: Long = 0L
    private var correctCount = 0
    private var studiedCount = 0

    init {
        startSession()
    }

    private fun startSession() {
        viewModelScope.launch {
            sessionStartTime = System.currentTimeMillis()
            val deck = deckRepository.getDeckById(deckId)
            _uiState.update { it.copy(deckName = deck?.name ?: "") }

            // Create a new study session record.
            val newSession = StudySession(
                deckId = deckId,
                startedAt = sessionStartTime,
            )
            sessionId = cardRepository.insertStudySession(newSession)

            // Load due cards synchronously.
            val now = System.currentTimeMillis()
            val dueCards = flashcardRepository.getDueCardsSync(deckId = deckId, now = now)
            cardQueue.clear()
            cardQueue.addAll(dueCards)

            if (cardQueue.isEmpty()) {
                finishSession()
            } else {
                _uiState.update {
                    it.copy(
                        cards = cardQueue.toList(),
                        currentCard = cardQueue.first(),
                        isLoading = false,
                    )
                }
            }
        }
    }

    /**
     * Flips the current card to show the answer.
     */
    fun flipCard() {
        if (_uiState.value.isFlipped) return
        _uiState.update { it.copy(isFlipped = true) }
    }

    /**
     * Submits a grade for the current card, persists the result, and advances
     * to the next card (or finishes the session).
     *
     * @param quality The [ReviewQuality] chosen by the user.
     */
    fun submitGrade(quality: ReviewQuality) {
        val state = _uiState.value
        val card = state.currentCard ?: return
        if (state.isSubmitting) return

        _uiState.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            try {
                // Compute new SM-2 values.
                val updated = Sm2Algorithm.calculateNextReview(
                    grade = quality.value,
                    repetitionCount = card.repetitionCount,
                    easeFactor = card.easeFactor,
                    intervalDays = card.intervalDays,
                    now = System.currentTimeMillis(),
                )
                val updatedCard = card.copy(
                    nextReviewAt = updated.nextReviewAt,
                    repetitionCount = updated.repetitionCount,
                    easeFactor = updated.easeFactor,
                    intervalDays = updated.intervalDays,
                    difficulty = updated.difficulty,
                )
                cardRepository.updateCard(updatedCard)

                // Record the study event.
                val record = StudyRecord(
                    flashcardId = card.id,
                    sessionId = sessionId,
                    deckId = card.deckId,
                    quality = quality,
                    reviewedAt = System.currentTimeMillis(),
                    intervalDays = updated.intervalDays,
                    easeFactor = updated.easeFactor,
                )
                cardRepository.insertStudyRecord(record)

                studiedCount += 1
                if (quality.value >= CORRECT_THRESHOLD) {
                    correctCount += 1
                }

                // Remove current card from queue and advance.
                if (cardQueue.isNotEmpty()) cardQueue.removeFirst()

                val nextCard = cardQueue.firstOrNull()
                if (nextCard == null) {
                    _uiState.update {
                        it.copy(
                            cardsStudied = studiedCount,
                            correctCount = correctCount,
                            isSubmitting = false,
                        )
                    }
                    finishSession()
                } else {
                    val total = studiedCount + cardQueue.size
                    val progress = if (total > 0) studiedCount.toFloat() / total else 0f
                    _uiState.update {
                        it.copy(
                            cards = cardQueue.toList(),
                            currentCard = nextCard,
                            isFlipped = false,
                            cardsStudied = studiedCount,
                            correctCount = correctCount,
                            sessionProgress = progress,
                            isSubmitting = false,
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private fun finishSession() {
        viewModelScope.launch {
            cardRepository.closeSession(
                sessionId = sessionId,
                endedAt = System.currentTimeMillis(),
                cardsReviewed = studiedCount,
            )
            _uiState.update {
                it.copy(
                    isFinished = true,
                    sessionProgress = 1f,
                    currentCard = null,
                )
            }
        }
    }

    companion object {
        /** Grades >= 3 are considered "correct" per the SM-2 scale. */
        private const val CORRECT_THRESHOLD = 3
    }
}
