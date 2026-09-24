package com.example.weather

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class bắt buộc cho Hilt DI.
 * @HiltAndroidApp kích hoạt code generation của Hilt cho toàn bộ app.
 */
@HiltAndroidApp
class WeatherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
    }
}
