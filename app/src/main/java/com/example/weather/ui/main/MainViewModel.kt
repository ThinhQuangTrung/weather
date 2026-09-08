package com.example.weather.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.weather.R
import com.example.weather.data.model.OnboardingItem
import com.example.weather.data.model.WeatherResponse
import com.example.weather.data.repository.WeatherRepository
import com.example.weather.utils.Resource
import kotlinx.coroutines.launch

/**
 * ViewModel trong mô hình MVVM:
 * - Lưu giữ và quản lý State (Trạng thái UI, Danh sách onboarding, Thời tiết thời gian thực).
 * - Sống sót qua các sự kiện cấu hình (Configuration changes như xoay màn hình).
 * - Giao tiếp với Repository để tải dữ liệu bất đồng bộ qua Coroutines.
 */
class MainViewModel @JvmOverloads constructor(
    application: Application,
    private val weatherRepository: WeatherRepository = WeatherRepository()
) : AndroidViewModel(application) {

    // Danh sách các mục onboarding
    private val _onboardingItems = MutableLiveData<List<OnboardingItem>>()
    val onboardingItems: LiveData<List<OnboardingItem>> = _onboardingItems

    // Vị trí trang hiện tại
    private val _currentPage = MutableLiveData<Int>(0)
    val currentPage: LiveData<Int> = _currentPage

    // Trạng thái dữ liệu thời tiết thực từ OpenWeatherMap API
    private val _weatherState = MutableLiveData<Resource<WeatherResponse>>()
    val weatherState: LiveData<Resource<WeatherResponse>> = _weatherState

    init {
        loadOnboardingData()
    }

    private fun loadOnboardingData() {
        val context = getApplication<Application>()
        val items = listOf(
            OnboardingItem(
                iconRes = R.drawable.ic_thermometer,
                iconTintRes = R.color.badge_blue_icon,
                iconBgRes = R.color.badge_blue_bg,
                tagText = context.getString(R.string.card1_tag),
                tagBgRes = R.color.tag_gray_bg,
                tagTextColorRes = R.color.tag_gray_text,
                title = context.getString(R.string.card1_title),
                description = context.getString(R.string.card1_desc),
                stat1Icon = R.drawable.ic_wind,
                stat1Text = context.getString(R.string.card1_stat1),
                stat2Icon = R.drawable.ic_humidity,
                stat2Text = context.getString(R.string.card1_stat2),
                stat3Icon = R.drawable.ic_uv,
                stat3Text = context.getString(R.string.card1_stat3),
                statColorRes = R.color.stat_primary
            ),
            OnboardingItem(
                iconRes = R.drawable.ic_trend_up,
                iconTintRes = R.color.badge_orange_icon,
                iconBgRes = R.color.badge_orange_bg,
                tagText = context.getString(R.string.card2_tag),
                tagBgRes = R.color.tag_orange_bg,
                tagTextColorRes = R.color.tag_orange_text,
                title = context.getString(R.string.card2_title),
                description = context.getString(R.string.card2_desc),
                stat1Icon = R.drawable.ic_clock,
                stat1Text = context.getString(R.string.card2_stat1),
                stat2Icon = R.drawable.ic_trend_up,
                stat2Text = context.getString(R.string.card2_stat2),
                stat3Icon = R.drawable.ic_humidity,
                stat3Text = context.getString(R.string.card2_stat3),
                statColorRes = R.color.stat_secondary
            ),
            OnboardingItem(
                iconRes = R.drawable.ic_location,
                iconTintRes = R.color.badge_teal_icon,
                iconBgRes = R.color.badge_teal_bg,
                tagText = context.getString(R.string.card3_tag),
                tagBgRes = R.color.tag_teal_bg,
                tagTextColorRes = R.color.tag_teal_text,
                title = context.getString(R.string.card3_title),
                description = context.getString(R.string.card3_desc),
                stat1Icon = R.drawable.ic_location,
                stat1Text = context.getString(R.string.card3_stat1),
                stat2Icon = R.drawable.ic_clock,
                stat2Text = context.getString(R.string.card3_stat2),
                stat3Icon = R.drawable.ic_wind,
                stat3Text = context.getString(R.string.card3_stat3),
                statColorRes = R.color.stat_teal
            )
        )
        _onboardingItems.value = items
    }

    /**
     * Cập nhật vị trí trang hiện tại
     */
    fun onPageChanged(position: Int) {
        if (_currentPage.value != position) {
            _currentPage.value = position
        }
    }

    /**
     * Gọi API OpenWeatherMap để tải thông tin thời tiết thực tế
     */
    fun fetchWeather(cityName: String = "Hanoi") {
        viewModelScope.launch {
            _weatherState.value = Resource.Loading
            val result = weatherRepository.getCurrentWeather(cityName)
            _weatherState.value = result
        }
    }
}
