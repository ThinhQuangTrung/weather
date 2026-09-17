package com.example.weather

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class bắt buộc cho Hilt DI.
 * @HiltAndroidApp kích hoạt code generation của Hilt cho toàn bộ app.
 */
@HiltAndroidApp
class WeatherApplication : Application()
