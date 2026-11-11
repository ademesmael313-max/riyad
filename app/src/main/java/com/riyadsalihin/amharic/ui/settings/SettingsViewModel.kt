package com.riyadsalihin.amharic.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riyadsalihin.amharic.data.AppSettings
import com.riyadsalihin.amharic.data.SettingsRepository
import com.riyadsalihin.amharic.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val settings: SettingsRepository) : ViewModel() {

    val state: StateFlow<AppSettings> = settings.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings(),
    )

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { settings.setTheme(mode) }
    }

    fun setShowEnglish(enabled: Boolean) {
        viewModelScope.launch { settings.setShowEnglish(enabled) }
    }

    fun setArabicScale(scale: Float) {
        viewModelScope.launch { settings.setArabicScale(scale) }
    }

    fun setAmharicScale(scale: Float) {
        viewModelScope.launch { settings.setAmharicScale(scale) }
    }
}
