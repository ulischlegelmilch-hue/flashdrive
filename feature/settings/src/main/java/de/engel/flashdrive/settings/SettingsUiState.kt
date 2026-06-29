package de.engel.flashdrive.settings

/**
 * Supported languages for TTS and UI localization.
 */
enum class AppLanguage(val code: String, val displayName: String) {
    GERMAN("de", "Deutsch"),
    ENGLISH("en", "English"),
}

/**
 * Immutable UI state for the Settings screen.
 *
 * @param darkMode Whether dark mode is enabled.
 * @param ttsEnabled Whether text-to-speech is enabled.
 * @param syncEnabled Whether cloud sync is enabled.
 * @param language The currently selected language.
 * @param isLoading Whether the settings are being loaded.
 * @param appVersion The current app version string.
 */
data class SettingsUiState(
    val darkMode: Boolean = false,
    val ttsEnabled: Boolean = true,
    val syncEnabled: Boolean = false,
    val language: AppLanguage = AppLanguage.GERMAN,
    val isLoading: Boolean = true,
    val appVersion: String = "1.0.0",
)
