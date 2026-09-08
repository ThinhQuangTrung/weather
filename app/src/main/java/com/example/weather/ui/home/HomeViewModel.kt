package com.example.weather.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weather.data.model.WeatherResponse
import com.example.weather.data.repository.WeatherRepository
import com.example.weather.utils.Resource
import kotlinx.coroutines.launch

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}

/**
 * ViewModel cho màn hình Home:
 * - Quản lý trạng thái nạp dữ liệu thời tiết thực tế từ API.
 * - Quản lý việc chuyển đổi đơn vị đo nhiệt độ (°C / °F).
 * - Quản lý trạng thái yêu thích (Favorite/Saved).
 * - Cung cấp dữ liệu sống (LiveData) để HomeFragment lắng nghe và cập nhật giao diện.
 */
class HomeViewModel @JvmOverloads constructor(
    application: Application,
    private val weatherRepository: WeatherRepository = WeatherRepository()
) : AndroidViewModel(application) {

    private val _weatherState = MutableLiveData<Resource<WeatherResponse>>()
    val weatherState: LiveData<Resource<WeatherResponse>> = _weatherState

    private val _tempUnit = MutableLiveData<TemperatureUnit>(TemperatureUnit.CELSIUS)
    val tempUnit: LiveData<TemperatureUnit> = _tempUnit

    private val _isFavorite = MutableLiveData<Boolean>(false)
    val isFavorite: LiveData<Boolean> = _isFavorite

    private var currentCity: String = "Hanoi"

    init {
        loadWeather(currentCity)
    }

    /**
     * Tải dữ liệu thời tiết cho một thành phố
     */
    fun loadWeather(cityName: String = currentCity) {
        currentCity = cityName
        viewModelScope.launch {
            _weatherState.value = Resource.Loading
            val result = weatherRepository.getCurrentWeather(cityName)
            _weatherState.value = result
        }
    }

    /**
     * Tải lại dữ liệu thời tiết
     */
    fun refresh() {
        loadWeather(currentCity)
    }

    /**
     * Chuyển đổi đơn vị nhiệt độ giữa °C và °F
     */
    fun setTemperatureUnit(unit: TemperatureUnit) {
        if (_tempUnit.value != unit) {
            _tempUnit.value = unit
        }
    }

    /**
     * Chuyển đổi trạng thái yêu thích của địa điểm hiện tại
     */
    fun toggleFavorite() {
        _isFavorite.value = !(_isFavorite.value ?: false)
    }
}
