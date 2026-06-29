package de.engel.flashdrive.feature.statistics.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.engel.flashdrive.core.data.repository.DeckRepository
import de.engel.flashdrive.core.data.repository.StudyRepository
import de.engel.flashdrive.core.domain.usecase.GetBadgesUseCase
import de.engel.flashdrive.core.model.Badge
import de.engel.flashdrive.core.model.StudyRecord
import de.engel.flashdrive.core.model.StudySession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Statistics screen.
 * Aggregates study data and computes badges, streaks, and heatmap data.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val studyRepository: StudyRepository,
    private val deckRepository: DeckRepository,
    private val getBadgesUseCase: GetBadgesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            combine(
                studyRepository.getAllSessions(),
                deckRepository.getAllDecks()
            ) { sessions, decks ->
                val completedSessions = sessions.filter { it.isCompleted }
                val totalCards = decks.sumOf { deck ->
                    deckRepository.getDeckWithCardCount(deck.id)?.cardCount ?: 0
                }

                // Fetch all records for heatmap (last 365 days)
                val now = System.currentTimeMillis()
                val oneYearAgo = now - (365L * 24L * 60L * 60L * 1000L)
                val records = mutableListOf<StudyRecord>()

                studyRepository.getRecordsInRange(oneYearAgo, now)
                    .catch { /* ignore */ }
                    .collect { records.addAll(it) }

                // Calculate mastered cards (cards with consistently high quality)
                val masteredCount = calculateMasteredCards(records)

                // Calculate streaks
                val currentStreak = calculateCurrentStreak(completedSessions, now)
                val longestStreak = calculateLongestStreak(completedSessions)

                // Calculate average accuracy
                val averageAccuracy = calculateAverageAccuracy(completedSessions)

                // Build heatmap data
                val heatmapData = buildHeatmapData(records, now)

                // Evaluate badges
                val badges = getBadgesUseCase(
                    sessions = sessions,
                    records = records,
                    masteredCount = masteredCount,
                    now = now
                )

                StatisticsUiState(
                    totalCards = totalCards,
                    masteredCards = masteredCount,
                    studySessions = completedSessions.size,
                    currentStreak = currentStreak,
                    longestStreak = longestStreak,
                    averageAccuracy = averageAccuracy,
                    badges = badges,
                    heatmapData = heatmapData,
                    isLoading = false
                )
            }.catch { error ->
                _uiState.value = _uiState.value.copy(isLoading = false)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun calculateMasteredCards(records: List<StudyRecord>): Int {
        // A card is "mastered" if its last 3 reviews were quality >= 4
        return records
            .groupBy { it.flashcardId }
            .count { (_, cardRecords) ->
                val recent = cardRecords.sortedByDescending { it.reviewedAt }.take(3)
                recent.size >= 3 && recent.all { it.quality.value >= 4 }
            }
    }

    private fun calculateCurrentStreak(sessions: List<StudySession>, now: Long): Int {
        if (sessions.isEmpty()) return 0
        val dayMillis = 24L * 60L * 60L * 1000L
        val daysWithSession = sessions
            .map { it.startedAt / dayMillis }
            .distinct()
            .sortedDescending()

        if (daysWithSession.isEmpty()) return 0

        val today = now / dayMillis
        if (daysWithSession.first() < today - 1) return 0

        var streak = 1
        for (i in 1 until daysWithSession.size) {
            if (daysWithSession[i - 1] - daysWithSession[i] == 1L) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateLongestStreak(sessions: List<StudySession>): Int {
        if (sessions.isEmpty()) return 0
        val dayMillis = 24L * 60L * 60L * 1000L
        val sortedDays = sessions
            .map { it.startedAt / dayMillis }
            .distinct()
            .sorted()

        if (sortedDays.isEmpty()) return 0

        var longest = 1
        var current = 1
        for (i in 1 until sortedDays.size) {
            if (sortedDays[i] - sortedDays[i - 1] == 1L) {
                current++
                longest = maxOf(longest, current)
            } else {
                current = 1
            }
        }
        return longest
    }

    private fun calculateAverageAccuracy(sessions: List<StudySession>): Float {
        val totalStudied = sessions.sumOf { it.cardsStudied }
        val totalCorrect = sessions.sumOf { it.correctCount }
        return if (totalStudied > 0) totalCorrect.toFloat() / totalStudied else 0f
    }

    private fun buildHeatmapData(records: List<StudyRecord>, now: Long): List<HeatmapDay> {
        val dayMillis = 24L * 60L * 60L * 1000L
        val todayStart = (now / dayMillis) * dayMillis

        // Group records by day
        val cardsPerDay = records
            .groupBy { (it.reviewedAt / dayMillis) * dayMillis }
            .mapValues { (_, recs) -> recs.size }

        // Build 365 days of data
        return (0 until 365).map { dayOffset ->
            val dayStart = todayStart - (dayOffset * dayMillis)
            HeatmapDay(
                dateMillis = dayStart,
                cardsStudied = cardsPerDay[dayStart] ?: 0
            )
        }.reversed()
    }
}
