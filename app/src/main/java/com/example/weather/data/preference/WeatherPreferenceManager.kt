package com.example.weather.data.preference

import android.content.Context
import android.content.SharedPreferences

/**
 * Quản lý SharedPreferences cho luồng người dùng và thiết lập hiển thị thông số thời tiết
 */
class WeatherPreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var isLanguageSelected: Boolean
        get() = prefs.getBoolean(KEY_LANGUAGE_SELECTED, false)
        set(value) = prefs.edit().putBoolean(KEY_LANGUAGE_SELECTED, value).apply()

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()

    var isWeatherSetupCompleted: Boolean
        get() = prefs.getBoolean(KEY_WEATHER_SETUP_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_WEATHER_SETUP_COMPLETED, value).apply()

    // Cấu hình hiển thị Widget trên màn hình Home
    var showTemperature: Boolean
        get() = prefs.getBoolean(KEY_SHOW_TEMPERATURE, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_TEMPERATURE, value).apply()

    var showHumidity: Boolean
        get() = prefs.getBoolean(KEY_SHOW_HUMIDITY, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_HUMIDITY, value).apply()

    var showWind: Boolean
        get() = prefs.getBoolean(KEY_SHOW_WIND, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_WIND, value).apply()

    var showVisibility: Boolean
        get() = prefs.getBoolean(KEY_SHOW_VISIBILITY, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_VISIBILITY, value).apply()

    var showPressure: Boolean
        get() = prefs.getBoolean(KEY_SHOW_PRESSURE, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_PRESSURE, value).apply()

    var showAirQuality: Boolean
        get() = prefs.getBoolean(KEY_SHOW_AIR_QUALITY, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_AIR_QUALITY, value).apply()

    companion object {
        private const val PREF_NAME = "app_prefs"
        private const val KEY_LANGUAGE_SELECTED = "language_selected"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_WEATHER_SETUP_COMPLETED = "weather_setup_completed"

        const val KEY_SHOW_TEMPERATURE = "show_temperature"
        const val KEY_SHOW_HUMIDITY = "show_humidity"
        const val KEY_SHOW_WIND = "show_wind"
        const val KEY_SHOW_VISIBILITY = "show_visibility"
        const val KEY_SHOW_PRESSURE = "show_pressure"
        const val KEY_SHOW_AIR_QUALITY = "show_air_quality"
    }
}
