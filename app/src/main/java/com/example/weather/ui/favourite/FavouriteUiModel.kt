package com.example.weather.ui.favourite

/**
 * UI Model cho màn hình Favourite.
 * Đã loại bỏ WeatherResponse (raw API DTO) — chỉ giữ các field cần thiết cho UI.
 * Dữ liệu được map từ domain.model.CurrentWeather trong FavouriteViewModel.
 */
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
    val isLoading: Boolean = false
)
