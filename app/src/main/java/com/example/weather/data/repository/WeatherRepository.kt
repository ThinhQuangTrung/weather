package com.example.weather.data.repository

import com.example.weather.BuildConfig
import com.example.weather.data.api.RetrofitClient
import com.example.weather.data.api.WeatherApiService
import com.example.weather.data.model.ForecastResponse
import com.example.weather.data.model.GeocodingItem
import com.example.weather.data.model.WeatherResponse
import com.example.weather.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository chịu trách nhiệm quản lý nguồn dữ liệu thời tiết (Local/Remote).
 * Tách biệt hoàn toàn tầng dữ liệu khỏi ViewModel và UI.
 */
class WeatherRepository(
    private val apiService: WeatherApiService = RetrofitClient.weatherApiService,
    private val apiKey: String = BuildConfig.OPEN_WEATHER_API_KEY
) {

// Sư lý dữ liệu hiện tai của thành phố
    suspend fun getCurrentWeather(cityName: String): Resource<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentWeather(
                    cityName = cityName,
                    apiKey = apiKey
                )
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error("Lỗi từ máy chủ: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi kết nối mạng", e)
            }
        }
    }
// lấy dữ liệu thời tiết và cùng vs thằng gps Location API dã lấy ở viewmoder
    suspend fun getCurrentWeatherByCoords(lat: Double, lon: Double): Resource<WeatherResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentWeatherByCoords(
                    lat = lat,
                    lon = lon,
                    apiKey = apiKey
                )
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error("Lỗi từ máy chủ: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi kết nối mạng", e)
            }
        }
    }

    /**
     * Lấy dự báo 5 ngày cho thành phố
     */
    suspend fun getForecast(cityName: String): Resource<ForecastResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getForecast(
                    cityName = cityName,
                    apiKey = apiKey
                )
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error("Lỗi từ máy chủ: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi kết nối mạng", e)
            }
        }
    }

    /**
     * Lấy dự báo 5 ngày theo tọa độ GPS
     */
//    suspend fun getForecastByCoords(lat: Double, lon: Double): Resource<ForecastResponse> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val response = apiService.getForecastByCoords(
//                    lat = lat,
//                    lon = lon,
//                    apiKey = apiKey
//                )
//                if (response.isSuccessful && response.body() != null) {
//                    Resource.Success(response.body()!!)
//                } else {
//                    Resource.Error("Lỗi từ máy chủ: ${response.code()} ${response.message()}")
//                }
//            } catch (e: Exception) {
//                Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi kết nối mạng", e)
//            }
//        }
//    }

    /**
     * Tìm kiếm thành phố theo từ khóa sử dụng Geocoding API
     * Trả về danh sách gợi ý tối đa 5 kết quả
     */
    suspend fun searchCity(query: String): Resource<List<GeocodingItem>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchCity(
                    cityName = query,
                    limit = 5,
                    apiKey = apiKey
                )
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!)
                } else {
                    Resource.Error("Lỗi tìm kiếm: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Không thể tìm kiếm, kiểm tra kết nối mạng")
            }
        }
    }
}
