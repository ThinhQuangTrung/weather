package com.example.weather.ui.forecast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.data.model.ForecastItem
import com.example.weather.data.model.ForecastResponse
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.data.repository.WeatherRepository
import com.example.weather.databinding.FragmentForecastBinding
import com.example.weather.utils.Resource
import com.example.weather.utils.WeatherIconUtil
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * ForecastFragment:
 * - Hien thi du bao thoi tiet 5 ngay va du bao theo gio (24 gio toi)
 * - Su dung OpenWeatherMap 5-Day / 3-Hour Forecast API
 * -
 */
class ForecastFragment : Fragment() {

    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!

    private val prefManager by lazy { WeatherPreferenceManager(requireContext()) }
    private val repository by lazy { WeatherRepository() }

    private lateinit var hourlyAdapter: HourlyForecastAdapter
    private lateinit var dailyAdapter: DailyForecastAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var lastLoadedCity: String? = null
    private var lastLoadedUnit: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        loadForecastData()
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            loadForecastData()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        // Khi nguoi dung chuyen sang tab Forecast tren Bottom Navigation, tu dong dong bo thanh pho tu Home
        if (!hidden) {
            loadForecastData()
        }
    }

    private fun setupRecyclerViews() {
        hourlyAdapter = HourlyForecastAdapter()
        binding.rvHourlyForecast.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyAdapter
        }

        dailyAdapter = DailyForecastAdapter()
        binding.rvDailyForecast.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = dailyAdapter
        }
    }

    private fun loadForecastData(force: Boolean = false) {
        if (_binding == null) return

        val cities = prefManager.getSavedCities()
        val selectedIndex = prefManager.selectedCityIndex.coerceIn(0, (cities.size - 1).coerceAtLeast(0))
        val currentCity = if (cities.isNotEmpty()) cities[selectedIndex] else "Hanoi"
        val currentUnit = prefManager.temperatureUnit

        // Neu da co du lieu va thanh pho + don vi khong thay doi thi giu nguyen
        if (!force && currentCity.equals(lastLoadedCity, ignoreCase = true) &&
            currentUnit == lastLoadedUnit && binding.layoutForecastContent.visibility == View.VISIBLE) {
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            binding.pbLoading.visibility = View.VISIBLE
            binding.tvErrorMessage.visibility = View.GONE

            when (val result = repository.getForecast(currentCity)) {
                is Resource.Success -> {
                    lastLoadedCity = currentCity
                    lastLoadedUnit = currentUnit
                    binding.pbLoading.visibility = View.GONE
                    binding.layoutForecastContent.visibility = View.VISIBLE
                    bindForecastData(result.data, currentCity)
                }
                is Resource.Error -> {
                    binding.pbLoading.visibility = View.GONE
                    binding.tvErrorMessage.visibility = View.VISIBLE
                    binding.tvErrorMessage.text = result.message ?: "Không thể tải dữ liệu dự báo"
                }
                is Resource.Loading -> {
                    binding.pbLoading.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun bindForecastData(data: ForecastResponse, defaultCity: String) {
        val isFahrenheit = prefManager.temperatureUnit.equals("fahrenheit", ignoreCase = true)
        val unitSymbol = if (isFahrenheit) "°F" else "°C"

        // 1. Header Dia diem
        val cityName = data.city?.name ?: defaultCity
        val country = data.city?.country ?: "Việt Nam"
        binding.tvForecastLocation.text = "$cityName, $country"

        val forecastList = data.forecastList ?: emptyList()
        if (forecastList.isEmpty()) {
            binding.tvErrorMessage.visibility = View.VISIBLE
            binding.tvErrorMessage.text = "Không có dữ liệu dự báo"
            return
        }

        // 2. Du bao theo gio (24 gio toi - 8 moc 3-hour)
        val hourlyList = processHourlyForecast(forecastList.take(8), isFahrenheit, unitSymbol)
        hourlyAdapter.submitList(hourlyList)

        // 3. Danh sach 5 ngay (Daily Forecast)
        val dailyList = processDailyForecast(forecastList, isFahrenheit, unitSymbol)
        dailyAdapter.submitList(dailyList)
    }

    /**
     * Xu ly danh sach 8 moc gio dau tien thanh du lieu theo gio
     */
    private fun processHourlyForecast(
        items: List<ForecastItem>,
        isFahrenheit: Boolean,
        unitSymbol: String
    ): List<HourlyForecastUiModel> {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        return items.mapIndexed { index, item ->
            val timeText = if (index == 0) {
                "Bây giờ"
            } else {
                timeFormat.format(Date(item.timestamp * 1000L))
            }

            val rawTemp = item.main?.temp ?: 0.0
            val displayTemp = if (isFahrenheit) {
                WeatherIconUtil.celsiusToFahrenheit(rawTemp)
            } else {
                rawTemp
            }
            val tempString = "${Math.round(displayTemp)}$unitSymbol"

            val popPercent = Math.round((item.pop ?: 0.0) * 100).toInt()
            val popString = "$popPercent% mưa"

            val iconCode = item.weatherList?.firstOrNull()?.icon

            HourlyForecastUiModel(
                time = timeText,
                iconCode = iconCode,
                tempString = tempString,
                popString = popString
            )
        }
    }

    /**
     * Gom nhom danh sach 40 items theo ngay va tao du lieu cho 5 ngay
     */
    private fun processDailyForecast(
        items: List<ForecastItem>,
        isFahrenheit: Boolean,
        unitSymbol: String
    ): List<DailyForecastUiModel> {
        // Gom nhom theo Date String
        val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val grouped = items.groupBy { item ->
            dayKeyFormat.format(Date(item.timestamp * 1000L))
        }

        val result = mutableListOf<DailyForecastUiModel>()
        var dayIndex = 0

        for ((_, dayItems) in grouped) {
            if (dayIndex >= 5) break

            // Tinh nhiet do Min / Max cua ngay
            val minTempRaw = dayItems.minOfOrNull { it.main?.tempMin ?: (it.main?.temp ?: 0.0) } ?: 0.0
            val maxTempRaw = dayItems.maxOfOrNull { it.main?.tempMax ?: (it.main?.temp ?: 0.0) } ?: 0.0

            val minTempDisplay = if (isFahrenheit) WeatherIconUtil.celsiusToFahrenheit(minTempRaw) else minTempRaw
            val maxTempDisplay = if (isFahrenheit) WeatherIconUtil.celsiusToFahrenheit(maxTempRaw) else maxTempRaw
            val tempRangeString = "${Math.round(minTempDisplay)}$unitSymbol - ${Math.round(maxTempDisplay)}$unitSymbol"

            // Chon item dai dien buoi trua (12h - 15h) hoac item dau tien
            val representativeItem = dayItems.firstOrNull { item ->
                val hour = SimpleDateFormat("HH", Locale.getDefault()).format(Date(item.timestamp * 1000L)).toIntOrNull() ?: 0
                hour in 11..15
            } ?: dayItems.first()

            val condition = representativeItem.weatherList?.firstOrNull()
            val iconCode = condition?.icon
            val description = condition?.id?.let { WeatherIconUtil.getWeatherDescription(it) }
                ?: condition?.description
                ?: "Quang đãng"

            // Tinh ten thu / tieu de ngay
            val cal = Calendar.getInstance().apply {
                timeInMillis = representativeItem.timestamp * 1000L
            }
            val dayName = formatDayLabel(dayIndex, cal)

            result.add(
                DailyForecastUiModel(
                    dayName = dayName,
                    description = description,
                    iconCode = iconCode,
                    tempRangeString = tempRangeString
                )
            )

            dayIndex++
        }

        return result
    }

    /**
     * Dinh dang ten ngay tieng Viet theo chuan thiet ke
     */
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
            Calendar.MONDAY -> "Thứ 2"
            Calendar.TUESDAY -> "Thứ 3"
            Calendar.WEDNESDAY -> "Thứ 4"
            Calendar.THURSDAY -> "Thứ 5"
            Calendar.FRIDAY -> "Thứ 6"
            Calendar.SATURDAY -> "Thứ 7"
            Calendar.SUNDAY -> "CN"
            else -> ""
        }

        return when (dayIndex) {
            0 -> "Hôm nay"
            1 -> "Ngày mai ($shortDay)"
            else -> fullDayName
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ForecastFragment()
    }
}
