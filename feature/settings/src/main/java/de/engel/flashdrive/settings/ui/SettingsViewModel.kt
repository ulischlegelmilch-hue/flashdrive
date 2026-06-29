package de.engel.flashdrive.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.engel.flashdrive.core.data.repository.DeckRepository
import de.engel.flashdrive.core.data.repository.FlashcardRepository
import de.engel.flashdrive.settings.AppLanguage
import de.engel.flashdrive.settings.SettingsUiState
import de.engel.flashdrive.settings.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 *
 * Exposes a [StateFlow] of [SettingsUiState] that combines all user preferences
 * from [UserPreferences] DataStore. Provides methods to update each preference.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val deckRepository: DeckRepository,
    private val flashcardRepository: FlashcardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                userPreferences.darkMode,
                userPreferences.ttsEnabled,
                userPreferences.syncEnabled,
                userPreferences.language,
            ) { darkMode, ttsEnabled, syncEnabled, language ->
                SettingsUiState(
                    darkMode = darkMode,
                    ttsEnabled = ttsEnabled,
                    syncEnabled = syncEnabled,
                    language = language,
                    isLoading = false,
                )
            }.collect { state ->
                _uiState.update { state.copy(appVersion = _uiState.value.appVersion) }
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(darkMode = enabled) }
        viewModelScope.launch {
            userPreferences.setDarkMode(enabled)
        }
    }

    fun setTtsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(ttsEnabled = enabled) }
        viewModelScope.launch {
            userPreferences.setTtsEnabled(enabled)
        }
    }

    fun setSyncEnabled(enabled: Boolean) {
        _uiState.update { it.copy(syncEnabled = enabled) }
        viewModelScope.launch {
            userPreferences.setSyncEnabled(enabled)
        }
    }

    fun setLanguage(language: AppLanguage) {
        _uiState.update { it.copy(language = language) }
        viewModelScope.launch {
            userPreferences.setLanguage(language)
        }
    }

    /**
     * Resets all user data: deletes all decks (cascades to cards) and clears preferences.
     */
    fun resetAllData(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            // Delete all decks — cascade deletes cards via repository
            val decks = deckRepository.getAllDecksSnapshot()
            decks.forEach { deck ->
                deckRepository.deleteDeckById(deck.id)
            }
            // Clear user preferences to defaults
            userPreferences.clearAll()
            onDone()
        }
    }
}
