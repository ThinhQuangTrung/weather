package com.example.weather.domain.usecase

import javax.inject.Inject

import com.example.weather.core.common.Resource
import com.example.weather.domain.model.CurrentWeather
import com.example.weather.domain.repository.WeatherRepository

/**
 * UseCase: Láº¥y thá»i tiáº¿t hiá»‡n táº¡i theo tÃªn thÃ nh phá»‘.
 * ÄÃ¢y lÃ  Ä‘Æ¡n vá»‹ business logic tÃ¡i sá»­ dá»¥ng Ä‘Æ°á»£c á»Ÿ báº¥t ká»³ ViewModel nÃ o.
 */
class GetCurrentWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(cityName: String): Resource<CurrentWeather> {
        return repository.getCurrentWeather(cityName)
    }
}
