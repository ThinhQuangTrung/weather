package com.example.weather.data.api

import com.example.weather.data.model.ForecastResponse
import com.example.weather.data.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface định nghĩa các API Endpoints của OpenWeatherMap
 */
interface WeatherApiService {

    /**
     * Lấy dữ liệu thời tiết hiện tại theo tên thành phố
     */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "vi"
    ): Response<WeatherResponse>

    /**
     * Lấy dữ liệu thời tiết hiện tại theo toạ độ GPS
     */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeatherByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "vi"
    ): Response<WeatherResponse>

    /**
     * Lấy dự báo thời tiết 5 ngày / 3 giờ theo tên thành phố
     */
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "vi"
    ): Response<ForecastResponse>

    /**
     * Lấy dự báo thời tiết 5 ngày / 3 giờ theo toạ độ GPS
     */
    @GET("data/2.5/forecast")
    suspend fun getForecastByCoords(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "vi"
    ): Response<ForecastResponse>
}
