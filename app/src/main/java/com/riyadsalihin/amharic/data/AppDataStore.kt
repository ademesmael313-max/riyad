package com.riyadsalihin.amharic.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val Context.progressDataStore: DataStore<Preferences> by preferencesDataStore(name = "progress")
