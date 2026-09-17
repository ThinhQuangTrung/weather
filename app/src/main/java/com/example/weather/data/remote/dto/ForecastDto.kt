package com.example.weather.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO ánh xạ dữ liệu phản hồi từ OpenWeatherMap 5-Day / 3-Hour Forecast API (/data/2.5/forecast).
 */
data class ForecastDto(
    @SerializedName("cod") val cod: String,
    @SerializedName("message") val message: Int,
    @SerializedName("cnt") val count: Int,
    @SerializedName("list") val forecastList: List<ForecastItemDto>?,
    @SerializedName("city") val city: CityInfoDto?
)

data class ForecastItemDto(
    @SerializedName("dt") val timestamp: Long,
    @SerializedName("main") val main: MainWeatherDataDto?,
    @SerializedName("weather") val weatherList: List<WeatherConditionDto>?,
    @SerializedName("wind") val wind: WindDataDto?,
    @SerializedName("clouds") val clouds: CloudDataDto? = null,
    @SerializedName("pop") val pop: Double? = null,
    @SerializedName("visibility") val visibility: Int? = null,
    @SerializedName("dt_txt") val dtTxt: String?
)

data class CityInfoDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
)
