package com.example.weather.domain.usecase

import javax.inject.Inject

import com.example.weather.core.common.Resource
import com.example.weather.domain.model.CurrentWeather
import com.example.weather.domain.repository.WeatherRepository

/**
 * UseCase: Láº¥y thá»i tiáº¿t hiá»‡n táº¡i theo tá»a Ä‘á»™ GPS.
 */
class GetWeatherByCoordsUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double): Resource<CurrentWeather> {
        return repository.getCurrentWeatherByCoords(lat, lon)
    }
}
