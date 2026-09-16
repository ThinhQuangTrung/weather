package com.example.weather.ui.favourite

import com.example.weather.data.model.WeatherResponse

data class FavouriteUiModel(
    val originalCityKey: String,
    val cityName: String,
    val countryCode: String = "VN",
    val weatherDesc: String = "",
    val windSpeed: Double = 0.0,
    val temp: Double = 0.0,
    val tempMin: Double = 0.0,
    val tempMax: Double = 0.0,
    val iconCode: String = "01d",
    val weatherId: Int? = null,
    val weatherResponse: WeatherResponse? = null,
    val isLoading: Boolean = false
)
