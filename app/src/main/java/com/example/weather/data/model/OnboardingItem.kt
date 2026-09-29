package com.example.weather.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Model dữ liệu cho mỗi trang onboarding.
 */
data class OnboardingItem(
    @param:StringRes val titleRes: Int,
    @param:StringRes val descriptionRes: Int,
    /** Ảnh minh hoạ cho trang */
    @param:DrawableRes val imageRes: Int,
    /** Dot indicator SVG theo trang (ic_dot_onboarding_1..4) */
    @param:DrawableRes val dotRes: Int
)
