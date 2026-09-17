package com.example.weather.domain.usecase

import javax.inject.Inject

import com.example.weather.domain.repository.PreferenceRepository

/**
 * UseCase: Láº¥y danh sÃ¡ch thÃ nh phá»‘ yÃªu thÃ­ch.
 */
class GetFavouriteCitiesUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke(): List<String> {
        return preferenceRepository.getFavoriteCities()
    }
}
