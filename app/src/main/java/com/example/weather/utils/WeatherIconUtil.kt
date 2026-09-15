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
    fun getWeatherDescription(conditionCode: Int): Int {
        return when (conditionCode) {
            in 200..232 -> R.string.weather_thunderstorm
            in 300..321 -> R.string.weather_drizzle
            500 -> R.string.weather_light_rain
            501 -> R.string.weather_moderate_rain
            in 502..504 -> R.string.weather_heavy_rain
            511 -> R.string.weather_freezing_rain
            in 520..531 -> R.string.weather_shower_rain
            in 600..622 -> R.string.weather_snow
            701, 741 -> R.string.weather_fog
            711 -> R.string.weather_smoke
            721 -> R.string.weather_haze
            731, 751, 761 -> R.string.weather_dust
            781 -> R.string.weather_tornado
            800 -> R.string.weather_clear_sky
            801 -> R.string.weather_few_clouds
            802 -> R.string.weather_scattered_clouds
            803, 804 -> R.string.weather_broken_clouds
            else -> R.string.weather_default
        }
    }


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
    fun getVisibilityEvaluation(visibilityMeters: Int): Pair<Int, Int> {
        val km = visibilityMeters / 1000.0

        return when {
            km >= 10 -> Pair(
                R.string.visibility_very_good,
                R.string.visibility_clear
            )

            km >= 5 -> Pair(
                R.string.visibility_good,
                R.string.visibility_normal
            )

            km >= 2 -> Pair(
                R.string.visibility_moderate,
                R.string.visibility_hazy
            )

            else -> Pair(
                R.string.visibility_limited,
                R.string.visibility_foggy
            )
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
     * Đánh giá độ che phủ của mây (%)
     * Trả về (Mô tả, Huy hiệu ngắn)
     */
    fun getCloudCoverEvaluation(cloudinessPercent: Int): Pair<Int, Int> {
        return when (cloudinessPercent) {
            in 0..10 -> Pair(
                R.string.cloud_clear_sky,
                R.string.cloud_clear
            )

            in 11..25 -> Pair(
                R.string.cloud_few_clouds_description,
                R.string.cloud_few_clouds
            )

            in 26..50 -> Pair(
                R.string.cloud_scattered_description,
                R.string.cloud_scattered_clouds
            )

            in 51..84 -> Pair(
                R.string.cloud_many_description,
                R.string.cloud_many
            )

            else -> Pair(
                R.string.cloud_overcast_description,
                R.string.cloud_overcast
            )
        }
    }

    /**
     * Chuyển đổi nhiệt độ C sang F
     */
    fun celsiusToFahrenheit(c: Double): Double = (c * 9.0 / 5.0) + 32.0
}
