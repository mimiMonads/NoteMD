package com.example.notemd.data

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsRepository(
    private val context: Context
) {

    private object Keys {
        val DarkMode = booleanPreferencesKey("dark_mode_enabled")
    }

    private val systemDarkThemeEnabled: Boolean
        get() {
            val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return uiMode == Configuration.UI_MODE_NIGHT_YES
        }

    val darkThemeEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[Keys.DarkMode] ?: systemDarkThemeEnabled }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.DarkMode] = enabled
        }
    }
}
