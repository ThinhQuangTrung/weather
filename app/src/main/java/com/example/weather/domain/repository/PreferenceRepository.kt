package com.example.weather.domain.repository

/**
 * Interface Repository cho SharedPreferences — định nghĩa ở Domain Layer.
 * Data Layer sẽ implement (PreferenceRepositoryImpl wrapping WeatherPreferenceManager).
 */
interface PreferenceRepository {

    // ── Onboarding / Setup ────────────────────────────────────────────────────
    var isLanguageSelected: Boolean
    var isOnboardingCompleted: Boolean
    var isWeatherSetupCompleted: Boolean

    // ── Saved Cities ──────────────────────────────────────────────────────────
    fun getSavedCities(): List<String>
    fun saveCities(cities: List<String>)
    fun addCity(cityName: String): Boolean
    fun removeCity(cityName: String): Boolean

    var selectedCityIndex: Int

    // ── Favourite Cities ──────────────────────────────────────────────────────
    fun getFavoriteCities(): List<String>
    fun isFavoriteCity(cityName: String): Boolean
    fun toggleFavoriteCity(cityName: String): Boolean
    fun removeFavoriteCity(cityName: String)

    // ── Display Settings ──────────────────────────────────────────────────────
    var temperatureUnit: String   // "celsius" | "fahrenheit"
    var themeMode: String         // "light" | "dark" | "system"

    var showTemperature: Boolean
    var showHumidity: Boolean
    var showWind: Boolean
    var showVisibility: Boolean
    var showPressure: Boolean
    var showAirQuality: Boolean
    var showCloudCover: Boolean
    var showSunCycle: Boolean
}
