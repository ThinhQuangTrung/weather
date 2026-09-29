package com.example.weather.ui.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.R
import com.example.weather.data.model.OnboardingItem
import com.example.weather.domain.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel quản lý dữ liệu và trạng thái của màn hình Onboarding theo kiến trúc Clean Architecture & MVVM.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {

    private val _onboardingItems = MutableLiveData<List<OnboardingItem>>()
    val onboardingItems: LiveData<List<OnboardingItem>> = _onboardingItems

    private val _currentPage = MutableLiveData(0)
    val currentPage: LiveData<Int> = _currentPage

    private val _navigateToSetup = MutableLiveData<Boolean>()
    val navigateToSetup: LiveData<Boolean> = _navigateToSetup

    init {
        loadOnboardingData()
    }

    private fun loadOnboardingData() {
        val items = listOf(
            OnboardingItem(
                titleRes = R.string.card1_title,
                descriptionRes = R.string.card1_desc,
                imageRes = R.drawable.img,
                dotRes = R.drawable.ic_dot_onboarding_1
            ),
            OnboardingItem(
                titleRes = R.string.card2_title,
                descriptionRes = R.string.card2_desc,
                imageRes = R.drawable.img_4,
                dotRes = R.drawable.ic_dot_onboarding_2
            ),
            OnboardingItem(
                titleRes = R.string.card3_title,
                descriptionRes = R.string.card3_desc,
                imageRes = R.drawable.img_1,
                dotRes = R.drawable.ic_dot_onboarding_3
            ),
            OnboardingItem(
                titleRes = R.string.card4_title,
                descriptionRes = R.string.card4_desc,
                imageRes = R.drawable.img_2,
                dotRes = R.drawable.ic_dot_onboarding_4
            )
        )
        _onboardingItems.value = items
    }

    fun onPageChanged(position: Int) {
        if (_currentPage.value != position) {
            _currentPage.value = position
        }
    }

    fun completeOnboarding() {
        preferenceRepository.isOnboardingCompleted = true
        _navigateToSetup.value = true
    }
}
