package com.example.weather.data.mapper

import com.example.weather.data.remote.dto.ForecastDto
import com.example.weather.data.remote.dto.ForecastItemDto
import com.example.weather.data.remote.dto.GeocodingItemDto
import com.example.weather.data.remote.dto.WeatherDto
import com.example.weather.domain.model.CityLocation
import com.example.weather.domain.model.CurrentWeather
import com.example.weather.domain.model.DailyForecast
import com.example.weather.domain.model.ForecastHour
import com.example.weather.domain.model.ForecastResult

/**
 * Mapper chuyển đổi DTOs (Data Layer) → Domain Entities (Domain Layer).
 * Tách biệt hoàn toàn cấu trúc API khỏi business logic.
 */

// ─── WeatherDto → CurrentWeather ────────────────────────────────────────────

fun WeatherDto.toDomain(): CurrentWeather {
    val condition = weatherList?.firstOrNull()
    return CurrentWeather(
        cityName = cityName,
        countryCode = sys?.country ?: "",
        temp = main?.temp ?: 0.0,
        feelsLike = main?.feelsLike ?: 0.0,
        tempMin = main?.tempMin ?: 0.0,
        tempMax = main?.tempMax ?: 0.0,
        humidity = main?.humidity ?: 0,
        pressure = main?.pressure ?: 0,
        windSpeed = wind?.speed ?: 0.0,
        windDeg = wind?.deg ?: 0,
        windGust = wind?.gust,
        cloudiness = clouds?.cloudiness ?: 0,
        visibility = visibility ?: 0,
        weatherId = condition?.id ?: 800,
        weatherMain = condition?.main ?: "",
        weatherDescription = condition?.description ?: "",
        weatherIcon = condition?.icon ?: "01d",
        sunrise = sys?.sunrise ?: 0L,
        sunset = sys?.sunset ?: 0L,
        timezone = timezone,
        timestamp = timestamp,
        lat = coordinates?.latitude ?: 0.0,
        lon = coordinates?.longitude ?: 0.0
    )
}

// ─── ForecastItemDto → ForecastHour ─────────────────────────────────────────

fun ForecastItemDto.toDomain(): ForecastHour {
    val condition = weatherList?.firstOrNull()
    return ForecastHour(
        timestamp = timestamp,
        dtTxt = dtTxt ?: "",
        temp = main?.temp ?: 0.0,
        tempMin = main?.tempMin ?: 0.0,
        tempMax = main?.tempMax ?: 0.0,
        feelsLike = main?.feelsLike ?: 0.0,
        humidity = main?.humidity ?: 0,
        weatherId = condition?.id ?: 800,
        weatherIcon = condition?.icon ?: "01d",
        weatherDescription = condition?.description ?: "",
        windSpeed = wind?.speed ?: 0.0,
        cloudiness = clouds?.cloudiness ?: 0,
        pop = pop ?: 0.0
    )
}

// ─── ForecastDto → ForecastResult ───────────────────────────────────────────

fun ForecastDto.toDomain(): ForecastResult {
    val allHours = forecastList?.map { it.toDomain() } ?: emptyList()

    // Nhóm theo ngày (yyyy-MM-dd) lấy từ dtTxt hoặc timestamp
    val grouped = allHours.groupBy { hour ->
        if (hour.dtTxt.length >= 10) hour.dtTxt.substring(0, 10)
        else {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            sdf.format(java.util.Date(hour.timestamp * 1000L))
        }
    }

    val dailyForecasts = grouped.map { (dateKey, items) ->
        DailyForecast(dateKey = dateKey, items = items)
    }

    return ForecastResult(
        cityName = city?.name ?: "",
        countryCode = city?.country ?: "",
        sunrise = city?.sunrise ?: 0L,
        sunset = city?.sunset ?: 0L,
        dailyForecasts = dailyForecasts
    )
}

// ─── GeocodingItemDto → CityLocation ────────────────────────────────────────

fun GeocodingItemDto.toDomain(): CityLocation {
    return CityLocation(
        name = name,
        lat = lat,
        lon = lon,
        country = country,
        state = state
    )
}
