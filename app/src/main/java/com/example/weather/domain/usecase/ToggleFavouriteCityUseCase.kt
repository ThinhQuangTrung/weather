package com.example.weather.domain.usecase

import javax.inject.Inject

import com.example.weather.domain.repository.PreferenceRepository

/**
 * UseCase: Báº­t/Táº¯t tráº¡ng thÃ¡i yÃªu thÃ­ch cá»§a thÃ nh phá»‘.
 * Tráº£ vá» true náº¿u vá»«a Ä‘Æ°á»£c thÃªm, false náº¿u vá»«a bá»‹ gá»¡.
 */
class ToggleFavouriteCityUseCase @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) {
    operator fun invoke(cityName: String): Boolean {
        return preferenceRepository.toggleFavoriteCity(cityName)
    }
}
