package com.example.weather.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model ánh xạ dữ liệu phản hồi từ OpenWeatherMap 5-Day / 3-Hour Forecast API (/data/2.5/forecast)
 */
data class ForecastResponse(
    @SerializedName("cod") val cod: String,
    @SerializedName("message") val message: Int,
    @SerializedName("cnt") val count: Int,
    @SerializedName("list") val forecastList: List<ForecastItem>?,
    @SerializedName("city") val city: CityInfo?
)

data class ForecastItem(
    @SerializedName("dt") val timestamp: Long,
    @SerializedName("main") val main: MainWeatherData?,
    @SerializedName("weather") val weatherList: List<WeatherCondition>?,
    @SerializedName("wind") val wind: WindData?,
    @SerializedName("clouds") val clouds: CloudData? = null,
    @SerializedName("pop") val pop: Double? = null,
    @SerializedName("visibility") val visibility: Int? = null,
    @SerializedName("dt_txt") val dtTxt: String?
)

data class CityInfo(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
)
