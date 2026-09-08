package com.example.weather.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model ánh xạ dữ liệu phản hồi từ OpenWeatherMap Current Weather API (/data/2.5/weather)
 */
data class WeatherResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val cityName: String,
    @SerializedName("coord") val coordinates: Coordinates?,
    @SerializedName("weather") val weatherList: List<WeatherCondition>?,
    @SerializedName("main") val main: MainWeatherData?,
    @SerializedName("wind") val wind: WindData?,
    @SerializedName("clouds") val clouds: CloudData?,
    @SerializedName("sys") val sys: SysData?,
    @SerializedName("visibility") val visibility: Int?,
    @SerializedName("dt") val timestamp: Long,
    @SerializedName("timezone") val timezone: Int
)

data class Coordinates(
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double
)

data class WeatherCondition(
    @SerializedName("id") val id: Int,               // Weather condition code (e.g. 800, 500)
    @SerializedName("main") val main: String,         // Group of weather parameters (Rain, Snow, Extreme etc.)
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String          // Weather icon id (e.g. "01d", "10n")
)

data class MainWeatherData(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int
)

data class WindData(
    @SerializedName("speed") val speed: Double,       // Wind speed in m/s or km/h
    @SerializedName("deg") val deg: Int
)

data class CloudData(
    @SerializedName("all") val cloudiness: Int
)

data class SysData(
    @SerializedName("country") val country: String?,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
)
