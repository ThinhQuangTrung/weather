package com.example.weather.data.model

import androidx.annotation.DrawableRes

/**
 * Model biểu diễn mục ngôn ngữ trong danh sách chọn ngôn ngữ
 */
data class LanguageItem(
    val code: String,
    val displayName: String,
    val nativeName: String,
    @param:DrawableRes val flagRes: Int,
    val isDefault: Boolean = false
)
