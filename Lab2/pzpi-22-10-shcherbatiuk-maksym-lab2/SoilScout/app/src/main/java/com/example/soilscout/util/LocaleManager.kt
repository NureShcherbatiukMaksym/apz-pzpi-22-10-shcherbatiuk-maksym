// app/src/main/java/com/example/soilscout/util/LocaleManager.kt
package com.example.soilscout.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.Log
import java.util.Locale

object LocaleManager {

    private const val LANGUAGE_KEY = "language_key"
    const val DEFAULT_LANGUAGE = "uk"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("app_locale_prefs", Context.MODE_PRIVATE)
        Log.d("LocaleManager", "LocaleManager initialized with language: ${getLanguage()}")
    }

    fun getLanguage(): String {
        return prefs.getString(LANGUAGE_KEY, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun setLanguage(context: Context, language: String) {
        Log.d("LocaleManager", "Setting language to: $language")
        prefs.edit().putString(LANGUAGE_KEY, language).apply()
    }

    @Suppress("DEPRECATION")
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val currentResources: Resources = context.resources
        val configuration: Configuration = Configuration(currentResources.configuration)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            Log.d("LocaleManager", "Updating resources for N+ for language: $language. Returning new context.")
            context.createConfigurationContext(configuration)
        } else {
            configuration.locale = locale
            Log.d("LocaleManager", "Updating resources for <N for language: $language. Updating existing.")
            currentResources.updateConfiguration(configuration, currentResources.displayMetrics)
            context
        }
    }

    fun applyLocale(context: Context): Context {
        val currentLanguage = getLanguage()
        Log.d("LocaleManager", "Applying locale for context. Current language: $currentLanguage")
        return updateResources(context, currentLanguage)
    }
}