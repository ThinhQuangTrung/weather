package com.example.weather.domain.usecase

import javax.inject.Inject

import com.example.weather.core.common.Resource
import com.example.weather.domain.model.CityLocation
import com.example.weather.domain.repository.WeatherRepository

/**
 * UseCase: TÃ¬m kiáº¿m thÃ nh phá»‘ theo tá»« khÃ³a (Geocoding API).
 */
class SearchCityUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(query: String): Resource<List<CityLocation>> {
        return repository.searchCity(query)
    }
}
