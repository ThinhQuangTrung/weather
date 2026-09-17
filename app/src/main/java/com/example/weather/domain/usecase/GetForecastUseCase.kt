package com.example.weather.domain.usecase

import javax.inject.Inject

import com.example.weather.core.common.Resource
import com.example.weather.domain.model.ForecastResult
import com.example.weather.domain.repository.WeatherRepository

/**
 * UseCase: Láº¥y dá»± bÃ¡o thá»i tiáº¿t 5 ngÃ y theo tÃªn thÃ nh phá»‘.
 */
class GetForecastUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(cityName: String): Resource<ForecastResult> {
        return repository.getForecast(cityName)
    }
}
