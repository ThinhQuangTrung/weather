package com.example.weather.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO ánh xạ dữ liệu phản hồi từ OpenWeatherMap Current Weather API (/data/2.5/weather).
 * Chỉ dùng ở tầng data, không được dùng trực tiếp ở UI/Domain.
 */
data class WeatherDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val cityName: String,
    @SerializedName("coord") val coordinates: CoordinatesDto?,
    @SerializedName("weather") val weatherList: List<WeatherConditionDto>?,
    @SerializedName("main") val main: MainWeatherDataDto?,
    @SerializedName("wind") val wind: WindDataDto?,
    @SerializedName("clouds") val clouds: CloudDataDto?,
    @SerializedName("sys") val sys: SysDataDto?,
    @SerializedName("visibility") val visibility: Int?,
    @SerializedName("dt") val timestamp: Long,
    @SerializedName("timezone") val timezone: Int
)

data class CoordinatesDto(
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double
)

data class WeatherConditionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class MainWeatherDataDto(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("sea_level") val seaLevel: Int? = null,
    @SerializedName("grnd_level") val groundLevel: Int? = null
)

data class WindDataDto(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val deg: Int,
    @SerializedName("gust") val gust: Double? = null
)

data class CloudDataDto(
    @SerializedName("all") val cloudiness: Int
)

data class SysDataDto(
    @SerializedName("country") val country: String?,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
)
