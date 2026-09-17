package com.example.weather.domain.usecase

import javax.inject.Inject

import com.example.weather.domain.repository.PreferenceRepository

/**
 * UseCase: Quáº£n lÃ½ danh sÃ¡ch thÃ nh phá»‘ Ä‘Ã£ lÆ°u.
 * Bao gá»“m: getSavedCities, addCity, removeCity, selectedCityIndex.
 */
class ManageSavedCitiesUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    fun getSavedCities(): List<String> = preferenceRepository.getSavedCities()

    fun addCity(cityName: String): Boolean = preferenceRepository.addCity(cityName)

    fun removeCity(cityName: String): Boolean = preferenceRepository.removeCity(cityName)

    fun getSelectedIndex(): Int = preferenceRepository.selectedCityIndex

    fun setSelectedIndex(index: Int) {
        preferenceRepository.selectedCityIndex = index
    }

    fun removeFavoriteCity(cityName: String) = preferenceRepository.removeFavoriteCity(cityName)
}
