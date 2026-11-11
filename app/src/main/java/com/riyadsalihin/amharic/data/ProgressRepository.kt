package com.riyadsalihin.amharic.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class LastRead(val bookId: Int, val hadithNumber: Int)

class ProgressRepository(private val context: Context) {

    private val bookKey = intPreferencesKey("last_book")
    private val hadithKey = intPreferencesKey("last_hadith")

    val lastRead: Flow<LastRead?> = context.progressDataStore.data.map { prefs ->
        val book = prefs[bookKey]
        val hadith = prefs[hadithKey]
        if (book != null && hadith != null) LastRead(book, hadith) else null
    }

    suspend fun markRead(bookId: Int, hadithNumber: Int) {
        context.progressDataStore.edit { prefs ->
            prefs[bookKey] = bookId
            prefs[hadithKey] = hadithNumber
        }
    }
}
