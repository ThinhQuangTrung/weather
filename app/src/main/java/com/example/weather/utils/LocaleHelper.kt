package com.example.weather.utils

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * Tiện ích quản lý và áp dụng ngôn ngữ (Locale) động cho toàn bộ ứng dụng
 */
object LocaleHelper {

    private const val PREFS_NAME = "app_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    const val DEFAULT_LANGUAGE = "en"

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
    ) {
        persistLanguage(context, languageCode)

        val appLocale = LocaleListCompat.forLanguageTags(languageCode)

        AppCompatDelegate.setApplicationLocales(appLocale)
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
}
