package com.riyadsalihin.amharic

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.riyadsalihin.amharic.data.LocaleSettings
import com.riyadsalihin.amharic.ui.RiyadApp
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val tag = LocaleSettings.readTag(newBase)
        if (tag == LocaleSettings.SYSTEM) {
            super.attachBaseContext(newBase)
            return
        }
        val locale = Locale(tag)
        val configuration = Configuration(newBase.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        super.attachBaseContext(newBase.createConfigurationContext(configuration))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as RiyadApplication).container
        setContent {
            RiyadApp(container)
        }
    }
}
