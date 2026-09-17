package com.example.weather.ui.forecast

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weather.core.base.BaseViewModel
import com.example.weather.core.common.Resource
import com.example.weather.domain.model.DailyForecast
import com.example.weather.domain.model.ForecastHour
import com.example.weather.domain.repository.PreferenceRepository
import com.example.weather.domain.usecase.GetForecastUseCase
import com.example.weather.utils.WeatherIconUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import javax.inject.Inject

/**
 * ViewModel cho màn hình Forecast.
 * Tách logic xử lý dữ liệu ra khỏi Fragment — Fragment chỉ quan sát LiveData.
 */
@HiltViewModel
class ForecastViewModel @Inject constructor(
    private val getForecastUseCase: GetForecastUseCase,
    private val preferenceRepository: PreferenceRepository
) : BaseViewModel() {

    private val _hourlyList = MutableLiveData<List<HourlyForecastUiModel>>()
    val hourlyList: LiveData<List<HourlyForecastUiModel>> = _hourlyList

    private val _dailyList = MutableLiveData<List<DailyForecastUiModel>>()
    val dailyList: LiveData<List<DailyForecastUiModel>> = _dailyList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _locationHeader = MutableLiveData<String>()
    val locationHeader: LiveData<String> = _locationHeader

    private var lastLoadedCity: String? = null
    private var lastLoadedUnit: String? = null

    /**
     * Tải dữ liệu dự báo cho thành phố hiện tại đang được chọn.
     * @param force Bắt buộc tải lại dù city/unit không đổi.
     */
    fun loadForecast(force: Boolean = false) {
        val cities = preferenceRepository.getSavedCities()
        val selectedIndex = preferenceRepository.selectedCityIndex
            .coerceIn(0, (cities.size - 1).coerceAtLeast(0))
        val currentCity = if (cities.isNotEmpty()) cities[selectedIndex] else "Hanoi"
        val currentUnit = preferenceRepository.temperatureUnit

        // Không tải lại nếu dữ liệu đã có và không thay đổi
        if (!force && currentCity.equals(lastLoadedCity, ignoreCase = true) &&
            currentUnit == lastLoadedUnit &&
            _hourlyList.value?.isNotEmpty() == true
        ) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = getForecastUseCase(currentCity)) {
                is Resource.Success -> {
                    lastLoadedCity = currentCity
                    lastLoadedUnit = currentUnit
                    val data = result.data
                    _locationHeader.value = "${data.cityName}, ${data.countryCode}"

                    val isFahrenheit = currentUnit.equals("fahrenheit", ignoreCase = true)
                    val unitSymbol = if (isFahrenheit) "°F" else "°C"

                    // Xử lý danh sách theo giờ (8 mốc 3h đầu tiên)
                    val allHours = data.dailyForecasts.flatMap { it.items }
                    _hourlyList.value = processHourlyForecast(allHours.take(8), isFahrenheit, unitSymbol)

                    // Xử lý danh sách theo ngày
                    _dailyList.value = processDailyForecast(data.dailyForecasts, isFahrenheit, unitSymbol)
                    _isLoading.value = false
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    _errorMessage.value = result.message
                }
                is Resource.Loading -> {
                    _isLoading.value = true
                }
            }
        }
    }

    private fun processHourlyForecast(
        items: List<ForecastHour>,
        isFahrenheit: Boolean,
        unitSymbol: String
    ): List<HourlyForecastUiModel> {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return items.mapIndexed { index, item ->
            val timeText = if (index == 0) "Hiện tại"
            else timeFormat.format(Date(item.timestamp * 1000L))

            val displayTemp = if (isFahrenheit) WeatherIconUtil.celsiusToFahrenheit(item.temp)
            else item.temp
            val tempString = "${displayTemp.roundToInt()}$unitSymbol"
            val popPercent = (item.pop * 100).roundToInt()

            HourlyForecastUiModel(
                time = timeText,
                iconCode = item.weatherIcon,
                tempString = tempString,
                popString = "$popPercent%"
            )
        }
    }

    private fun processDailyForecast(
        dailyForecasts: List<DailyForecast>,
        isFahrenheit: Boolean,
        unitSymbol: String
    ): List<DailyForecastUiModel> {
        return dailyForecasts.take(5).mapIndexed { dayIndex, daily ->
            val minRaw = daily.items.minOfOrNull { it.tempMin } ?: 0.0
            val maxRaw = daily.items.maxOfOrNull { it.tempMax } ?: 0.0

            val minDisplay = if (isFahrenheit) WeatherIconUtil.celsiusToFahrenheit(minRaw) else minRaw
            val maxDisplay = if (isFahrenheit) WeatherIconUtil.celsiusToFahrenheit(maxRaw) else maxRaw
            val tempRange = "${minDisplay.roundToInt()}$unitSymbol - ${maxDisplay.roundToInt()}$unitSymbol"

            // Chọn item đại diện (buổi trưa 11-15h hoặc item đầu)
            val representative = daily.items.firstOrNull { item ->
                val hour = SimpleDateFormat("HH", Locale.getDefault())
                    .format(Date(item.timestamp * 1000L)).toIntOrNull() ?: 0
                hour in 11..15
            } ?: daily.items.first()

            val weatherId = representative.weatherId
            val iconCode = representative.weatherIcon

            val cal = Calendar.getInstance().apply {
                timeInMillis = representative.timestamp * 1000L
            }
            val dayName = formatDayLabel(dayIndex, cal)

            DailyForecastUiModel(
                dayName = dayName,
                description = representative.weatherDescription.replaceFirstChar { it.uppercase() },
                iconCode = iconCode,
                tempRangeString = tempRange
            )
        }
    }

    private fun formatDayLabel(dayIndex: Int, cal: Calendar): String {
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val fullDayName = when (dayOfWeek) {
            Calendar.MONDAY -> "Thứ Hai"
            Calendar.TUESDAY -> "Thứ Ba"
            Calendar.WEDNESDAY -> "Thứ Tư"
            Calendar.THURSDAY -> "Thứ Năm"
            Calendar.FRIDAY -> "Thứ Sáu"
            Calendar.SATURDAY -> "Thứ Bảy"
            Calendar.SUNDAY -> "Chủ Nhật"
            else -> ""
        }
        val shortDay = when (dayOfWeek) {
            Calendar.MONDAY -> "T2"
            Calendar.TUESDAY -> "T3"
            Calendar.WEDNESDAY -> "T4"
            Calendar.THURSDAY -> "T5"
            Calendar.FRIDAY -> "T6"
            Calendar.SATURDAY -> "T7"
            Calendar.SUNDAY -> "CN"
            else -> ""
        }
        return when (dayIndex) {
            0 -> "Hôm nay"
            1 -> "Ngày mai ($shortDay)"
            else -> fullDayName
        }
    }
}
