package com.example.weather.core.common

/**
 * Enum loại dialog quyền vị trí.
 * Di chuyển từ HomeViewModel ra core để dễ tái sử dụng.
 */
enum class LocationDialogType {
    OPEN_APP_SETTINGS,  // Dẫn vào Cài đặt ứng dụng để cấp quyền
    ENABLE_GPS_SETTINGS // Dẫn vào Cài đặt GPS để bật GPS
}
