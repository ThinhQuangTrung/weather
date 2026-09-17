package com.example.weather.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.weather.core.base.BaseViewModel
import com.example.weather.domain.repository.PreferenceRepository
import com.example.weather.utils.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel cho màn hình Settings.
 * Tương tác với PreferenceRepository.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : BaseViewModel() {

    private val _language = MutableLiveData<String>()
    val language: LiveData<String> = _language

    private val _theme = MutableLiveData<String>()
    val theme: LiveData<String> = _theme

    private val _unit = MutableLiveData<String>()
    val unit: LiveData<String> = _unit

    init {
        _theme.value = preferenceRepository.themeMode
        _unit.value = preferenceRepository.temperatureUnit
    }

    fun initLanguage(currentLang: String) {
        _language.value = currentLang
    }

    fun setLanguage(langCode: String) {
        _language.value = langCode
    }

    fun setTheme(themeMode: String) {
        _theme.value = themeMode
        preferenceRepository.themeMode = themeMode
        when (themeMode) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun setUnit(unitCode: String) {
        _unit.value = unitCode
        preferenceRepository.temperatureUnit = unitCode
    }
}