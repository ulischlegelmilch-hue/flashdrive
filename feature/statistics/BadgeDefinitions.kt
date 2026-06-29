package de.engel.flashdrive.feature.statistics

import de.engel.flashdrive.core.model.Badge
import de.engel.flashdrive.core.model.BadgeCategory
import de.engel.flashdrive.core.model.BadgeTier

/**
 * Static definition of an achievement badge.
 *
 * @param id Unique string identifier for the badge.
 * @param category The category of achievement.
 * @param tier The tier/rarity of the badge.
 * @param threshold The value required to unlock this badge.
 * @param iconName The Material Symbol name for the badge icon.
 */
data class BadgeDefinition(
    val id: String,
    val category: BadgeCategory,
    val tier: BadgeTier,
    val threshold: Int,
    val iconName: String
)

/**
 * All badge definitions used in the app.
 */
object BadgeDefinitions {

    // ── Streak Badges ──────────────────────────────────────────────────────
    val FIRST_STEPS = BadgeDefinition(
        id = "first_steps",
        category = BadgeCategory.STREAK,
        tier = BadgeTier.BRONZE,
        threshold = 3,
        iconName = "local_fire_department"
    )

    val KEEP_GOING = BadgeDefinition(
        id = "keep_going",
        category = BadgeCategory.STREAK,
        tier = BadgeTier.SILVER,
        threshold = 7,
        iconName = "whatshot"
    )

    val PERFECT_WEEK = BadgeDefinition(
        id = "perfect_week",
        category = BadgeCategory.STREAK,
        tier = BadgeTier.GOLD,
        threshold = 14,
        iconName = "emoji_events"
    )

    val UNSTOPPABLE = BadgeDefinition(
        id = "unstoppable",
        category = BadgeCategory.STREAK,
        tier = BadgeTier.DIAMOND,
        threshold = 30,
        iconName = "workspace_premium"
    )

    // ── Cards Mastered Badges ──────────────────────────────────────────────
    val KNOWLEDGE_COLLECTOR = BadgeDefinition(
        id = "knowledge_collector",
        category = BadgeCategory.CARDS_MASTERED,
        tier = BadgeTier.BRONZE,
        threshold = 10,
        iconName = "menu_book"
    )

    val SCHOLAR = BadgeDefinition(
        id = "scholar",
        category = BadgeCategory.CARDS_MASTERED,
        tier = BadgeTier.SILVER,
        threshold = 50,
        iconName = "school"
    )

    val KNOWLEDGE_MASTER = BadgeDefinition(
        id = "knowledge_master",
        category = BadgeCategory.CARDS_MASTERED,
        tier = BadgeTier.GOLD,
        threshold = 100,
        iconName = "military_tech"
    )

    // ── Study Sessions Badges ──────────────────────────────────────────────
    val BEGINNER = BadgeDefinition(
        id = "beginner",
        category = BadgeCategory.STUDY_SESSIONS,
        tier = BadgeTier.BRONZE,
        threshold = 5,
        iconName = "play_circle"
    )

    val DILIGENT = BadgeDefinition(
        id = "diligent",
        category = BadgeCategory.STUDY_SESSIONS,
        tier = BadgeTier.SILVER,
        threshold = 25,
        iconName = "auto_mode"
    )

    val EXPERIENCED = BadgeDefinition(
        id = "experienced",
        category = BadgeCategory.STUDY_SESSIONS,
        tier = BadgeTier.GOLD,
        threshold = 100,
        iconName = "verified"
    )

    // ── Accuracy Badges ────────────────────────────────────────────────────
    val PRECISE = BadgeDefinition(
        id = "precise",
        category = BadgeCategory.ACCURACY,
        tier = BadgeTier.BRONZE,
        threshold = 80,
        iconName = "gps_fixed"
    )

    val SHARP = BadgeDefinition(
        id = "sharp",
        category = BadgeCategory.ACCURACY,
        tier = BadgeTier.SILVER,
        threshold = 90,
        iconName = "my_location"
    )

    val FLAWLESS = BadgeDefinition(
        id = "flawless",
        category = BadgeCategory.ACCURACY,
        tier = BadgeTier.GOLD,
        threshold = 95,
        iconName = "stars"
    )

    // ── Dedication Badges ──────────────────────────────────────────────────
    val READY_TO_LEARN = BadgeDefinition(
        id = "ready_to_learn",
        category = BadgeCategory.DEDICATION,
        tier = BadgeTier.BRONZE,
        threshold = 50,
        iconName = "favorite"
    )

    val COMMITMENT = BadgeDefinition(
        id = "commitment",
        category = BadgeCategory.DEDICATION,
        tier = BadgeTier.SILVER,
        threshold = 200,
        iconName = "volunteer_activism"
    )

    val PASSION = BadgeDefinition(
        id = "passion",
        category = BadgeCategory.DEDICATION,
        tier = BadgeTier.GOLD,
        threshold = 500,
        iconName = "diamond"
    )

    /**
     * All badge definitions as a list.
     */
    val ALL: List<BadgeDefinition> = listOf(
        FIRST_STEPS,
        KEEP_GOING,
        PERFECT_WEEK,
        UNSTOPPABLE,
        KNOWLEDGE_COLLECTOR,
        SCHOLAR,
        KNOWLEDGE_MASTER,
        BEGINNER,
        DILIGENT,
        EXPERIENCED,
        PRECISE,
        SHARP,
        FLAWLESS,
        READY_TO_LEARN,
        COMMITMENT,
        PASSION
    )

    /**
     * Returns the badge definition for a given [Badge], matched by category and tier.
     */
    fun forBadge(badge: Badge): BadgeDefinition? =
        ALL.firstOrNull { it.category == badge.category && it.tier == badge.tier }
}
