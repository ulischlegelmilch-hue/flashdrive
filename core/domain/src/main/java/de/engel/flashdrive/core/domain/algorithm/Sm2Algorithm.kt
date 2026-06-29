package de.engel.flashdrive.core.domain.algorithm

/**
 * Implementation of the SM-2 spaced repetition algorithm by Piotr Wozniak.
 *
 * https://www.supermemo.com/en/blog/application-of-a-computer-to-improve-the-results-obtained-in-working-with-the-supermemo-method
 */
object Sm2Algorithm {

    /**
     * Minimum ease factor allowed by SM-2.
     */
    const val MIN_EASE_FACTOR = 1.3f

    /**
     * Default ease factor for new cards.
     */
    const val DEFAULT_EASE_FACTOR = 2.5f

    /**
     * Result of an SM-2 calculation containing the updated card state.
     */
    data class UpdatedValues(
        val repetitionCount: Int,
        val easeFactor: Float,
        val intervalDays: Int,
        val nextReviewAt: Long,
        val difficulty: de.engel.flashdrive.core.model.Difficulty
    )

    /**
     * Calculates the next review state based on the SM-2 algorithm.
     *
     * @param grade Quality of the response (0-5), where:
     *              0 = complete blackout
     *              1 = incorrect, but remembered upon seeing answer
     *              2 = incorrect, but answer seemed easy to recall
     *              3 = correct with difficulty
     *              4 = correct with some hesitation
     *              5 = perfect response
     * @param repetitionCount Current number of successful repetitions.
     * @param easeFactor Current ease factor (EF).
     * @param intervalDays Current inter-repetition interval in days.
     * @param now Current timestamp in millis (used to compute nextReviewAt).
     * @return UpdatedValues with the new state.
     */
    fun calculateNextReview(
        grade: Int,
        repetitionCount: Int,
        easeFactor: Float,
        intervalDays: Int,
        now: Long = System.currentTimeMillis()
    ): UpdatedValues {
        val q = grade.coerceIn(0, 5)

        // Update ease factor per SM-2 formula
        val newEf = (easeFactor + (0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f)))
            .coerceAtLeast(MIN_EASE_FACTOR)

        val newRepetitionCount: Int
        val newIntervalDays: Int

        if (q < 3) {
            // Incorrect response: reset repetitions, review again in 1 day
            newRepetitionCount = 0
            newIntervalDays = 1
        } else {
            newRepetitionCount = repetitionCount + 1
            newIntervalDays = when (newRepetitionCount) {
                1 -> 6
                2 -> (intervalDays * newEf).toInt().coerceAtLeast(1)
                else -> (intervalDays * newEf).toInt().coerceAtLeast(1)
            }
        }

        val difficulty = when {
            newRepetitionCount == 0 && q >= 3 -> de.engel.flashdrive.core.model.Difficulty.LEARNING
            q < 3 -> de.engel.flashdrive.core.model.Difficulty.LEARNING
            newRepetitionCount >= 4 && newIntervalDays >= 21 -> de.engel.flashdrive.core.model.Difficulty.MASTERED
            newRepetitionCount >= 2 -> de.engel.flashdrive.core.model.Difficulty.REVIEW
            else -> de.engel.flashdrive.core.model.Difficulty.LEARNING
        }

        val nextReviewAt = now + newIntervalDays * 24L * 60L * 60L * 1000L

        return UpdatedValues(
            repetitionCount = newRepetitionCount,
            easeFactor = newEf,
            intervalDays = newIntervalDays,
            nextReviewAt = nextReviewAt,
            difficulty = difficulty
        )
    }
}
