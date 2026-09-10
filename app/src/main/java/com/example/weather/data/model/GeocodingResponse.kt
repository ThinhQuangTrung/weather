package com.example.weather.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model phan hoi tu OpenWeatherMap Geocoding API
 * Dung de tim kiem thanh pho theo ten va lay toa do chinh xac
 */
data class GeocodingItem(
    @SerializedName("name") val name: String,
    @SerializedName("local_names") val localNames: Map<String, String>?,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("country") val country: String,
    @SerializedName("state") val state: String?
) {
    /**
     * Ten hien thi day du gom ten thanh pho, tinh/bang  (neu co), va quoc gia
     */
    val displayName: String
        get() = buildString {
            append(name)
            if (!state.isNullOrBlank()) append(", $state")
            if (country.isNotBlank()) append(", $country")
        }
}
