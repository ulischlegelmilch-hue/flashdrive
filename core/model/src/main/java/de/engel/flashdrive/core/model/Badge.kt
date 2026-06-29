package de.engel.flashdrive.core.model

/**
 * Represents an achievement badge that users can earn through study progress.
 */
data class Badge(
    val id: Long = 0,
    val name: String,
    val description: String,
    val iconResName: String? = null,
    val category: BadgeCategory,
    val tier: BadgeTier,
    val progress: Float = 0f,
    val target: Int = 100,
    val currentValue: Int = 0,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
) {
    /**
     * Whether the badge is fully completed/unlocked.
     */
    val isCompleted: Boolean
        get() = currentValue >= target
}

/**
 * Category of achievement the badge belongs to.
 */
enum class BadgeCategory {
    STREAK,
    CARDS_MASTERED,
    STUDY_SESSIONS,
    ACCURACY,
    SPEED,
    DEDICATION
}

/**
 * Tier/rarity of a badge.
 */
enum class BadgeTier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM,
    DIAMOND
}
