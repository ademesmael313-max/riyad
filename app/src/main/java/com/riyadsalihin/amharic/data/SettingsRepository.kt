package com.riyadsalihin.amharic.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val showEnglish: Boolean = true,
    val arabicScale: Float = 1f,
    val amharicScale: Float = 1f,
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val theme = stringPreferencesKey("theme")
        val showEnglish = booleanPreferencesKey("show_english")
        val arabicScale = floatPreferencesKey("arabic_scale")
        val amharicScale = floatPreferencesKey("amharic_scale")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            theme = runCatching {
                ThemeMode.valueOf(prefs[Keys.theme] ?: ThemeMode.SYSTEM.name)
            }.getOrDefault(ThemeMode.SYSTEM),
            showEnglish = prefs[Keys.showEnglish] ?: true,
            arabicScale = (prefs[Keys.arabicScale] ?: 1f).coerceIn(0.8f, 1.8f),
            amharicScale = (prefs[Keys.amharicScale] ?: 1f).coerceIn(0.8f, 1.8f),
        )
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.theme] = mode.name }
    }

    suspend fun setShowEnglish(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.showEnglish] = enabled }
    }

    suspend fun setArabicScale(scale: Float) {
        context.settingsDataStore.edit { it[Keys.arabicScale] = scale.coerceIn(0.8f, 1.8f) }
    }

    suspend fun setAmharicScale(scale: Float) {
        context.settingsDataStore.edit { it[Keys.amharicScale] = scale.coerceIn(0.8f, 1.8f) }
    }
}
