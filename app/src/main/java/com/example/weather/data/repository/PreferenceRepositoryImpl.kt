package com.example.weather.data.repository

import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.domain.repository.PreferenceRepository
import javax.inject.Inject

/**
 * Triển khai cụ thể của PreferenceRepository interface.
 * Wrap WeatherPreferenceManager và expose qua interface chuẩn.
 */
class PreferenceRepositoryImpl @Inject constructor(
    private val prefManager: WeatherPreferenceManager
) : PreferenceRepository {

    override var isLanguageSelected: Boolean
        get() = prefManager.isLanguageSelected
        set(value) { prefManager.isLanguageSelected = value }

    override var isOnboardingCompleted: Boolean
        get() = prefManager.isOnboardingCompleted
        set(value) { prefManager.isOnboardingCompleted = value }

    override var isWeatherSetupCompleted: Boolean
        get() = prefManager.isWeatherSetupCompleted
        set(value) { prefManager.isWeatherSetupCompleted = value }

    override fun getSavedCities(): List<String> = prefManager.getSavedCities()

    override fun saveCities(cities: List<String>) = prefManager.saveCities(cities)

    override fun addCity(cityName: String): Boolean = prefManager.addCity(cityName)

    override fun removeCity(cityName: String): Boolean = prefManager.removeCity(cityName)

    override var selectedCityIndex: Int
        get() = prefManager.selectedCityIndex
        set(value) { prefManager.selectedCityIndex = value }

    override fun getFavoriteCities(): List<String> = prefManager.getFavoriteCities()

    override fun isFavoriteCity(cityName: String): Boolean = prefManager.isFavoriteCity(cityName)

    override fun toggleFavoriteCity(cityName: String): Boolean = prefManager.toggleFavoriteCity(cityName)

    override fun removeFavoriteCity(cityName: String) = prefManager.removeFavoriteCity(cityName)

    override var temperatureUnit: String
        get() = prefManager.temperatureUnit
        set(value) { prefManager.temperatureUnit = value }

    override var themeMode: String
        get() = prefManager.themeMode
        set(value) { prefManager.themeMode = value }

    override var showTemperature: Boolean
        get() = prefManager.showTemperature
        set(value) { prefManager.showTemperature = value }

    override var showHumidity: Boolean
        get() = prefManager.showHumidity
        set(value) { prefManager.showHumidity = value }

    override var showWind: Boolean
        get() = prefManager.showWind
        set(value) { prefManager.showWind = value }

    override var showVisibility: Boolean
        get() = prefManager.showVisibility
        set(value) { prefManager.showVisibility = value }

    override var showPressure: Boolean
        get() = prefManager.showPressure
        set(value) { prefManager.showPressure = value }

    override var showAirQuality: Boolean
        get() = prefManager.showAirQuality
        set(value) { prefManager.showAirQuality = value }

    override var showCloudCover: Boolean
        get() = prefManager.showCloudCover
        set(value) { prefManager.showCloudCover = value }

    override var showSunCycle: Boolean
        get() = prefManager.showSunCycle
        set(value) { prefManager.showSunCycle = value }
}
