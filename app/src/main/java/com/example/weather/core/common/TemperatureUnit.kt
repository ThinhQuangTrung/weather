package com.example.weather.core.common

/**
 * Enum đơn vị nhiệt độ dùng chung cho toàn app.
 * Di chuyển từ HomeViewModel ra core để FavouriteViewModel và SettingsViewModel cùng dùng.
 */
enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}
