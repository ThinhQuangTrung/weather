package com.example.weather.ui.forecast

data class HourlyForecastUiModel(
    val time: String,
    val iconCode: String?,
    val tempString: String,
    val popString: String
)

data class DailyForecastUiModel(
    val dayName: String,
    val description: String,
    val iconCode: String?,
    val tempRangeString: String
)
