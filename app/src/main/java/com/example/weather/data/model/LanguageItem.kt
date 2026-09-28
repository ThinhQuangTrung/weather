package com.example.weather.data.model

/**
 * Model biểu diễn mục ngôn ngữ trong danh sách chọn ngôn ngữ
 */
data class LanguageItem(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flagAssetPath: String,
)
