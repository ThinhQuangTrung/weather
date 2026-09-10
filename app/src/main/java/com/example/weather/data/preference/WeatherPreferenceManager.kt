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

    var selectedCityIndex: Int
        get() = prefs.getInt(KEY_SELECTED_CITY_INDEX, 0)
        set(value) = prefs.edit().putInt(KEY_SELECTED_CITY_INDEX, value).apply()

    /**
     * Lấy danh sách thành phố đã lưu
     */
    fun getSavedCities(): List<String> {
        val raw = prefs.getString(KEY_SAVED_CITIES, null)
        return if (raw.isNullOrEmpty()) {
            listOf("Hanoi", "Ho Chi Minh", "Da Nang")
        } else {
            raw.split(DELIMITER).filter { it.isNotBlank() }
        }
    }

    /**
     * Lưu danh sách thành phố
     */
    fun saveCities(cities: List<String>) {
        val raw = cities.filter { it.isNotBlank() }.joinToString(DELIMITER)
        prefs.edit().putString(KEY_SAVED_CITIES, raw).apply()
    }

    /**
     * Thêm một thành phố vào danh sách nếu chưa có
     */
    fun addCity(cityName: String): Boolean {
        val trimmed = cityName.trim()
        if (trimmed.isEmpty()) return false
        val current = getSavedCities().toMutableList()
        val exists = current.any { it.equals(trimmed, ignoreCase = true) }
        if (!exists) {
            current.add(trimmed)
            saveCities(current)
            return true
        }
        return false
    }

    /**
     * Xóa một thành phố khỏi danh sách
     */
    fun removeCity(cityName: String): Boolean {
        val current = getSavedCities().toMutableList()
        val index = current.indexOfFirst { it.equals(cityName, ignoreCase = true) }
        if (index != -1 && current.size > 1) {
            current.removeAt(index)
            saveCities(current)
            if (selectedCityIndex >= current.size) {
                selectedCityIndex = current.size - 1
            }
            return true
        }
        return false
    }

    companion object {
        private const val PREF_NAME = "app_prefs"
        private const val KEY_LANGUAGE_SELECTED = "language_selected"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_WEATHER_SETUP_COMPLETED = "weather_setup_completed"
        private const val KEY_SAVED_CITIES = "saved_cities"
        private const val KEY_SELECTED_CITY_INDEX = "selected_city_index"
        private const val DELIMITER = "|||"

        const val KEY_SHOW_TEMPERATURE = "show_temperature"
        const val KEY_SHOW_HUMIDITY = "show_humidity"
        const val KEY_SHOW_WIND = "show_wind"
        const val KEY_SHOW_VISIBILITY = "show_visibility"
        const val KEY_SHOW_PRESSURE = "show_pressure"
        const val KEY_SHOW_AIR_QUALITY = "show_air_quality"
    }
}
