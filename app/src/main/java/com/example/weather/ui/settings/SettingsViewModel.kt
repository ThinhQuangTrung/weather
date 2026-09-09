package com.example.weather.ui.settings

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * - Quản lý và lưu trữ cài đặt người dùng (Ngôn ngữ, Giao diện Theme Sáng/Tối/Hệ thống, Đơn vị nhiệt độ).
 * - Lưu trạng thái  SharedPreferences.
 * - Phát ra LiveData để UI cập nhật tức thì.
 */
class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("weather_settings", Context.MODE_PRIVATE)

    private val _language = MutableLiveData<String>()
    val language: LiveData<String> = _language

    private val _theme = MutableLiveData<String>()
    val theme: LiveData<String> = _theme

    private val _unit = MutableLiveData<String>()
    val unit: LiveData<String> = _unit

    init {
        _language.value = prefs.getString("key_language", "vi")
        _theme.value = prefs.getString("key_theme", "system")
        _unit.value = prefs.getString("key_unit", "celsius")
    }

    fun setLanguage(langCode: String) {
        _language.value = langCode
        prefs.edit().putString("key_language", langCode).apply()
    }

    fun setTheme(themeMode: String) {
        _theme.value = themeMode
        prefs.edit().putString("key_theme", themeMode).apply()

        when (themeMode) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun setUnit(unitCode: String) {
        _unit.value = unitCode
        prefs.edit().putString("key_unit", unitCode).apply()
    }
}