package com.example.weather.domain.repository

import com.example.weather.core.common.Resource
import com.example.weather.domain.model.CityLocation
import com.example.weather.domain.model.CurrentWeather
import com.example.weather.domain.model.ForecastResult

/**
 * Interface Repository thời tiết — định nghĩa ở Domain Layer.
 * Data Layer sẽ implement interface này (WeatherRepositoryImpl).
 * ViewModel/UseCase chỉ phụ thuộc vào interface này, không biết đến data layer.
 */
interface WeatherRepository {

    /** Lấy thời tiết hiện tại theo tên thành phố */
    suspend fun getCurrentWeather(cityName: String): Resource<CurrentWeather>

    /** Lấy thời tiết hiện tại theo tọa độ GPS */
    suspend fun getCurrentWeatherByCoords(lat: Double, lon: Double): Resource<CurrentWeather>

    /** Lấy dự báo 5 ngày theo tên thành phố */
    suspend fun getForecast(cityName: String): Resource<ForecastResult>

    /** Tìm kiếm thành phố theo từ khóa (Geocoding API) */
    suspend fun searchCity(query: String): Resource<List<CityLocation>>
}
