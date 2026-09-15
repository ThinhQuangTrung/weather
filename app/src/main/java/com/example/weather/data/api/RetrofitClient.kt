package com.example.weather.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitClient {

    private const val BASE_URL = "https://api.openweathermap.org/"//

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)// cơ chế cho phép chăn chỉnh sửa hoặc ghi log
        .connectTimeout(15, TimeUnit.SECONDS)// thời gian tối đa để thiết kế vs server
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {// by lazy chưa tao luon chỉ khi nào retrofit đc sư dung vs tạo
        Retrofit.Builder()// bắt đầu xay dựng nó
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val weatherApiService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java) //kết nối vs interface API
    }
}
