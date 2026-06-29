package de.engel.flashdrive.core.domain.usecase

import de.engel.flashdrive.core.model.Badge
import de.engel.flashdrive.core.model.BadgeCategory
import de.engel.flashdrive.core.model.StudyRecord
import de.engel.flashdrive.core.model.StudySession

/**
 * Evaluates which badges should be unlocked based on the user's study history.
 *
 * Badge rules:
 * - STREAK: consecutive days with at least one study session.
 * - CARDS_MASTERED: total cards with MASTERED difficulty.
 * - STUDY_SESSIONS: total completed study sessions.
 * - ACCURACY: overall accuracy across all sessions >= threshold.
 * - SPEED: average response time below threshold (placeholder).
 * - DEDICATION: total cards studied.
 */
class GetBadgesUseCase {

    /**
     * Evaluates badge unlock status based on study history.
     *
     * @param sessions All study sessions.
     * @param records All study records (individual card reviews).
     * @param masteredCount Number of cards currently in MASTERED state.
     * @param now Current timestamp in millis.
     * @return List of badges with updated progress/unlock state.
     */
    operator fun invoke(
        sessions: List<StudySession>,
        records: List<StudyRecord>,
        masteredCount: Int,
        now: Long = System.currentTimeMillis()
    ): List<Badge> {
        val completedSessions = sessions.filter { it.isCompleted }
        val streakDays = calculateStreak(completedSessions, now)
        val totalCardsStudied = records.size
        val overallAccuracy = calculateOverallAccuracy(completedSessions)

        return buildBadgeList(
            streakDays = streakDays,
            masteredCount = masteredCount,
            sessionCount = completedSessions.size,
            totalCardsStudied = totalCardsStudied,
            accuracy = overallAccuracy
        )
    }

    private fun buildBadgeList(
        streakDays: Int,
        masteredCount: Int,
        sessionCount: Int,
        totalCardsStudied: Int,
        accuracy: Float
    ): List<Badge> {
        val now = System.currentTimeMillis()
        val badges = mutableListOf<Badge>()

        // STREAK badges
        badges.add(createBadge("Erste Schritte", "3 Tage am Streak", BadgeCategory.STREAK, de.engel.flashdrive.core.model.BadgeTier.BRONZE, streakDays, 3, now))
        badges.add(createBadge("Dranbleiben", "7 Tage am Streak", BadgeCategory.STREAK, de.engel.flashdrive.core.model.BadgeTier.SILVER, streakDays, 7, now))
        badges.add(createBadge("Perfekte Woche", "14 Tage am Streak", BadgeCategory.STREAK, de.engel.flashdrive.core.model.BadgeTier.GOLD, streakDays, 14, now))
        badges.add(createBadge("Unaufhaltsam", "30 Tage am Streak", BadgeCategory.STREAK, de.engel.flashdrive.core.model.BadgeTier.PLATINUM, streakDays, 30, now))

        // CARDS_MASTERED badges
        badges.add(createBadge("Wissenssammler", "10 Karten gemeistert", BadgeCategory.CARDS_MASTERED, de.engel.flashdrive.core.model.BadgeTier.BRONZE, masteredCount, 10, now))
        badges.add(createBadge("Gelehrter", "50 Karten gemeistert", BadgeCategory.CARDS_MASTERED, de.engel.flashdrive.core.model.BadgeTier.SILVER, masteredCount, 50, now))
        badges.add(createBadge("Meister des Wissens", "100 Karten gemeistert", BadgeCategory.CARDS_MASTERED, de.engel.flashdrive.core.model.BadgeTier.GOLD, masteredCount, 100, now))

        // STUDY_SESSIONS badges
        badges.add(createBadge("Anfänger", "5 Sessions absolviert", BadgeCategory.STUDY_SESSIONS, de.engel.flashdrive.core.model.BadgeTier.BRONZE, sessionCount, 5, now))
        badges.add(createBadge("Fleißig", "25 Sessions absolviert", BadgeCategory.STUDY_SESSIONS, de.engel.flashdrive.core.model.BadgeTier.SILVER, sessionCount, 25, now))
        badges.add(createBadge("Erfahren", "100 Sessions absolviert", BadgeCategory.STUDY_SESSIONS, de.engel.flashdrive.core.model.BadgeTier.GOLD, sessionCount, 100, now))

        // ACCURACY badges
        badges.add(createBadge("Präzise", "80% Genauigkeit", BadgeCategory.ACCURACY, de.engel.flashdrive.core.model.BadgeTier.BRONZE, (accuracy * 100).toInt(), 80, now))
        badges.add(createBadge("Scharf", "90% Genauigkeit", BadgeCategory.ACCURACY, de.engel.flashdrive.core.model.BadgeTier.SILVER, (accuracy * 100).toInt(), 90, now))
        badges.add(createBadge("Fehlerlos", "95% Genauigkeit", BadgeCategory.ACCURACY, de.engel.flashdrive.core.model.BadgeTier.GOLD, (accuracy * 100).toInt(), 95, now))

        // DEDICATION badges
        badges.add(createBadge("Lernbereit", "50 Karten gelernt", BadgeCategory.DEDICATION, de.engel.flashdrive.core.model.BadgeTier.BRONZE, totalCardsStudied, 50, now))
        badges.add(createBadge("Einsatz", "200 Karten gelernt", BadgeCategory.DEDICATION, de.engel.flashdrive.core.model.BadgeTier.SILVER, totalCardsStudied, 200, now))
        badges.add(createBadge("Leidenschaft", "500 Karten gelernt", BadgeCategory.DEDICATION, de.engel.flashdrive.core.model.BadgeTier.GOLD, totalCardsStudied, 500, now))

        return badges
    }

    private fun createBadge(
        name: String,
        description: String,
        category: BadgeCategory,
        tier: de.engel.flashdrive.core.model.BadgeTier,
        currentValue: Int,
        target: Int,
        now: Long
    ): Badge {
        val unlocked = currentValue >= target
        return Badge(
            name = name,
            description = description,
            category = category,
            tier = tier,
            target = target,
            currentValue = currentValue.coerceAtMost(target),
            isUnlocked = unlocked,
            unlockedAt = if (unlocked) now else null
        )
    }

    private fun calculateStreak(sessions: List<StudySession>, now: Long): Int {
        if (sessions.isEmpty()) return 0

        val dayMillis = 24L * 60L * 60L * 1000L
        val daysWithSession = sessions
            .map { it.startedAt / dayMillis }
            .distinct()
            .sortedDescending()

        if (daysWithSession.isEmpty()) return 0

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

    private fun calculateOverallAccuracy(sessions: List<StudySession>): Float {
        val totalStudied = sessions.sumOf { it.cardsStudied }
        val totalCorrect = sessions.sumOf { it.correctCount }
        return if (totalStudied > 0) totalCorrect.toFloat() / totalStudied else 0f
    }
}
