package com.example.weather.data.api

import com.example.weather.data.model.ForecastResponse
import com.example.weather.data.model.GeocodingItem
import com.example.weather.data.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherApiService {

    /**
     * Tìm kiếm thành phố theo tên (Geocoding API)
     * Trả về danh sách tối đa [limit] kết quả khớp với [cityName]
     */
    @GET("geo/1.0/direct")
    suspend fun searchCity(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): Response<List<GeocodingItem>>


    /**
     * Lấy dữ liệu thời tiết hiện tại theo tên thành phố tạo HTTP request
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
