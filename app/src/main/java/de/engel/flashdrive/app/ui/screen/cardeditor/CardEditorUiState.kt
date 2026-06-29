package de.engel.flashdrive.app.ui.screen.cardeditor

import de.engel.flashdrive.core.model.Flashcard

/**
 * UI state for the Card Editor screen.
 */
data class CardEditorUiState(
    val cardId: Long? = null,
    val deckId: Long = -1L,
    val front: String = "",
    val back: String = "",
    val hint: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isNewCard: Boolean = true,
    val frontError: String? = null,
    val backError: String? = null,
) {
    val isValid: Boolean
        get() = front.isNotBlank() && back.isNotBlank()
}
