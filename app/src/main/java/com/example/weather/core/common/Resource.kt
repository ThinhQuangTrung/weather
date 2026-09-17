package com.example.weather.core.common

/**
 * Sealed class bao gói trạng thái dữ liệu (Loading, Success, Error).
 * Dùng chung cho toàn bộ các tầng của app.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>() // đây là trạng thái thành công
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()// trạng thái thất bại /lỗi error
    object Loading : Resource<Nothing>()//trang thái tải
}
