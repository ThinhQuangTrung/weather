package com.example.weather.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.weather.MainActivity
import com.example.weather.ads.AppOpenAdManager
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.databinding.ActivitySplashBinding
import com.example.weather.core.base.BaseActivity
import com.example.weather.ui.home.HomeActivity
import com.example.weather.ui.language.LanguageActivity
import com.example.weather.ui.setup.WeatherSetupActivity
import com.example.weather.utils.ICallBackItem
import com.example.weather.utils.LocaleHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.weather.R
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val prefManager by lazy { WeatherPreferenceManager(this) }
    private val appOpenAdManager by lazy { AppOpenAdManager() }
    private var isNavigated = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSplashAnimation()
        setupLoading()

        checkFirstLaunch()
    }

    private fun setupSplashAnimation() {
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
    }

    private fun setupLoading() {
        binding.vLoading.onProgress = object : ICallBackItem {
            override fun callBack(ob: Any?, position: Int) {
                binding.tvProgress.text = "Loading (${position}%)..."
                if (position >= 99 && !isNavigated) {
                    navigateNextScreen()
                }
            }
        }
    }

    private fun checkFirstLaunch() {
        showAppOpenAd()
    }

    private fun showAppOpenAd() {
        appOpenAdManager.loadAd(this) {

            appOpenAdManager.showAdIfAvailable(this) {

                navigateNextScreen()
            }
        }
    }

    private fun navigateNextScreen() {
        if (isNavigated || isFinishing || isDestroyed) return
        isNavigated = true

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
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }

        startActivity(nextIntent)
        finish()
    }
}
