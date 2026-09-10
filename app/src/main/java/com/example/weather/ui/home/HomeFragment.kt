package com.example.weather.ui.home

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
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
import androidx.viewpager2.widget.ViewPager2
import com.example.weather.R
import com.example.weather.data.location.LocationBoundService
import com.example.weather.data.location.LocationManager
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.databinding.FragmentHomeBinding
import com.example.weather.utils.Resource
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * - Bấm vào nút addCity để mở CityManagementFragment (thêm thành phố mới, chọn thành phố, nhấn giữ để xóa).
 * - Đồng bộ tức thì với cài đặt hiển thị widget và đơn vị nhiệt độ (°C / °F).
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var cityWeatherAdapter: CityWeatherPagerAdapter
    private var activeDialog: AlertDialog? = null
    private val prefManager by lazy { WeatherPreferenceManager(requireContext()) }

    // ─── Bound Service fields ────────────────────────────────────────────────
    private var locationService: LocationBoundService? = null
    private var isServiceBound = false

    /**
     * ServiceConnection: callbacks báo khi Service kết nối / mất kết nối.
     * - onServiceConnected  → Fragment đang hiện ra, lấy vị trí GPS ngay.
     * - onServiceDisconnected → Service bị kill bất thường (hiếm gặp).
     */
    private val locationServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val localBinder = binder as? LocationBoundService.LocalBinder ?: return
            locationService = localBinder.getService()
            isServiceBound = true

            // Ngay khi kết nối thành công → yêu cầu vị trí GPS hiện tại
            locationService?.requestCurrentLocation { location ->
                if (location != null) {
                    // Có vị trí → truyền cho ViewModel tải thời tiết
                    viewModel.fetchWeatherByLocation(location)
                } else {
                    // Không lấy được GPS (chưa cấp quyền, GPS tắt, …)
                    if (!LocationManager.hasLocationPermission(requireContext())) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        viewModel.fetchWeatherByCurrentLocation()
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            locationService = null
            isServiceBound = false
        }
    }
    // ────────────────────────────────────────────────────────────────────────

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
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

        setupTopHeaderListeners()
        setupViewPager()
        setupFragmentResultListeners()
        observeViewModel()

        // Khởi động dữ liệu cho thành phố đầu tiên
        initCityWeather()
    }

    private fun setupTopHeaderListeners() {
        binding.btnCelsius.setOnClickListener {
            viewModel.setTemperatureUnit(TemperatureUnit.CELSIUS)
        }

        binding.btnFahrenheit.setOnClickListener {
            viewModel.setTemperatureUnit(TemperatureUnit.FAHRENHEIT)
        }

        binding.btnSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .add(
                    R.id.fragmentContainer,
                    com.example.weather.ui.settings.SettingsFragment.newInstance(),
                    "SETTINGS"
                )
                .addToBackStack("SETTINGS")
                .commit()
        }
    }

    private fun setupViewPager() {
        val savedCities = prefManager.getSavedCities()
        val currentUnit = viewModel.tempUnit.value ?: TemperatureUnit.CELSIUS

        cityWeatherAdapter = CityWeatherPagerAdapter(
            cities = savedCities,
            prefManager = prefManager,
            tempUnit = currentUnit,
            onAddCityClick = {
                openCityManagement()
            },
            onLocationClick = { cityName, _ ->
                viewModel.refresh(cityName)
            }
        )

        binding.viewPagerCities.apply {
            adapter = cityWeatherAdapter
            offscreenPageLimit = 2

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    val cities = cityWeatherAdapter.getCities()
                    if (position in cities.indices) {
                        val city = cities[position]
                        prefManager.selectedCityIndex = position
                        updatePageIndicator(position, cities.size)
                        // Gọi API OpenWeatherMap ngay khi lướt sang thành phố này
                        viewModel.loadWeather(city)
                    }
                }
            })
        }
    }

    private fun initCityWeather() {
        val savedCities = prefManager.getSavedCities()
        val initialIndex = prefManager.selectedCityIndex.coerceIn(0, (savedCities.size - 1).coerceAtLeast(0))
        binding.viewPagerCities.setCurrentItem(initialIndex, false)
        updatePageIndicator(initialIndex, savedCities.size)

        // Hiển thị thành phố đã lưu làm fallback trong khi GPS đang được lấy
        if (initialIndex in savedCities.indices) {
            viewModel.loadWeather(savedCities[initialIndex])
        }
        // Lưu ý: việc lấy GPS hiện tại được xử lý trong onStart() qua Bound Service.
    }

    private fun openCityManagement() {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .add(
                R.id.fragmentContainer,
                CityManagementFragment.newInstance(),
                "CITY_MGMT"
            )
            .addToBackStack("CITY_MGMT")
            .commit()
    }

    private fun setupFragmentResultListeners() {
        // Lắng nghe khi người dùng chọn 1 thành phố từ màn hình Quản lý thành phố
        parentFragmentManager.setFragmentResultListener(
            CityManagementFragment.REQUEST_KEY_CITY_SELECTED,
            viewLifecycleOwner
        ) { _, bundle ->
            val useCurrentLocation = bundle.getBoolean(CityManagementFragment.KEY_USE_CURRENT_LOCATION, false)
            if (useCurrentLocation) {
                if (!LocationManager.hasLocationPermission(requireContext())) {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                } else {
                    viewModel.fetchWeatherByCurrentLocation()
                }
                return@setFragmentResultListener
            }

            val selectedIndex = bundle.getInt(CityManagementFragment.KEY_SELECTED_CITY_INDEX, 0)
            val updatedCities = prefManager.getSavedCities()
            cityWeatherAdapter.setCities(updatedCities)

            val targetIndex = selectedIndex.coerceIn(0, (updatedCities.size - 1).coerceAtLeast(0))
            binding.viewPagerCities.setCurrentItem(targetIndex, true)
            updatePageIndicator(targetIndex, updatedCities.size)

            if (targetIndex in updatedCities.indices) {
                viewModel.loadWeather(updatedCities[targetIndex])
            }
        }

        // Lắng nghe khi danh sách thành phố bị thay đổi (ví dụ đã xóa thành phố)
        parentFragmentManager.setFragmentResultListener(
            CityManagementFragment.REQUEST_KEY_CITY_CHANGED,
            viewLifecycleOwner
        ) { _, _ ->
            val updatedCities = prefManager.getSavedCities()
            cityWeatherAdapter.setCities(updatedCities)

            val currentIndex = binding.viewPagerCities.currentItem.coerceIn(0, (updatedCities.size - 1).coerceAtLeast(0))
            binding.viewPagerCities.setCurrentItem(currentIndex, false)
            updatePageIndicator(currentIndex, updatedCities.size)

            if (currentIndex in updatedCities.indices) {
                viewModel.loadWeather(updatedCities[currentIndex])
            }
        }
    }

    private fun observeViewModel() {
        // Quan sát kết quả thời tiết theo từng thành phố
        viewModel.cityWeatherResult.observe(viewLifecycleOwner) { (cityName, resource) ->
            cityWeatherAdapter.updateWeatherData(cityName, resource)
            if (resource is Resource.Error) {
                Toast.makeText(
                    requireContext(),
                    resource.message ?: "Không thể kết nối máy chủ thời tiết cho $cityName",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Quan sát thành phố xác định từ GPS hiện tại
        viewModel.locationResolvedCity.observe(viewLifecycleOwner) { resolvedCity ->
            if (!resolvedCity.isNullOrEmpty()) {
                val currentCities = prefManager.getSavedCities().toMutableList()
                var index = currentCities.indexOfFirst { it.equals(resolvedCity, ignoreCase = true) }
                if (index == -1) {
                    prefManager.addCity(resolvedCity)
                    val updated = prefManager.getSavedCities()
                    cityWeatherAdapter.setCities(updated)
                    index = updated.indexOfFirst { it.equals(resolvedCity, ignoreCase = true) }
                }
                if (index >= 0) {
                    prefManager.selectedCityIndex = index
                    binding.viewPagerCities.setCurrentItem(index, true)
                    updatePageIndicator(index, cityWeatherAdapter.itemCount)
                }
                viewModel.clearLocationResolvedCity()
            }
        }

        // Quan sát đơn vị nhiệt độ
        viewModel.tempUnit.observe(viewLifecycleOwner) { unit ->
            updateUnitToggleUI(unit)
            cityWeatherAdapter.setTempUnit(unit)
        }

        // Quan sát thông báo người dùng
        viewModel.userMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearUserMessage()
            }
        }

        // Quan sát hộp thoại quyền vị trí
        viewModel.showLocationDialog.observe(viewLifecycleOwner) { dialogType ->
            activeDialog?.dismiss()
            when (dialogType) {
                LocationDialogType.OPEN_APP_SETTINGS -> showAppSettingsDialog()
                LocationDialogType.ENABLE_GPS_SETTINGS -> showEnableGpsDialog()
                null -> {}
            }
        }
    }

    private fun updatePageIndicator(position: Int, total: Int) {
        val totalCount = if (total > 0) total else 1
        binding.tvPageIndicator.text = "${position + 1} / $totalCount"
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

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun openGpsSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        // onStart: Bind Service để lấy GPS ngay khi Fragment hiện ra
        Intent(requireContext(), LocationBoundService::class.java).also { intent ->
            requireContext().bindService(intent, locationServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        //  onStop khi hủy thì KHÔNG chạy ngầm dùng Service
        if (isServiceBound) {
            requireContext().unbindService(locationServiceConnection)
            isServiceBound = false
            locationService = null
        }
    }

    override fun onResume() {
        super.onResume()
        // Cập nhật lại giao diện widget nếu có thay đổi từ màn hình Settings
        cityWeatherAdapter.notifyDataSetChanged()
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
