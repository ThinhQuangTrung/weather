package com.example.weather.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Tiện ích quản lý và áp dụng ngôn ngữ (Locale) dung cho toàn bộ ứng dụng
 */
object LocaleHelper {

    private const val PREFS_NAME = "app_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    const val DEFAULT_LANGUAGE = "en"

    fun onAttach(context: Context): Context {
        val lang = getLanguage(context)
        return updateResources(context, lang)
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        return prefs.getString(
            KEY_LANGUAGE,
            DEFAULT_LANGUAGE
        ) ?: DEFAULT_LANGUAGE
    }

    fun setLocale(
        context: Context,
        languageCode: String
    ): Context {
        persistLanguage(context, languageCode)
        return updateResources(context, languageCode)
    }

    private fun persistLanguage(
        context: Context,
        language: String
    ) {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
            try {
                @Suppress("DEPRECATION")
                context.applicationContext.resources.updateConfiguration(configuration, resources.displayMetrics)
            } catch (_: Exception) { }
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
            context
        }
    }
}
