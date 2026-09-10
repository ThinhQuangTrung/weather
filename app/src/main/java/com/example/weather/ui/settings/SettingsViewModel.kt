package com.example.weather.ui.settings

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.utils.LocaleHelper

/**
 * - Lưu trạng thái SharedPreferences đồng bộ qua WeatherPreferenceManager.
 * - Phát ra LiveData để UI cập nhật tức thì.
 */
class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val prefManager = WeatherPreferenceManager(application)

    private val _language = MutableLiveData<String>()
    val language: LiveData<String> = _language

    private val _theme = MutableLiveData<String>()
    val theme: LiveData<String> = _theme

    private val _unit = MutableLiveData<String>()
    val unit: LiveData<String> = _unit

    init {
        _language.value = LocaleHelper.getLanguage(application)
        _theme.value = prefManager.themeMode
        _unit.value = prefManager.temperatureUnit
    }

    fun setLanguage(langCode: String) {
        _language.value = langCode
        LocaleHelper.setLocale(getApplication(), langCode)
    }

    fun setTheme(themeMode: String) {
        _theme.value = themeMode
        prefManager.themeMode = themeMode

        when (themeMode) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun setUnit(unitCode: String) {
        _unit.value = unitCode
        prefManager.temperatureUnit = unitCode
    }
}