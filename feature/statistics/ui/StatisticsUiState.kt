package de.engel.flashdrive.feature.statistics.ui

import de.engel.flashdrive.core.model.Badge

/**
 * Represents a single day in the heatmap calendar.
 *
 * @param dateMillis The timestamp for this day (midnight).
 * @param cardsStudied Number of cards studied on this day.
 */
data class HeatmapDay(
    val dateMillis: Long,
    val cardsStudied: Int
)

/**
 * UI state for the Statistics screen.
 */
data class StatisticsUiState(
    val totalCards: Int = 0,
    val masteredCards: Int = 0,
    val studySessions: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val averageAccuracy: Float = 0f,
    val badges: List<Badge> = emptyList(),
    val heatmapData: List<HeatmapDay> = emptyList(),
    val isLoading: Boolean = true
)
