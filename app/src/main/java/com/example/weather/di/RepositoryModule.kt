package com.example.weather.di

import android.content.Context
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.data.repository.PreferenceRepositoryImpl
import com.example.weather.data.repository.WeatherRepositoryImpl
import com.example.weather.domain.repository.PreferenceRepository
import com.example.weather.domain.repository.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        impl: WeatherRepositoryImpl
    ): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindPreferenceRepository(
        impl: PreferenceRepositoryImpl
    ): PreferenceRepository

    companion object {
        @Provides
        @Singleton
        fun provideWeatherPreferenceManager(
            @ApplicationContext context: Context
        ): WeatherPreferenceManager {
            return WeatherPreferenceManager(context)
        }
    }
}
