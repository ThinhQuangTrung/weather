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
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    fun setLocale(context: Context, languageCode: String): Context {
        persistLanguage(context, languageCode)

        // Áp dụng với AppCompatDelegate cho Android 13+ (Per-App Language)
        try {
            val appLocale = LocaleListCompat.forLanguageTags(languageCode)
            AppCompatDelegate.setApplicationLocales(appLocale)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return updateResources(context, languageCode)
    }

    fun onAttach(context: Context): Context {
        val lang = getLanguage(context)
        return updateResources(context, lang)
    }

    private fun persistLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    @Suppress("DEPRECATION")
    private fun updateResources(context: Context, language: String): Context {
        val locale = parseLocale(language)
        Locale.setDefault(locale)

        val res: Resources = context.resources
        val config = Configuration(res.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setLocaleForApi24(config, locale)
            return context.createConfigurationContext(config)
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
            return context
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun setLocaleForApi24(config: Configuration, target: Locale) {
        val set = LinkedHashSet<Locale>()
        set.add(target)
        val defaultList = LocaleList.getDefault()
        for (i in 0 until defaultList.size()) {
            set.add(defaultList.get(i))
        }
        val array = set.toTypedArray()
        config.setLocales(LocaleList(*array))
    }

    private fun parseLocale(language: String): Locale {
        return try {
            when {
                language.contains("-") -> {
                    val parts = language.split("-")
                    Locale.Builder().setLanguage(parts[0]).setRegion(parts[1]).build()
                }
                language.contains("_") -> {
                    val parts = language.split("_")
                    Locale.Builder().setLanguage(parts[0]).setRegion(parts[1]).build()
                }
                else -> Locale.Builder().setLanguage(language).build()
            }
        } catch (e: Exception) {
            Locale.ENGLISH
        }
    }
}
