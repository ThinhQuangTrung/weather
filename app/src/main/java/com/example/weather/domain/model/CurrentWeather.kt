package com.example.weather.domain.model

/**
 * Domain Entity cho thời tiết hiện tại.
 * Không phụ thuộc vào Retrofit, Gson hay bất kỳ framework nào.
 * Đây là "ngôn ngữ" của tầng business logic.
 */
data class CurrentWeather(
    val cityName: String,
    val countryCode: String,
    val temp: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Double,
    val windDeg: Int,
    val windGust: Double?,
    val cloudiness: Int,
    val visibility: Int,
    val weatherId: Int,
    val weatherMain: String,
    val weatherDescription: String,
    val weatherIcon: String,
    val sunrise: Long,
    val sunset: Long,
    val timezone: Int,
    val timestamp: Long,
    val lat: Double,
    val lon: Double
)
