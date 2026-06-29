package de.engel.flashdrive.settings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import de.engel.flashdrive.settings.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * DataStore Preferences Manager for persisting user settings.
 *
 * Stores dark mode, TTS enabled state, cloud sync preference, and language selection.
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DARK_MODE] ?: false
    }

    val ttsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.TTS_ENABLED] ?: true
    }

    val syncEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.SYNC_ENABLED] ?: false
    }

    val language: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        val code = prefs[Keys.LANGUAGE] ?: AppLanguage.GERMAN.code
        AppLanguage.entries.find { it.code == code } ?: AppLanguage.GERMAN
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TTS_ENABLED] = enabled
        }
    }

    suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SYNC_ENABLED] = enabled
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.code
        }
    }

    /**
     * Clears all user preferences back to defaults.
     */
    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
