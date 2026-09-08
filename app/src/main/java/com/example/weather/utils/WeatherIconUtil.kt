package com.example.weather.utils

import com.example.weather.R

/**
 * Tiện ích xử lý Weather Icons & Condition Codes từ OpenWeatherMap:
 * - Cung cấp URL tải ảnh icon động trực tiếp từ CDN OpenWeatherMap (@2x, @4x).
 * - Cung cấp ánh xạ mã icon/mã thời tiết OpenWeatherMap sang Vector Drawable cục bộ khi cần offline/fallback.
 */
object WeatherIconUtil {

    private const val BASE_ICON_URL = "https://openweathermap.org/img/wn/"

    /**
     * Lấy URL ảnh icon chuẩn từ OpenWeatherMap (độ phân giải 2x).
     * Ví dụ: 01d -> https://openweathermap.org/img/wn/01d@2x.png
     */
    fun getIconUrl(iconCode: String?): String {
        val code = if (iconCode.isNullOrBlank()) "01d" else iconCode
        return "$BASE_ICON_URL$code@2x.png"
    }

    /**
     * Lấy URL ảnh icon độ nét cao (4x) cho màn hình chi tiết.
     */
    fun getIconUrl4x(iconCode: String?): String {
        val code = if (iconCode.isNullOrBlank()) "01d" else iconCode
        return "$BASE_ICON_URL$code@4x.png"
    }

    /**
     * Ánh xạ mã icon OpenWeather sang Vector Drawable cục bộ tương ứng trong app.
     * Hỗ trợ các mã:
     * 01d/01n (Clear sky), 02d/02n (Few clouds), 03d/03n (Scattered), 04d/04n (Broken),
     * 09d/09n (Shower rain), 10d/10n (Rain), 11d/11n (Thunderstorm), 13d/13n (Snow), 50d/50n (Mist).
     */
    fun getLocalDrawableForIcon(iconCode: String?): Int {
        return when (iconCode) {
            "01d", "01n" -> R.drawable.ic_uv
            "02d", "02n", "03d", "03n", "04d", "04n" -> R.drawable.ic_weather_illustration
            "09d", "09n", "10d", "10n", "11d", "11n" -> R.drawable.ic_humidity
            "13d", "13n" -> R.drawable.ic_humidity
            "50d", "50n" -> R.drawable.ic_wind
            else -> R.drawable.ic_weather_illustration
        }
    }

    /**
     * Chuyển đổi mã trạng thái thời tiết OpenWeather (Weather Condition Codes: 2xx, 3xx, 5xx, 6xx, 7xx, 800, 80x)
     * sang chuỗi mô tả tiếng Việt thân thiện.
     */
    fun getWeatherDescription(conditionCode: Int): String {
        return when (conditionCode) {
            in 200..232 -> "Giông bão kèm sấm sét"
            in 300..321 -> "Mưa phùn nhẹ"
            500 -> "Mưa nhẹ"
            501 -> "Mưa vừa"
            in 502..504 -> "Mưa to đến rất to"
            511 -> "Mưa băng tuyết"
            in 520..531 -> "Mưa rào từng đợt"
            in 600..622 -> "Tuyết rơi"
            701, 741 -> "Sương mù"
            711 -> "Khói bụi"
            721 -> "Mù sương khô"
            731, 751, 761 -> "Cát bụi"
            781 -> "Lốc xoáy"
            800 -> "Quang đãng"
            801 -> "Ít mây"
            802 -> "Mây rải rác"
            803, 804 -> "Trời nhiều mây"
            else -> "Thời tiết ổn định"
        }
    }

    /**
     * Chuyển đổi độ góc gió thành chuỗi hướng gió tiếng Việt
     */
    fun getWindDirectionText(deg: Int): String {
        val directions = arrayOf(
            "Hướng Bắc", "Hướng Đông Bắc", "Hướng Đông", "Hướng Đông - Đông Nam",
            "Hướng Nam", "Hướng Tây Nam", "Hướng Tây", "Hướng Tây Bắc"
        )
        val normalized = ((deg % 360) + 360) % 360
        val index = (((normalized + 22.5) / 45).toInt()) % 8
        return directions[index]
    }

    /**
     * Chuyển đổi độ góc gió thành ký hiệu quốc tế ngắn (ví dụ: 120° ESE)
     */
    fun getWindDirectionShort(deg: Int): String {
        val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = (((deg % 360) + 11.25) / 22.5).toInt() % 16
        return "$deg° ${directions[index]}"
    }

    /**
     * Tính toán điểm sương (Dew Point) ước tính theo công thức Magnus-Tetens
     */
    fun calculateDewPoint(tempCelsius: Double, humidityPercent: Int): Int {
        val a = 17.27
        val b = 237.7
        val alpha = ((a * tempCelsius) / (b + tempCelsius)) + Math.log(humidityPercent.toDouble() / 100.0)
        val dewPoint = (b * alpha) / (a - alpha)
        return Math.round(dewPoint).toInt()
    }

    /**
     * Đánh giá chất lượng tầm nhìn
     */
    fun getVisibilityEvaluation(visibilityMeters: Int): Pair<String, String> {
        val km = visibilityMeters / 1000.0
        return when {
            km >= 10 -> Pair("Tầm nhìn rất tốt", "Rõ nét")
            km >= 5 -> Pair("Tầm nhìn tốt", "Bình thường")
            km >= 2 -> Pair("Tầm nhìn trung bình", "Hơi mờ")
            else -> Pair("Tầm nhìn hạn chế", "Mù sương")
        }
    }

    /**
     * Định dạng giờ từ timestamp Unix (giây) theo định dạng AM/PM
     */
    fun formatTime(timestampSeconds: Long, timezoneOffsetSeconds: Int = 0): String {
        val date = java.util.Date((timestampSeconds + timezoneOffsetSeconds) * 1000L)
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    /**
     * Tính thời gian chiếu sáng (Daylight duration)
     */
    fun formatDaylightDuration(sunriseSec: Long, sunsetSec: Long): String {
        val diff = sunsetSec - sunriseSec
        if (diff <= 0) return "--"
        val hours = diff / 3600
        val minutes = (diff % 3600) / 60
        return "${hours}h ${minutes}m"
    }

    /**
     * Chuyển đổi nhiệt độ C sang F
     */
    fun celsiusToFahrenheit(c: Double): Double = (c * 9.0 / 5.0) + 32.0
}
