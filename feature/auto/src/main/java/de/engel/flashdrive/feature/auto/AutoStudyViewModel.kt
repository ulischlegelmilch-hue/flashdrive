package de.engel.flashdrive.feature.auto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.engel.flashdrive.core.data.repository.DeckRepository
import de.engel.flashdrive.core.data.repository.FlashcardRepository
import de.engel.flashdrive.core.domain.algorithm.Sm2Algorithm
import de.engel.flashdrive.core.data.repository.CardRepository
import de.engel.flashdrive.core.model.Difficulty
import de.engel.flashdrive.core.model.Flashcard
import de.engel.flashdrive.core.model.ReviewQuality
import de.engel.flashdrive.core.model.StudyRecord
import de.engel.flashdrive.core.model.StudySession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.ArrayDeque
import javax.inject.Inject

/**
 * UI state for the Android Auto study flow.
 */
data class AutoStudyUiState(
    val deckName: String = "",
    val currentQuestion: String? = null,
    val currentAnswer: String? = null,
    val isFlipped: Boolean = false,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val isSubmitting: Boolean = false,
    val cardsStudied: Int = 0,
    val correctCount: Int = 0,
    val sessionProgress: Float = 0f,
)

/**
 * Hilt ViewModel that drives the Android Auto study experience.
 * Manages card loading, flipping, grading via SM-2, and session lifecycle.
 */
@HiltViewModel
class AutoStudyViewModel @Inject constructor(
    private val flashcardRepository: FlashcardRepository,
    private val deckRepository: DeckRepository,
    private val cardRepository: CardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AutoStudyUiState())
    val uiState: StateFlow<AutoStudyUiState> = _uiState.asStateFlow()

    /** Queue of cards still to be reviewed. */
    private val cardQueue = ArrayDeque<Flashcard>()

    private var deckId: Long = -1L
    private var sessionId: Long = 0L
    private var sessionStartTime: Long = 0L

    /**
     * Initializes the study session for the given deck.
     * Must be called before using other methods.
     */
    fun initialize(deckId: Long) {
        this.deckId = deckId
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
                        currentQuestion = cardQueue.first().front,
                        currentAnswer = cardQueue.first().back,
                        isLoading = false,
                    )
                }
            }
        }
    }

    /**
     * Flips the current card to reveal the answer.
     */
    fun flipCard() {
        if (_uiState.value.isFlipped) return
        _uiState.update { it.copy(isFlipped = true) }
    }

    /**
     * Grades the current card as correct (quality = CORRECT_HESITATION = 4).
     */
    fun gradeCorrect() {
        submitGrade(ReviewQuality.CORRECT_HESITATION)
    }

    /**
     * Grades the current card as wrong (quality = INCORRECT_REMEMBERED = 1).
     */
    fun gradeWrong() {
        submitGrade(ReviewQuality.INCORRECT_REMEMBERED)
    }

    /**
     * Skips the current card without grading.
     */
    fun skipCard() {
        val state = _uiState.value
        if (state.isSubmitting) return

        viewModelScope.launch {
            if (cardQueue.isNotEmpty()) cardQueue.removeFirst()
            advanceToNextCard()
        }
    }

    private fun submitGrade(quality: ReviewQuality) {
        val state = _uiState.value
        val card = cardQueue.firstOrNull() ?: return
        if (state.isSubmitting) return

        _uiState.update { it.copy(isSubmitting = true) }

        viewModelScope.launch {
            try {
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
                cardRepository.insertStudyRecord(
                    StudyRecord(
                        flashcardId = card.id,
                        deckId = card.deckId,
                        sessionId = sessionId,
                        quality = quality,
                        reviewedAt = System.currentTimeMillis(),
                        intervalDays = updated.intervalDays,
                        easeFactor = updated.easeFactor,
                    )
                )

                if (cardQueue.isNotEmpty()) cardQueue.removeFirst()

                val wasCorrect = quality.value >= CORRECT_THRESHOLD
                val newStudied = state.cardsStudied + 1
                val newCorrect = if (wasCorrect) state.correctCount + 1 else state.correctCount

                _uiState.update {
                    it.copy(
                        cardsStudied = newStudied,
                        correctCount = newCorrect,
                        isSubmitting = false,
                    )
                }

                advanceToNextCard()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private fun advanceToNextCard() {
        val nextCard = cardQueue.firstOrNull()
        if (nextCard == null) {
            _uiState.update {
                it.copy(
                    cardsStudied = it.cardsStudied,
                    correctCount = it.correctCount,
                    isSubmitting = false,
                )
            }
            finishSession()
        } else {
            val total = _uiState.value.cardsStudied + cardQueue.size
            val progress = if (total > 0) _uiState.value.cardsStudied.toFloat() / total else 0f
            _uiState.update {
                it.copy(
                    currentQuestion = nextCard.front,
                    currentAnswer = nextCard.back,
                    isFlipped = false,
                    sessionProgress = progress,
                    isSubmitting = false,
                )
            }
        }
    }

    /**
     * Ends the session early (user-initiated).
     */
    fun endSession() {
        viewModelScope.launch {
            finishSession()
        }
    }

    private suspend fun finishSession() {
        cardRepository.closeSession(
            sessionId = sessionId,
            endedAt = System.currentTimeMillis(),
            cardsReviewed = _uiState.value.cardsStudied,
        )
        _uiState.update {
            it.copy(
                isFinished = true,
                sessionProgress = 1f,
                currentQuestion = null,
                currentAnswer = null,
            )
        }
    }

    companion object {
        /** Grades >= 3 are considered "correct" per the SM-2 scale. */
        private const val CORRECT_THRESHOLD = 3
    }
}
