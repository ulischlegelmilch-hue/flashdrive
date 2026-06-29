package de.engel.flashdrive.core.model

/**
 * Represents a collection of flashcards grouped together for study.
 */
data class Deck(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val color: String? = null,
    val icon: String? = null,
    val cardCount: Int = 0,
    val newCardsCount: Int = 0,
    val dueCardsCount: Int = 0,
    val completionRate: Float = 0f,
    val isArchived: Boolean = false,
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
