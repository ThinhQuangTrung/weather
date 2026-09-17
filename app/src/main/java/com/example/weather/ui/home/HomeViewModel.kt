package com.example.weather.ui.home

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weather.core.base.BaseViewModel
import com.example.weather.core.common.LocationDialogType
import com.example.weather.core.common.Resource
import com.example.weather.core.common.TemperatureUnit
import com.example.weather.data.location.LocationManager
import com.example.weather.domain.model.CurrentWeather
import com.example.weather.domain.repository.PreferenceRepository
import com.example.weather.domain.usecase.GetCurrentWeatherUseCase
import com.example.weather.domain.usecase.GetWeatherByCoordsUseCase
import com.example.weather.domain.usecase.ToggleFavouriteCityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel cho màn hình Home.
 * Quản lý dữ liệu thời tiết và trạng thái vị trí thông qua Clean Architecture UseCases.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase,
    private val getWeatherByCoordsUseCase: GetWeatherByCoordsUseCase,
    private val toggleFavouriteCityUseCase: ToggleFavouriteCityUseCase,
    private val preferenceRepository: PreferenceRepository,
    private val locationManager: LocationManager
) : BaseViewModel() {

    // LiveData trả về kết quả thời tiết theo từng thành phố cụ thể
    private val _cityWeatherResult = MutableLiveData<Pair<String, Resource<CurrentWeather>>>()
    val cityWeatherResult: LiveData<Pair<String, Resource<CurrentWeather>>> = _cityWeatherResult

    private val _tempUnit = MutableLiveData(
        if (preferenceRepository.temperatureUnit == "fahrenheit") TemperatureUnit.FAHRENHEIT
        else TemperatureUnit.CELSIUS
    )
    val tempUnit: LiveData<TemperatureUnit> = _tempUnit

    private val _showLocationDialog = MutableLiveData<LocationDialogType?>()
    val showLocationDialog: LiveData<LocationDialogType?> = _showLocationDialog

    // LiveData thông báo tên thành phố vừa được xác định từ GPS
    private val _locationResolvedCity = MutableLiveData<String?>()
    val locationResolvedCity: LiveData<String?> = _locationResolvedCity

    private val _favoriteCityChanged = MutableLiveData<Boolean>()
    val favoriteCityChanged: LiveData<Boolean> = _favoriteCityChanged

    private var currentCity: String = "Hanoi"
    private var currentCoordinates: Pair<Double, Double>? = null

    /**
     * Tải thời tiết theo vị trí GPS hiện tại của người dùng.
     */
    fun fetchWeatherByCurrentLocation() {
        viewModelScope.launch {
            if (!locationManager.hasLocationPermission()) {
                _showLocationDialog.value = LocationDialogType.OPEN_APP_SETTINGS
                loadWeather(currentCity)
                return@launch
            }

            if (!locationManager.isLocationEnabled()) {
                _showLocationDialog.value = LocationDialogType.ENABLE_GPS_SETTINGS
                loadWeather(currentCity)
                return@launch
            }

            _cityWeatherResult.value = Pair(currentCity, Resource.Loading)

            val location = locationManager.getCurrentLocation()
            if (location != null) {
                currentCoordinates = Pair(location.latitude, location.longitude)
                val result = getWeatherByCoordsUseCase(location.latitude, location.longitude)
                if (result is Resource.Success) {
                    val resolvedCity = result.data.cityName
                    currentCity = resolvedCity
                    _cityWeatherResult.value = Pair(resolvedCity, result)
                    _locationResolvedCity.value = resolvedCity
                } else {
                    _cityWeatherResult.value = Pair(currentCity, result)
                }
            } else {
                postUserMessage("Không thể lấy tọa độ GPS, hiển thị thời tiết mặc định")
                loadWeather(currentCity)
            }
        }
    }

    fun clearLocationResolvedCity() {
        _locationResolvedCity.value = null
    }

    /**
     * Tải thời tiết từ đối tượng Location do Bound Service cung cấp.
     */
    fun fetchWeatherByLocation(location: Location) {
        viewModelScope.launch {
            currentCoordinates = Pair(location.latitude, location.longitude)
            _cityWeatherResult.value = Pair(currentCity, Resource.Loading)
            val result = getWeatherByCoordsUseCase(location.latitude, location.longitude)
            if (result is Resource.Success) {
                val resolvedCity = result.data.cityName
                currentCity = resolvedCity
                _cityWeatherResult.value = Pair(resolvedCity, result)
                _locationResolvedCity.value = resolvedCity
            } else {
                _cityWeatherResult.value = Pair(currentCity, result)
            }
        }
    }

    fun onPermissionGranted() {
        dismissLocationDialog()
        fetchWeatherByCurrentLocation()
    }

    fun onPermissionDenied() {
        _showLocationDialog.value = LocationDialogType.OPEN_APP_SETTINGS
        loadWeather(currentCity)
    }

    fun dismissLocationDialog() {
        _showLocationDialog.value = null
    }

    /**
     * Tải dữ liệu thời tiết cho một thành phố cụ thể.
     */
    fun loadWeather(cityName: String = currentCity) {
        currentCity = cityName
        currentCoordinates = null
        viewModelScope.launch {
            _cityWeatherResult.value = Pair(cityName, Resource.Loading)
            val result = getCurrentWeatherUseCase(cityName)
            _cityWeatherResult.value = Pair(cityName, result)
        }
    }

    /**
     * Tải lại dữ liệu thời tiết hiện tại.
     */
    fun refresh(cityName: String = currentCity) {
        val coords = currentCoordinates
        if (coords != null && locationManager.hasLocationPermission() && cityName == currentCity) {
            viewModelScope.launch {
                _cityWeatherResult.value = Pair(cityName, Resource.Loading)
                val result = getWeatherByCoordsUseCase(coords.first, coords.second)
                _cityWeatherResult.value = Pair(cityName, result)
            }
        } else {
            loadWeather(cityName)
        }
    }

    fun setTemperatureUnit(unit: TemperatureUnit) {
        preferenceRepository.temperatureUnit =
            if (unit == TemperatureUnit.FAHRENHEIT) "fahrenheit" else "celsius"
        if (_tempUnit.value != unit) {
            _tempUnit.value = unit
        }
    }

    fun syncTemperatureUnitFromPrefs() {
        val unit = if (preferenceRepository.temperatureUnit == "fahrenheit")
            TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS
        if (_tempUnit.value != unit) {
            _tempUnit.value = unit
        }
    }

    fun isCityFavorite(cityName: String): Boolean {
        return preferenceRepository.isFavoriteCity(cityName)
    }

    fun toggleFavoriteCity(cityName: String) {
        val trimmed = cityName.trim()
        if (trimmed.isEmpty()) return
        val isNowFav = toggleFavouriteCityUseCase(trimmed)
        postUserMessage(
            if (isNowFav) "Đã thêm $trimmed vào danh sách yêu thích"
            else "Đã bỏ yêu thích $trimmed"
        )
        _favoriteCityChanged.value = isNowFav
    }
}
