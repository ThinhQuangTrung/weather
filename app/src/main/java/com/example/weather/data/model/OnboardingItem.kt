package com.example.weather.data.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

/**
 * Model chứa dữ liệu hiển thị cho các thẻ tính năng giới thiệu (Onboarding Carousel Cards)
 */
data class OnboardingItem(
    @param:DrawableRes val iconRes: Int,
    @param:ColorRes val iconTintRes: Int,
    @param:ColorRes val iconBgRes: Int,
    val tagText: String,
    @param:ColorRes val tagBgRes: Int,
    @param:ColorRes val tagTextColorRes: Int,
    val title: String,
    val description: String,
    @param:DrawableRes val stat1Icon: Int,
    val stat1Text: String,
    @param:DrawableRes val stat2Icon: Int,
    val stat2Text: String,
    @param:DrawableRes val stat3Icon: Int,
    val stat3Text: String,
    @param:ColorRes val statColorRes: Int
)
