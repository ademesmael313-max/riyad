package com.riyadsalihin.amharic.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BookmarksRepository(private val context: Context) {

    private val key = stringSetPreferencesKey("bookmarked_hadiths")

    val bookmarks: Flow<Set<Int>> = context.settingsDataStore.data.map { prefs ->
        prefs[key]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    suspend fun toggle(number: Int) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[key] ?: emptySet()
            val value = number.toString()
            prefs[key] = if (value in current) current - value else current + value
        }
    }
}
