package com.riyadsalihin.amharic

import android.app.Application
import android.content.Context
import com.riyadsalihin.amharic.data.BookmarksRepository
import com.riyadsalihin.amharic.data.ContentRepository
import com.riyadsalihin.amharic.data.LocaleSettings
import com.riyadsalihin.amharic.data.ProgressRepository
import com.riyadsalihin.amharic.data.SettingsRepository

class RiyadApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Tiny manual dependency container; no DI framework needed for an app this size. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val content = ContentRepository(appContext.assets)
    val bookmarks = BookmarksRepository(appContext)
    val settings = SettingsRepository(appContext)
    val progress = ProgressRepository(appContext)
    val locale = LocaleSettings(appContext)
}
