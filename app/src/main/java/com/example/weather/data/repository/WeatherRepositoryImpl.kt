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
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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

                mapNetworkError(e)
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

                mapNetworkError(e)
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
                mapNetworkError(e)
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
                mapNetworkError(e)            }
        }
    }
    private fun mapNetworkError(e: Exception): Resource.Error {

        return when (e) {

            is UnknownHostException -> {
                Resource.Error(
                    message = "Không có kết nối Internet",
                    cause = e
                )
            }

            is SocketTimeoutException -> {
                Resource.Error(
                    message = "Kết nối mạng quá thời gian",
                    cause = e
                )
            }

            is IOException -> {
                Resource.Error(
                    message = "Không thể kết nối đến máy chủ",
                    cause = e
                )
            }

            else -> {
                Resource.Error(
                    message = "Đã xảy ra lỗi, vui lòng thử lại",
                    cause = e
                )
            }
        }
    }
}
