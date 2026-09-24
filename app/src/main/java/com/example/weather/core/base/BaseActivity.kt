package com.example.weather.core.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.weather.data.preference.WeatherPreferenceManager

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Áp dụng theme TRƯỚC super.onCreate() để Android inflate đúng theme ngay từ đầu,
        // tránh nhấp nháy giao diện khi Activity được tạo lại.
        applyThemeFromPreferences()
        super.onCreate(savedInstanceState)
        hideSystemBars()
    }

    /**
     * Đọc theme đã lưu từ Data Layer và áp dụng toàn cục qua AppCompatDelegate.
     * BaseActivity là nơi hợp lý vì tất cả màn hình đều kế thừa nó,
     * và theme cần được set trước khi window được tạo.
     */
    private fun applyThemeFromPreferences() {
        val themeMode = WeatherPreferenceManager(this).themeMode
        val nightMode = when (themeMode) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark"  -> AppCompatDelegate.MODE_NIGHT_YES
            else    -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun hideSystemBars() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}