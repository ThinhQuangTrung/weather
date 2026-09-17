package com.example.weather.domain.model

/**
 * Domain Entity cho dự báo thời tiết theo giờ (một mốc 3h).
 */
data class ForecastHour(
    val timestamp: Long,
    val dtTxt: String,
    val temp: Double,
    val tempMin: Double,
    val tempMax: Double,
    val feelsLike: Double,
    val humidity: Int,
    val weatherId: Int,
    val weatherIcon: String,
    val weatherDescription: String,
    val windSpeed: Double,
    val cloudiness: Int,
    val pop: Double          // Probability of precipitation (0.0 - 1.0)
)

/**
 * Domain Entity cho dự báo thời tiết theo ngày (nhóm các ForecastHour của cùng 1 ngày).
 */
data class DailyForecast(
    val dateKey: String,           // "yyyy-MM-dd"
    val items: List<ForecastHour>  // Các mốc 3h trong ngày
)

/**
 * Domain Entity thông tin thành phố kèm dự báo.
 */
data class ForecastResult(
    val cityName: String,
    val countryCode: String,
    val sunrise: Long,
    val sunset: Long,
    val dailyForecasts: List<DailyForecast>
)
