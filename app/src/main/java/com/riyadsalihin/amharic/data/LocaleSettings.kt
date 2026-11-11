package com.riyadsalihin.amharic.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The UI language is kept in SharedPreferences (not DataStore) because it has to
 * be read synchronously while the Activity attaches its base context.
 */
class LocaleSettings(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _language = MutableStateFlow(readTag(context))
    val language: StateFlow<String> = _language.asStateFlow()

    fun setLanguage(tag: String) {
        prefs.edit().putString(KEY, tag).apply()
        _language.value = tag
    }

    companion object {
        const val PREFS = "ui_prefs"
        const val KEY = "ui_language"
        const val SYSTEM = "system"
        const val ENGLISH = "en"
        const val AMHARIC = "am"

        fun readTag(context: Context): String =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, SYSTEM) ?: SYSTEM
    }
}
