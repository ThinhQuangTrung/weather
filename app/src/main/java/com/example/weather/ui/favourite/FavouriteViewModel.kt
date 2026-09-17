package com.example.weather.ui.favourite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weather.core.base.BaseViewModel
import com.example.weather.core.common.Resource
import com.example.weather.core.common.TemperatureUnit
import com.example.weather.domain.model.CurrentWeather
import com.example.weather.domain.repository.PreferenceRepository
import com.example.weather.domain.usecase.GetCurrentWeatherUseCase
import com.example.weather.domain.usecase.GetFavouriteCitiesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel cho màn hình Favourite.
 * Sử dụng UseCases thay vì gọi Repository trực tiếp.
 */
@HiltViewModel
class FavouriteViewModel @Inject constructor(
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val getFavouriteCitiesUseCase: GetFavouriteCitiesUseCase,
    private val preferenceRepository: PreferenceRepository
) : BaseViewModel() {

    private val allFavourites = mutableListOf<FavouriteUiModel>()

    private val _favouriteList = MutableLiveData<List<FavouriteUiModel>>()
    val favouriteList: LiveData<List<FavouriteUiModel>> = _favouriteList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _tempUnit = MutableLiveData(
        if (preferenceRepository.temperatureUnit == "fahrenheit") TemperatureUnit.FAHRENHEIT
        else TemperatureUnit.CELSIUS
    )
    val tempUnit: LiveData<TemperatureUnit> = _tempUnit

    private var currentQuery: String = ""

    fun syncTemperatureUnit() {
        val unit = if (preferenceRepository.temperatureUnit == "fahrenheit")
            TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS
        if (_tempUnit.value != unit) {
            _tempUnit.value = unit
        }
    }

    /**
     * Tải dữ liệu thời tiết cho tất cả thành phố trong danh sách yêu thích.
     */
    fun loadFavourites() {
        val favoriteCities = getFavouriteCitiesUseCase()
        if (favoriteCities.isEmpty()) {
            allFavourites.clear()
            _favouriteList.value = emptyList()
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            syncTemperatureUnit()

            // Tạo danh sách tạm ban đầu (giữ dữ liệu đã tải nếu có)
            val initialList = favoriteCities.map { cityKey ->
                allFavourites.find { it.originalCityKey.equals(cityKey, ignoreCase = true) }
                    ?: FavouriteUiModel(originalCityKey = cityKey, cityName = cityKey, isLoading = true)
            }
            allFavourites.clear()
            allFavourites.addAll(initialList)
            filterAndEmitList()

            // Tải dữ liệu API song song
            val deferredList = favoriteCities.map { cityKey ->
                async {
                    val result = getCurrentWeatherUseCase(cityKey)
                    cityKey to result
                }
            }

            val results = deferredList.awaitAll()
            val updatedList = mutableListOf<FavouriteUiModel>()

            for ((cityKey, res) in results) {
                if (res is Resource.Success) {
                    val data: CurrentWeather = res.data
                    updatedList.add(
                        FavouriteUiModel(
                            originalCityKey = cityKey,
                            cityName = data.cityName.ifEmpty { cityKey },
                            countryCode = data.countryCode.ifEmpty { "VN" },
                            weatherDesc = data.weatherDescription,
                            windSpeed = data.windSpeed,
                            temp = data.temp,
                            tempMin = data.tempMin,
                            tempMax = data.tempMax,
                            iconCode = data.weatherIcon,
                            weatherId = data.weatherId,
                            isLoading = false
                        )
                    )
                } else {
                    updatedList.add(
                        FavouriteUiModel(
                            originalCityKey = cityKey,
                            cityName = cityKey,
                            countryCode = "VN",
                            weatherDesc = "Chưa có dữ liệu",
                            isLoading = false
                        )
                    )
                }
            }

            val distinctList = updatedList.distinctBy { it.originalCityKey.lowercase() }
            allFavourites.clear()
            allFavourites.addAll(distinctList)
            _isLoading.value = false
            filterAndEmitList()
        }
    }

    /**
     * Hủy yêu thích một thành phố.
     */
    fun removeFavourite(cityKey: String) {
        val trimmed = cityKey.trim()
        if (trimmed.isEmpty()) return
        preferenceRepository.removeFavoriteCity(trimmed)
        allFavourites.removeAll {
            it.originalCityKey.equals(trimmed, ignoreCase = true) ||
                    it.cityName.equals(trimmed, ignoreCase = true)
        }
        filterAndEmitList()
        postUserMessage("Đã bỏ yêu thích $trimmed")
    }

    /**
     * Tìm kiếm / lọc danh sách thành phố yêu thích.
     */
    fun searchFavourites(query: String) {
        currentQuery = query.trim()
        filterAndEmitList()
    }

    private fun filterAndEmitList() {
        if (currentQuery.isEmpty()) {
            _favouriteList.value = ArrayList(allFavourites)
        } else {
            val filtered = allFavourites.filter {
                it.cityName.contains(currentQuery, ignoreCase = true) ||
                        it.originalCityKey.contains(currentQuery, ignoreCase = true) ||
                        it.countryCode.contains(currentQuery, ignoreCase = true)
            }
            _favouriteList.value = filtered
        }
    }
}
