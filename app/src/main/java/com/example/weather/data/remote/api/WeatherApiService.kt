package com.example.weather.data.remote.api

import com.example.weather.data.remote.dto.ForecastDto
import com.example.weather.data.remote.dto.GeocodingItemDto
import com.example.weather.data.remote.dto.WeatherDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface Retrofit cho OpenWeatherMap API.
 * Đã cập nhật để dùng DTOs mới từ data.remote.dto thay vì data.model.
 */
interface WeatherApiService {

    /**
     * Tìm kiếm thành phố theo tên (Geocoding API)
     */
    @GET("geo/1.0/direct")
    suspend fun searchCity(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): Response<List<GeocodingItemDto>>

    /**
     * Lấy dữ liệu thời tiết hiện tại theo tên thành phố
     */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "vi"
    ): Response<WeatherDto>

    /**
     * Lấy dữ liệu thời tiết hiện tại theo tọa độ GPS
     */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "vi"
    ): Response<WeatherDto>

    /**
     * Lấy dự báo thời tiết 5 ngày / 3 giờ theo tên thành phố
     */
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "vi"
    ): Response<ForecastDto>

    /**
     * Lấy dự báo thời tiết 5 ngày / 3 giờ theo tọa độ GPS
     */
    @GET("data/2.5/forecast")
    suspend fun getForecastByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "vi"
    ): Response<ForecastDto>
}
