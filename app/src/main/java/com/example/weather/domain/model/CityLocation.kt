package com.example.weather.domain.model

/**
 * Domain Entity cho kết quả tìm kiếm thành phố (Geocoding).
 * Thay thế GeocodingItem ở tầng data, không có @SerializedName.
 */
data class CityLocation(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String?
) {
    /** Tên hiển thị đầy đủ (ví dụ: "Hà Nội, Hanoi, VN") */
    val displayName: String
        get() = buildString {
            append(name)
            if (!state.isNullOrBlank()) append(", $state")
            if (country.isNotBlank()) append(", $country")
        }

    /** Thông tin địa điểm phụ (ví dụ: "Hanoi, VN") */
    val locationDetail: String
        get() = buildString {
            if (!state.isNullOrBlank()) append(state)
            if (country.isNotBlank()) {
                if (isNotEmpty()) append(", ")
                append(country)
            }
        }
}
