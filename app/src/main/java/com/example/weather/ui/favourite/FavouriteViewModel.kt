package com.example.weather.ui.favourite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.data.repository.WeatherRepository
import com.example.weather.ui.home.TemperatureUnit
import com.example.weather.utils.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class FavouriteViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: WeatherRepository = WeatherRepository(),
    private val prefManager: WeatherPreferenceManager = WeatherPreferenceManager(application)
) : AndroidViewModel(application) {

    private val allFavourites = mutableListOf<FavouriteUiModel>()

    private val _favouriteList = MutableLiveData<List<FavouriteUiModel>>()
    val favouriteList: LiveData<List<FavouriteUiModel>> = _favouriteList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _userMessage = MutableLiveData<String?>()
    val userMessage: LiveData<String?> = _userMessage

    private val _tempUnit = MutableLiveData(
        if (prefManager.temperatureUnit == "fahrenheit") TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS
    )
    val tempUnit: LiveData<TemperatureUnit> = _tempUnit

    private var currentQuery: String = ""

    fun syncTemperatureUnit() {
        val unit = if (prefManager.temperatureUnit == "fahrenheit") TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS
        if (_tempUnit.value != unit) {
            _tempUnit.value = unit
        }
    }

    /**
     * Tải dữ liệu thời tiết cho tất cả thành phố trong danh sách yêu thích
     */
    fun loadFavourites() {
        val favoriteCities = prefManager.getFavoriteCities()
        if (favoriteCities.isEmpty()) {
            allFavourites.clear()
            _favouriteList.value = emptyList()
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            syncTemperatureUnit()

            // Tạo danh sách tạm ban đầu giữ các dữ liệu đã tải nếu có
            val initialList = favoriteCities.map { cityKey ->
                allFavourites.find { it.originalCityKey.equals(cityKey, ignoreCase = true) }
                    ?: FavouriteUiModel(originalCityKey = cityKey, cityName = cityKey, isLoading = true)
            }
            allFavourites.clear()
            allFavourites.addAll(initialList)
            filterAndEmitList()

            // Tải dữ liệu API song song cho các thành phố yêu thích
            val deferredList = favoriteCities.map { cityKey ->
                async {
                    val result = repository.getCurrentWeather(cityKey)
                    cityKey to result
                }
            }

            val results = deferredList.awaitAll()
            val updatedList = mutableListOf<FavouriteUiModel>()

            for ((cityKey, res) in results) {
                if (res is Resource.Success) {
                    val data = res.data
                    val condition = data.weatherList?.firstOrNull()
                    val main = data.main
                    val wind = data.wind
                    val sys = data.sys

                    updatedList.add(
                        FavouriteUiModel(
                            originalCityKey = cityKey,
                            cityName = data.cityName.ifEmpty { cityKey },
                            countryCode = sys?.country ?: "VN",
                            weatherDesc = condition?.description ?: "",
                            windSpeed = wind?.speed ?: 0.0,
                            temp = main?.temp ?: 0.0,
                            tempMin = main?.tempMin ?: 0.0,
                            tempMax = main?.tempMax ?: 0.0,
                            iconCode = condition?.icon ?: "01d",
                            weatherId = condition?.id,
                            weatherResponse = data,
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

            // Loại bỏ trùng lặp nếu có
            val distinctList = updatedList.distinctBy { it.originalCityKey.lowercase() }
            allFavourites.clear()
            allFavourites.addAll(distinctList)
            _isLoading.value = false
            filterAndEmitList()
        }
    }

    /**
     * Hủy yêu thích một thành phố từ màn hình Yêu thích ngay trong 1 lần nhấn
     */
    fun removeFavourite(cityKey: String) {
        val trimmed = cityKey.trim()
        if (trimmed.isEmpty()) return

        prefManager.removeFavoriteCity(trimmed)
        allFavourites.removeAll {
            it.originalCityKey.equals(trimmed, ignoreCase = true) ||
            it.cityName.equals(trimmed, ignoreCase = true)
        }
        filterAndEmitList()
        _userMessage.value = "Đã bỏ yêu thích $trimmed"
    }

    /**
     * Tìm kiếm / lọc danh sách thành phố yêu thích
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

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
