package com.example.weather.utils

/**
 * Lớp Sealed Class đóng gói trạng thái dữ liệu (Loading, Success, Error)
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
