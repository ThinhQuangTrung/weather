package com.example.weather.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weather.data.location.LocationManager
import com.example.weather.data.model.WeatherResponse
import com.example.weather.data.repository.WeatherRepository
import com.example.weather.utils.Resource
import kotlinx.coroutines.launch

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}

enum class LocationDialogType {
    OPEN_APP_SETTINGS,   // cấp quyền và dẫn vào Cài đặt ứng dụng
    ENABLE_GPS_SETTINGS  // bật GPS và dẫn vào Cài đặt GPS
}


class HomeViewModel @JvmOverloads constructor(
    application: Application,
    private val weatherRepository: WeatherRepository = WeatherRepository(),
    private val locationManager: LocationManager = LocationManager(application)
) : AndroidViewModel(application) {

    private val _weatherState = MutableLiveData<Resource<WeatherResponse>>()
    val weatherState: LiveData<Resource<WeatherResponse>> = _weatherState

    private val _tempUnit = MutableLiveData<TemperatureUnit>(TemperatureUnit.CELSIUS)
    val tempUnit: LiveData<TemperatureUnit> = _tempUnit

    private val _isFavorite = MutableLiveData<Boolean>(false)
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _userMessage = MutableLiveData<String?>()
    val userMessage: LiveData<String?> = _userMessage

    private val _showLocationDialog = MutableLiveData<LocationDialogType?>()
    val showLocationDialog: LiveData<LocationDialogType?> = _showLocationDialog

    private var currentCity: String = "vinh"
    private var currentCoordinates: Pair<Double, Double>? = null

    /**
     * Tải thời tiết theo vị trí GPS hiện tại của người dùng
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

            _weatherState.value = Resource.Loading

            val location = locationManager.getCurrentLocation()
            if (location != null) {
                currentCoordinates = Pair(location.latitude, location.longitude)
                val result = weatherRepository.getCurrentWeatherByCoords(
                    lat = location.latitude,
                    lon = location.longitude
                )
                _weatherState.value = result
            } else {
                // Fallback khi không lấy được GPS (ví dụ giả lập hoặc mất tín hiệu)
                _userMessage.value = "Không thể lấy toạ độ GPS, hiển thị thời tiết mặc định"
                loadWeather(currentCity)
            }
        }
    }

    /**
     * Xử lý khi người dùng đồng ý cấp quyền vị trí
     */
    fun onPermissionGranted() {
        dismissLocationDialog()
        fetchWeatherByCurrentLocation()
    }

    /**
     * Xử lý khi người dùng từ chối cấp quyền vị trí
     */
    fun onPermissionDenied() {
        _showLocationDialog.value = LocationDialogType.OPEN_APP_SETTINGS
        loadWeather(currentCity)
    }

    /**
     * Đóng hộp thoại thông báo cài đặt
     */
    fun dismissLocationDialog() {
        _showLocationDialog.value = null
    }

    /**
     * Tải dữ liệu thời tiết cho một thành phố cụ thể
     */
    fun loadWeather(cityName: String = currentCity) {
        currentCity = cityName
        currentCoordinates = null
        viewModelScope.launch {
            _weatherState.value = Resource.Loading
            val result = weatherRepository.getCurrentWeather(cityName)
            _weatherState.value = result
        }
    }

    /**
     * Tải lại dữ liệu thời tiết hiện tại
     */
    fun refresh() {
        val coords = currentCoordinates
        if (coords != null && locationManager.hasLocationPermission()) {
            viewModelScope.launch {
                _weatherState.value = Resource.Loading
                val result = weatherRepository.getCurrentWeatherByCoords(coords.first, coords.second)
                _weatherState.value = result
            }
        } else {
            loadWeather(currentCity)
        }
    }

    /**
     * Kiểm tra xem đã có dữ liệu vị trí toạ độ GPS chưa
     */
    fun hasCurrentCoordinates(): Boolean = currentCoordinates != null

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

    /**
     * Xóa thông báo sau khi UI đã hiển thị
     */
    fun clearUserMessage() {
        _userMessage.value = null
    }
}
