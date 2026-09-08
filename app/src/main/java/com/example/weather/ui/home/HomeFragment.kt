package com.example.weather.ui.home

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.weather.R
import com.example.weather.data.location.LocationManager
import com.example.weather.data.model.WeatherResponse
import com.example.weather.databinding.FragmentHomeBinding
import com.example.weather.utils.Resource
import com.example.weather.utils.WeatherIconUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

/**
 * - Kết nối dữ liệu với HomeViewModel và cập nhật giao diện theo LiveData.
 * - Quản lý việc yêu cầu cấp quyền vị trí khi khởi chạy và xử lý kết quả cấp quyền.
 * - Hiển thị hộp thoại nổi dẫn trực tiếp vào Cài đặt khi chưa được cấp quyền hoặc GPS tắt.
 * - Hiển thị toàn bộ thông số thời tiết chi tiết, nạp icon động từ OpenWeatherMap CDN qua Glide.
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private var activeDialog: AlertDialog? = null

    /**
     * Launcher xin quyền vị trí chuẩn Android Jetpack Activity Result API
     */
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

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

        // Kiểm tra và xin cấp quyền vị trí ngay khi vào màn hình thời tiết
        checkAndRequestLocationPermission()
    }

    override fun onResume() {
        super.onResume()
        // Tự động làm mới thời tiết theo GPS khi người dùng cấp quyền/bật GPS từ Cài đặt trở về app
        if (LocationManager.hasLocationPermission(requireContext()) && !viewModel.hasCurrentCoordinates()) {
            viewModel.fetchWeatherByCurrentLocation()
        }
    }

    /**
     * Kiểm tra quyền vị trí và yêu cầu cấp quyền nếu chưa có
     */
    private fun checkAndRequestLocationPermission() {
        if (LocationManager.hasLocationPermission(requireContext())) {
            viewModel.fetchWeatherByCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
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
            openAppSettings()
        }

        // Bấm vào thẻ vị trí để cập nhật lại thời tiết theo GPS hiện tại hoặc xin quyền nếu chưa có
        binding.cardLocation.setOnClickListener {
            checkAndRequestLocationPermission()
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

        viewModel.userMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearUserMessage()
            }
        }

        // Quan sát sự kiện hiển thị thông báo nổi đi tới Cài đặt
        viewModel.showLocationDialog.observe(viewLifecycleOwner) { dialogType ->
            activeDialog?.dismiss()
            when (dialogType) {
                LocationDialogType.OPEN_APP_SETTINGS -> showAppSettingsDialog()
                LocationDialogType.ENABLE_GPS_SETTINGS -> showEnableGpsDialog()
                null -> {}
            }
        }
    }

    /**
     * Hiển thị hộp thoại nổi thông báo người dùng vào Cài đặt để cấp quyền vị trí
     */
    private fun showAppSettingsDialog() {
        activeDialog = MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_location)
            .setTitle("Yêu cầu quyền vị trí")
            .setMessage("Ứng dụng cần quyền truy cập vị trí để cung cấp thông tin thời tiết chính xác tại nơi bạn.\n\nVui lòng nhấn \"Mở Cài đặt\" để bật quyền vị trí cho ứng dụng.")
            .setCancelable(false)
            .setPositiveButton("Mở Cài đặt") { dialog, _ ->
                dialog.dismiss()
                viewModel.dismissLocationDialog()
                openAppSettings()
            }
            .setNegativeButton("Để sau") { dialog, _ ->
                dialog.dismiss()
                viewModel.dismissLocationDialog()
            }
            .show()
    }

    /**
     * Hiển thị hộp thoại nổi thông báo người dùng bật dịch vụ GPS của thiết bị
     */
    private fun showEnableGpsDialog() {
        activeDialog = MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_location)
            .setTitle("Bật định vị (GPS)")
            .setMessage("Dịch vụ định vị GPS trên thiết bị của bạn đang tắt. Hãy bật GPS để tự động cập nhật thời tiết tại vị trí hiện tại của bạn.")
            .setCancelable(false)
            .setPositiveButton("Bật GPS") { dialog, _ ->
                dialog.dismiss()
                viewModel.dismissLocationDialog()
                openGpsSettings()
            }
            .setNegativeButton("Để sau") { dialog, _ ->
                dialog.dismiss()
                viewModel.dismissLocationDialog()
            }
            .show()
    }

    /**
     * Mở trực tiếp màn hình Cài đặt ứng dụng trong hệ thống
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    /**
     * Mở trực tiếp màn hình Cài đặt Định vị (GPS) trong hệ thống
     */
    private fun openGpsSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
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

        // Icon OpenWeatherMap
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
        activeDialog?.dismiss()
        activeDialog = null
        _binding = null
    }

    companion object {
        fun newInstance() = HomeFragment()
    }
}
