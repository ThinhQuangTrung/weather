package com.example.weather.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO ánh xạ dữ liệu phản hồi từ OpenWeatherMap Geocoding API.
 * Dùng để tìm kiếm thành phố theo tên và lấy tọa độ chính xác.
 */
data class GeocodingItemDto(
    @SerializedName("name") val name: String,
    @SerializedName("local_names") val localNames: Map<String, String>?,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("country") val country: String,
    @SerializedName("state") val state: String?
)
