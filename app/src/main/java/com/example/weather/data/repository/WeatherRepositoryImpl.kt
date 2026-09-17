package com.example.weather.data.repository

import com.example.weather.core.common.Resource
import com.example.weather.data.mapper.toDomain
import com.example.weather.data.remote.api.WeatherApiService
import com.example.weather.domain.model.CityLocation
import com.example.weather.domain.model.CurrentWeather
import com.example.weather.domain.model.ForecastResult
import com.example.weather.domain.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

/**
 * Triển khai cụ thể của WeatherRepository interface.
 * Gọi API, xử lý lỗi, rồi dùng Mapper chuyển DTO → Domain Entity.
 */
class WeatherRepositoryImpl @Inject constructor(
    private val apiService: WeatherApiService,
    @Named("api_key") private val apiKey: String
) : WeatherRepository {

    override suspend fun getCurrentWeather(cityName: String): Resource<CurrentWeather> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentWeather(cityName = cityName, apiKey = apiKey)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.toDomain())
                } else {
                    Resource.Error("Lỗi từ máy chủ: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi kết nối mạng", e)
            }
        }
    }

    override suspend fun getCurrentWeatherByCoords(lat: Double, lon: Double): Resource<CurrentWeather> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentWeatherByCoords(lat = lat, lon = lon, apiKey = apiKey)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.toDomain())
                } else {
                    Resource.Error("Lỗi từ máy chủ: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi kết nối mạng", e)
            }
        }
    }

    override suspend fun getForecast(cityName: String): Resource<ForecastResult> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getForecast(cityName = cityName, apiKey = apiKey)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.toDomain())
                } else {
                    Resource.Error("Lỗi từ máy chủ: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Đã xảy ra lỗi kết nối mạng", e)
            }
        }
    }

    override suspend fun searchCity(query: String): Resource<List<CityLocation>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchCity(cityName = query, limit = 5, apiKey = apiKey)
                if (response.isSuccessful && response.body() != null) {
                    Resource.Success(response.body()!!.map { it.toDomain() })
                } else {
                    Resource.Error("Lỗi tìm kiếm: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Không thể tìm kiếm, kiểm tra kết nối mạng", e)
            }
        }
    }
}
