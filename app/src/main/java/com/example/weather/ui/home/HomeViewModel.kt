package com.example.weather.ui.home

import android.app.Application
import android.location.Location
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

    private val prefManager = com.example.weather.data.preference.WeatherPreferenceManager(application)

    private val _weatherState = MutableLiveData<Resource<WeatherResponse>>()
    val weatherState: LiveData<Resource<WeatherResponse>> = _weatherState

    // LiveData trả về kết quả thời tiết theo từng thành phố cụ thể (cityName, Resource)
    private val _cityWeatherResult = MutableLiveData<Pair<String, Resource<WeatherResponse>>>()
    val cityWeatherResult: LiveData<Pair<String, Resource<WeatherResponse>>> = _cityWeatherResult

    private val _tempUnit = MutableLiveData<TemperatureUnit>(
        if (prefManager.temperatureUnit == "fahrenheit") TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS
    )
    val tempUnit: LiveData<TemperatureUnit> = _tempUnit

    private val _userMessage = MutableLiveData<String?>()
    val userMessage: LiveData<String?> = _userMessage

    private val _showLocationDialog = MutableLiveData<LocationDialogType?>()
    val showLocationDialog: LiveData<LocationDialogType?> = _showLocationDialog

    // LiveData thông báo tên thành phố vừa được xác định thành công từ toạ độ GPS
    private val _locationResolvedCity = MutableLiveData<String?>()
    val locationResolvedCity: LiveData<String?> = _locationResolvedCity

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
                if (result is Resource.Success) {
                    val resolvedCity = result.data.cityName
                    currentCity = resolvedCity
                    _cityWeatherResult.value = Pair(resolvedCity, result)
                    _locationResolvedCity.value = resolvedCity
                }
                _weatherState.value = result
            } else {
                // Fallback khi không lấy được GPS (ví dụ giả lập hoặc mất tín hiệu)
                _userMessage.value = "Không thể lấy toạ độ GPS, hiển thị thời tiết mặc định"
                loadWeather(currentCity)
            }
        }
    }

    fun clearLocationResolvedCity() {
        _locationResolvedCity.value = null
    }

    /**
     * Tải thời tiết từ đối tượng Location do Bound Service cung cấp
     */
    fun fetchWeatherByLocation(location: Location) {
        viewModelScope.launch {
            currentCoordinates = Pair(location.latitude, location.longitude)
            _weatherState.value = Resource.Loading
            val result = weatherRepository.getCurrentWeatherByCoords(
                lat = location.latitude,
                lon = location.longitude
            )
            if (result is Resource.Success) {
                val resolvedCity = result.data.cityName
                currentCity = resolvedCity
                _cityWeatherResult.value = Pair(resolvedCity, result)
                _locationResolvedCity.value = resolvedCity
            }
            _weatherState.value = result
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
            _cityWeatherResult.value = Pair(cityName, Resource.Loading)
            _weatherState.value = Resource.Loading
            val result = weatherRepository.getCurrentWeather(cityName)
            _cityWeatherResult.value = Pair(cityName, result)
            _weatherState.value = result
        }
    }

    /**
     * Tải lại dữ liệu thời tiết hiện tại
     */
    fun refresh(cityName: String = currentCity) {
        val coords = currentCoordinates
        if (coords != null && locationManager.hasLocationPermission() && cityName == currentCity) {
            viewModelScope.launch {
                _weatherState.value = Resource.Loading
                _cityWeatherResult.value = Pair(cityName, Resource.Loading)
                val result = weatherRepository.getCurrentWeatherByCoords(coords.first, coords.second)
                _cityWeatherResult.value = Pair(cityName, result)
                _weatherState.value = result
            }
        } else {
            loadWeather(cityName)
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
        prefManager.temperatureUnit = if (unit == TemperatureUnit.FAHRENHEIT) "fahrenheit" else "celsius"
        if (_tempUnit.value != unit) {
            _tempUnit.value = unit
        }
    }

    /**
     * Đồng bộ đơn vị nhiệt độ từ SharedPreferences
     */
    fun syncTemperatureUnitFromPrefs() {
        val unit = if (prefManager.temperatureUnit == "fahrenheit") TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS
        if (_tempUnit.value != unit) {
            _tempUnit.value = unit
        }
    }

    /**
     * Xóa thông báo sau khi UI đã hiển thị
     */
    fun clearUserMessage() {
        _userMessage.value = null
    }
}
