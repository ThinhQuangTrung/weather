package com.example.weather.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.weather.MainActivity
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.databinding.ActivitySplashBinding
import com.example.weather.ui.home.HomeActivity
import com.example.weather.ui.language.LanguageActivity
import com.example.weather.ui.setup.WeatherSetupActivity
import com.example.weather.utils.LocaleHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val prefManager by lazy { WeatherPreferenceManager(this) }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hiệu ứng nhẹ logo splash
        binding.ivSplashLogo.alpha = 0f
        binding.ivSplashLogo.scaleX = 0.8f
        binding.ivSplashLogo.scaleY = 0.8f
        binding.ivSplashLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .start()

        lifecycleScope.launch {
            delay(1000)
            navigateNextScreen()
        }
    }

    private fun navigateNextScreen() {
        val nextIntent = when {
            // Bước 2: Chưa chọn ngôn ngữ -> Mở màn hình Language
            !prefManager.isLanguageSelected -> {
                Intent(this, LanguageActivity::class.java)
            }
            // Bước 3: Chưa hoàn thành Onboarding -> Mở màn hình Onboarding (MainActivity)
            !prefManager.isOnboardingCompleted -> {
                Intent(this, MainActivity::class.java)
            }
            // Bước 4: Chưa thiết lập thông số thời tiết -> Mở màn hình Thiết lập thời tiết
            !prefManager.isWeatherSetupCompleted -> {
                Intent(this, WeatherSetupActivity::class.java)
            }
            // Bước 5: Đã hoàn tất toàn bộ -> Vào thẳng màn hình Home
            else -> {
                Intent(this, HomeActivity::class.java)
            }
        }

        startActivity(nextIntent)
        finish()
    }
}
