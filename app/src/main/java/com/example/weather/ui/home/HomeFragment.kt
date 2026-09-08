package com.example.weather.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.weather.R
import com.example.weather.data.model.WeatherResponse
import com.example.weather.databinding.FragmentHomeBinding
import com.example.weather.utils.Resource
import com.example.weather.utils.WeatherIconUtil
import java.util.Locale

/**
 * HomeFragment trong mô hình MVVM:
 * - Sử dụng ViewBinding toàn diện (không dùng findViewById).
 * - Kết nối dữ liệu với HomeViewModel và cập nhật giao diện theo LiveData.
 * - Hiển thị toàn bộ thông số thời tiết chi tiết, nạp icon động từ OpenWeatherMap CDN qua Glide.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnCelsius.setOnClickListener {
            viewModel.setTemperatureUnit(TemperatureUnit.CELSIUS)
        }

        binding.btnFahrenheit.setOnClickListener {
            viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        }

        binding.btnFavorite.setOnClickListener {
            viewModel.toggleFavorite()
        }

        binding.btnSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Cài đặt ứng dụng", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.weatherState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.pbLoading.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.pbLoading.visibility = View.GONE
                    bindWeatherData(resource.data)
                }
                is Resource.Error -> {
                    binding.pbLoading.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Không thể kết nối máy chủ thời tiết",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewModel.tempUnit.observe(viewLifecycleOwner) { unit ->
            updateUnitToggleUI(unit)
            // Cập nhật lại số liệu nhiệt độ khi đổi đơn vị
            val currentState = viewModel.weatherState.value
            if (currentState is Resource.Success) {
                bindWeatherData(currentState.data)
            }
        }

        viewModel.isFavorite.observe(viewLifecycleOwner) { isFav ->
            if (isFav) {
                binding.btnFavorite.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.stat_secondary)
                )
            } else {
                binding.btnFavorite.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.brand_section_title)
                )
            }
        }
    }

    private fun updateUnitToggleUI(unit: TemperatureUnit) {
        if (unit == TemperatureUnit.CELSIUS) {
            binding.btnCelsius.setBackgroundResource(R.drawable.bg_unit_toggle_active)
            binding.btnCelsius.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.unit_toggle_text_active)
            )
            binding.btnFahrenheit.setBackgroundColor(
                ContextCompat.getColor(requireContext(), android.R.color.transparent)
            )
            binding.btnFahrenheit.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.unit_toggle_text_inactive)
            )
        } else {
            binding.btnFahrenheit.setBackgroundResource(R.drawable.bg_unit_toggle_active)
            binding.btnFahrenheit.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.unit_toggle_text_active)
            )
            binding.btnCelsius.setBackgroundColor(
                ContextCompat.getColor(requireContext(), android.R.color.transparent)
            )
            binding.btnCelsius.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.unit_toggle_text_inactive)
            )
        }
    }

    private fun bindWeatherData(data: WeatherResponse) {
        val isFahrenheit = viewModel.tempUnit.value == TemperatureUnit.FAHRENHEIT
        val unitSymbol = if (isFahrenheit) "°F" else "°C"

        // 1. Thẻ Vị trí
        val country = data.sys?.country ?: "VN"
        binding.tvLocationName.text = "${data.cityName}, $country"
        val timeFormatted = WeatherIconUtil.formatTime(data.timestamp, data.timezone)
        binding.tvLastUpdated.text = "🕒 Cập nhật lúc $timeFormatted"

        // 2. Thẻ Thời tiết chính (Hero Card)
        val condition = data.weatherList?.firstOrNull()
        val descVi = condition?.let { WeatherIconUtil.getWeatherDescription(it.id) } ?: "Quang đãng"
        val descEn = condition?.description?.replaceFirstChar { it.uppercase() } ?: "Clear Sky"
        binding.tvConditionDescription.text = "$descVi • $descEn"

        // Tải Icon OpenWeatherMap CDN qua Glide
        val iconCode = condition?.icon ?: "01d"
        Glide.with(this)
            .load(WeatherIconUtil.getIconUrl4x(iconCode))
            .placeholder(WeatherIconUtil.getLocalDrawableForIcon(iconCode))
            .error(WeatherIconUtil.getLocalDrawableForIcon(iconCode))
            .into(binding.ivWeatherMainIcon)

        // Nhiệt độ chính & Cảm giác như
        val main = data.main
        if (main != null) {
            val currentTemp = if (isFahrenheit) {
                WeatherIconUtil.celsiusToFahrenheit(main.temp)
            } else {
                main.temp
            }
            binding.tvMainTemp.text = Math.round(currentTemp).toString()
            binding.tvMainTempUnit.text = unitSymbol

            val feelsLikeTemp = if (isFahrenheit) {
                WeatherIconUtil.celsiusToFahrenheit(main.feelsLike)
            } else {
                main.feelsLike
            }
            binding.tvFeelsLike.text = "Cảm giác như ${Math.round(feelsLikeTemp)}$unitSymbol"

            val tempMax = if (isFahrenheit) {
                WeatherIconUtil.celsiusToFahrenheit(main.tempMax)
            } else {
                main.tempMax
            }
            val tempMin = if (isFahrenheit) {
                WeatherIconUtil.celsiusToFahrenheit(main.tempMin)
            } else {
                main.tempMin
            }
            binding.tvTempHigh.text = "↑ Cao: ${Math.round(tempMax)}°"
            binding.tvTempLow.text = "↓ Thấp: ${Math.round(tempMin)}°"

            // 3. Telemetry - Độ ẩm & Điểm sương
            binding.tvHumidityVal.text = "${main.humidity}%"
            binding.pbHumidity.progress = main.humidity
            val dewPoint = WeatherIconUtil.calculateDewPoint(main.temp, main.humidity)
            val dewPointDisplay = if (isFahrenheit) {
                Math.round(WeatherIconUtil.celsiusToFahrenheit(dewPoint.toDouble())).toInt()
            } else {
                dewPoint
            }
            binding.tvDewPoint.text = "Điểm sương: $dewPointDisplay$unitSymbol"

            // 4. Telemetry - Áp suất
            binding.tvPressureVal.text = "${main.pressure} hPa"
            val atm = main.pressure / 1013.25
            binding.tvPressureAtmBadge.text = String.format(Locale.US, "%.2f atm", atm)
        }

        // 5. Telemetry - Gió
        val wind = data.wind
        if (wind != null) {
            binding.tvWindSpeedVal.text = "${wind.speed} m/s"
            binding.tvWindDirectionDegree.text = "↗ ${WeatherIconUtil.getWindDirectionShort(wind.deg)}"
        }

        // 6. Telemetry - Tầm nhìn
        val visibilityMeters = data.visibility ?: 10000
        val visKm = visibilityMeters / 1000.0
        binding.tvVisibilityVal.text = if (visKm >= 10.0) "${visKm.toInt()} km" else String.format(Locale.US, "%.1f km", visKm)
        val (visDesc, visBadge) = WeatherIconUtil.getVisibilityEvaluation(visibilityMeters)
        binding.tvVisibilityDesc.text = visDesc
        binding.tvVisibilityBadge.text = visBadge

        // 7. Mặt trời & Chu kỳ ngày
        val sys = data.sys
        if (sys != null) {
            val sunriseTime = WeatherIconUtil.formatTime(sys.sunrise, data.timezone)
            val sunsetTime = WeatherIconUtil.formatTime(sys.sunset, data.timezone)
            binding.tvSunriseTime.text = sunriseTime
            binding.tvSunsetTime.text = sunsetTime
            binding.tvDaylightDuration.text = WeatherIconUtil.formatDaylightDuration(sys.sunrise, sys.sunset)

            // Tiến độ mặt trời
            val nowSeconds = System.currentTimeMillis() / 1000L
            val progress = if (sys.sunset > sys.sunrise && nowSeconds in sys.sunrise..sys.sunset) {
                (nowSeconds - sys.sunrise).toFloat() / (sys.sunset - sys.sunrise).toFloat()
            } else if (nowSeconds >= sys.sunset) {
                1.0f
            } else {
                0.0f
            }
            binding.sunPathView.setSunProgress(progress)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
