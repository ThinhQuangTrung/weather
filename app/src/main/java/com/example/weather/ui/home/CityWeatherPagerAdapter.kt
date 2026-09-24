package com.example.weather.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weather.R
import com.example.weather.core.common.Resource
import com.example.weather.core.common.TemperatureUnit
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.databinding.ItemHomeWeatherPageBinding
import com.example.weather.domain.model.CurrentWeather
import com.example.weather.utils.WeatherIconUtil
import com.airbnb.lottie.LottieAnimationView
import java.util.Locale

class CityWeatherPagerAdapter(
    private var cities: List<String>,
    private val prefManager: WeatherPreferenceManager,
    private var tempUnit: TemperatureUnit,
    private val onAddCityClick: () -> Unit,
    private val onLocationClick: (String, Int) -> Unit,
    private val onFavoriteClick: (String, Int) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<CityWeatherPagerAdapter.WeatherPageViewHolder>() {

    private val weatherMap = mutableMapOf<String, Resource<CurrentWeather>>()

    fun setCities(newCities: List<String>) {
        this.cities = newCities
        notifyDataSetChanged()
    }

    fun getCities(): List<String> = cities

    fun setTempUnit(unit: TemperatureUnit) {
        this.tempUnit = unit
        notifyDataSetChanged()
    }

    fun updateWeatherData(cityName: String, resource: Resource<CurrentWeather>) {
        weatherMap[cityName.lowercase()] = resource
        if (resource is Resource.Success) {
            weatherMap[resource.data.cityName.lowercase()] = resource
        }
        val index = cities.indexOfFirst {
            it.equals(cityName, ignoreCase = true) ||
                    (resource is Resource.Success && it.equals(resource.data.cityName, ignoreCase = true))
        }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherPageViewHolder {
        val binding = ItemHomeWeatherPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WeatherPageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeatherPageViewHolder, position: Int) {
        val cityName = cities[position]
        val resource = weatherMap[cityName.lowercase()]
        holder.bind(cityName, position, resource, tempUnit, prefManager)
    }

    override fun getItemCount(): Int = cities.size

    inner class WeatherPageViewHolder(val binding: ItemHomeWeatherPageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            cityName: String,
            position: Int,
            resource: Resource<CurrentWeather>?,
            unit: TemperatureUnit,
            pref: WeatherPreferenceManager
        ) {
            val context = binding.root.context
            val isFahrenheit = unit == TemperatureUnit.FAHRENHEIT
            val unitSymbol = if (isFahrenheit) "°F" else "°C"

            // Widget Visibility Settings
            val showTemp = pref.showTemperature
            val showHumidity = pref.showHumidity
            val showWind = pref.showWind
            val showVisibility = pref.showVisibility
            val showPressure = pref.showPressure
            val showAqi = pref.showAirQuality

            binding.cardHeroWeather.visibility = if (showTemp) View.VISIBLE else View.GONE
            binding.cardHumidity.visibility = if (showHumidity) View.VISIBLE else View.GONE
            binding.cardWind.visibility = if (showWind) View.VISIBLE else View.GONE
            binding.layoutTelemetryRow1.visibility = if (showHumidity || showWind) View.VISIBLE else View.GONE
            binding.cardVisibility.visibility = if (showVisibility) View.VISIBLE else View.GONE
            binding.cardPressure.visibility = if (showPressure) View.VISIBLE else View.GONE
            binding.layoutTelemetryRow2.visibility = if (showVisibility || showPressure) View.VISIBLE else View.GONE
            binding.cardAirQuality.visibility = if (showAqi) View.VISIBLE else View.GONE

            val hasAnyTelemetry = showHumidity || showWind || showVisibility || showPressure || showAqi
            binding.tvTelemetrySectionTitle.visibility = if (hasAnyTelemetry) View.VISIBLE else View.GONE

            // Favorite button state & click listener
            val isFav = pref.isFavoriteCity(cityName)
            val favTint = if (isFav) {
                android.graphics.Color.parseColor("#EF4444")
            } else {
                androidx.core.content.ContextCompat.getColor(context, R.color.brand_section_title)
            }
            binding.btnFavorite.setColorFilter(favTint)
            binding.btnFavorite.setOnClickListener {
                onFavoriteClick(cityName, position)
            }

            // Click listener for Add City button
            binding.addCity.setOnClickListener {
                onAddCityClick()
            }

            // Click listener for Location card to refresh
            binding.cardLocation.setOnClickListener {
                onLocationClick(cityName, position)
            }

            // Default display before data loaded
            binding.tvLocationName.text = cityName

            when (resource) {
                is Resource.Loading -> {
                    binding.lottieLoading.visibility = View.VISIBLE
                    binding.lottieLoading.playAnimation()
                }
                is Resource.Success -> {
                    binding.lottieLoading.visibility = View.GONE
                    binding.lottieLoading.pauseAnimation()
                    val data = resource.data

                    // Áp dụng cấu hình hiển thị Widget từ WeatherPreferenceManager
                    val showTemp = prefManager.showTemperature
                    val showHum = prefManager.showHumidity
                    val showWind = prefManager.showWind
                    val showVis = prefManager.showVisibility
                    val showPress = prefManager.showPressure
                    val showCloud = prefManager.showCloudCover
                    val showAqi = prefManager.showAirQuality
                    val showSun = prefManager.showSunCycle

                    binding.cardHeroWeather.visibility = if (showTemp) View.VISIBLE else View.GONE

                    binding.cardHumidity.visibility = if (showHum) View.VISIBLE else View.GONE
                    binding.cardWind.visibility = if (showWind) View.VISIBLE else View.GONE
                    binding.layoutTelemetryRow1.visibility = if (showHum || showWind) View.VISIBLE else View.GONE

                    binding.cardVisibility.visibility = if (showVis) View.VISIBLE else View.GONE
                    binding.cardPressure.visibility = if (showPress) View.VISIBLE else View.GONE
                    binding.layoutTelemetryRow2.visibility = if (showVis || showPress) View.VISIBLE else View.GONE

                    binding.cardCloudCover.visibility = if (showCloud) View.VISIBLE else View.GONE
                    binding.cardPressureDetail.visibility = if (showPress) View.VISIBLE else View.GONE
                    binding.layoutTelemetryRow3.visibility = if (showCloud || showPress) View.VISIBLE else View.GONE

                    binding.cardAirQuality.visibility = if (showAqi) View.VISIBLE else View.GONE
                    binding.cardSunCycle.visibility = if (showSun) View.VISIBLE else View.GONE

                    // 1. Thẻ Vị trí
                    val country = if (data.countryCode.isNotBlank()) data.countryCode else "VN"
                    binding.tvLocationName.text = "${data.cityName}, $country"
                    val timeFormatted = WeatherIconUtil.formatTime(data.timestamp, data.timezone)
                    binding.tvLastUpdated.text = context.getString(R.string.last_updated_format, timeFormatted)

                    // 2. Thẻ Thời tiết chính (Hero Card)
                    val description = if (data.weatherId != 0) {
                        binding.root.context.getString(
                            WeatherIconUtil.getWeatherDescription(data.weatherId)
                        )
                    } else {
                        data.weatherDescription.replaceFirstChar { it.uppercase() }
                    }
                    binding.tvConditionDescription.text = description

                    // Icon OpenWeatherMap
                    val iconCode = if (data.weatherIcon.isNotBlank()) data.weatherIcon else "01d"
                    Glide.with(context)
                        .load(WeatherIconUtil.getIconUrl4x(iconCode))
                        .placeholder(WeatherIconUtil.getLocalDrawableForIcon(iconCode))
                        .error(WeatherIconUtil.getLocalDrawableForIcon(iconCode))
                        .into(binding.ivWeatherMainIcon)

                    // Nhiệt độ
                    val currentTemp = if (isFahrenheit) {
                        WeatherIconUtil.celsiusToFahrenheit(data.temp)
                    } else {
                        data.temp
                    }
                    binding.tvMainTemp.text = Math.round(currentTemp).toString()
                    binding.tvMainTempUnit.text = unitSymbol

                    val feelsLikeTemp = if (isFahrenheit) {
                        WeatherIconUtil.celsiusToFahrenheit(data.feelsLike)
                    } else {
                        data.feelsLike
                    }
                    binding.tvFeelsLike.text = context.getString(R.string.feels_like, Math.round(feelsLikeTemp).toInt(), unitSymbol)

                    val tempMax = if (isFahrenheit) {
                        WeatherIconUtil.celsiusToFahrenheit(data.tempMax)
                    } else {
                        data.tempMax
                    }
                    val tempMin = if (isFahrenheit) {
                        WeatherIconUtil.celsiusToFahrenheit(data.tempMin)
                    } else {
                        data.tempMin
                    }
                    binding.tvTempHigh.text = context.getString(R.string.temp_high_format, Math.round(tempMax).toInt())
                    binding.tvTempLow.text = context.getString(R.string.temp_low_format, Math.round(tempMin).toInt())

                    // 3. Telemetry - Độ ẩm
                    binding.tvHumidityVal.text = "${data.humidity}%"
                    binding.pbHumidity.progress = data.humidity
                    val dewPoint = WeatherIconUtil.calculateDewPoint(data.temp, data.humidity)
                    val dewPointDisplay = if (isFahrenheit) {
                        Math.round(WeatherIconUtil.celsiusToFahrenheit(dewPoint.toDouble())).toInt()
                    } else {
                        dewPoint
                    }
                    binding.tvDewPoint.text = context.getString(R.string.dew_point_format, dewPointDisplay, unitSymbol)

                    // 4. Telemetry - Áp suất
                    binding.tvPressureVal.text = "${data.pressure} hPa"
                    val atm = data.pressure / 1013.25
                    binding.tvPressureAtmBadge.text = String.format(Locale.US, "%.2f atm", atm)

                    // 4.1. Chi tiết Áp suất biển & Mặt đất
                    binding.tvSeaLevelVal.text = "🌊 MSL: ${data.pressure} hPa"
                    binding.tvGroundLevelVal.text = "⛰ GND: ${data.pressure} hPa"
                    binding.tvPressureDiff.text = "Δ 0 hPa"

                    // 5. Telemetry - Gió & Gió giật
                    binding.tvWindSpeedVal.text = "${data.windSpeed} m/s"
                    binding.tvWindDirectionDegree.text = "↗ ${WeatherIconUtil.getWindDirectionShort(data.windDeg)}"

                    // 6. Telemetry - Tầm nhìn
                    val visibilityMeters = data.visibility
                    val visKm = visibilityMeters / 1000.0
                    binding.tvVisibilityVal.text = if (visKm >= 10.0) "${visKm.toInt()} km" else String.format(Locale.US, "%.1f km", visKm)
                    val (visDesc, visBadge) = WeatherIconUtil.getVisibilityEvaluation(visibilityMeters)
                    binding.tvVisibilityDesc.text = binding.root.context.getString(visDesc)
                    binding.tvVisibilityBadge.text = binding.root.context.getString(visBadge)

                    // 7. Telemetry - Mây (Cloud Cover)
                    val cloudiness = data.cloudiness
                    binding.tvCloudVal.text = "$cloudiness%"
                    binding.pbCloud.progress = cloudiness
                    val (_, cloudBadge) = WeatherIconUtil.getCloudCoverEvaluation(cloudiness)
                    binding.tvCloudBadge.text = binding.root.context.getString(cloudBadge)

                    // 8. Chất lượng không khí (AQI)
                    if (pref.showAirQuality) {
                        val aqiInfo = when {
                            visibilityMeters >= 9000 -> AqiInfo(32, R.string.aqi_good, R.string.aqi_good_desc, "#10B981", 1)
                            visibilityMeters >= 6000 -> AqiInfo(68, R.string.aqi_fair, R.string.aqi_fair_desc, "#3B82F6", 2)
                            visibilityMeters >= 3000 -> AqiInfo(120, R.string.aqi_moderate, R.string.aqi_moderate_desc, "#F59E0B", 3)
                            visibilityMeters >= 1000 -> AqiInfo(165, R.string.aqi_poor, R.string.aqi_poor_desc, "#F97316", 4)
                            else -> AqiInfo(230, R.string.aqi_very_poor, R.string.aqi_very_poor_desc, "#EF4444", 5)
                        }
                        binding.tvAirQualityVal.text = "${aqiInfo.value} AQI"
                        binding.tvAirQualityBadge.text = context.getString(aqiInfo.textRes)
                        binding.tvAirQualityBadge.setTextColor(android.graphics.Color.parseColor(aqiInfo.colorHex))
                        binding.tvAirQualityBadge.backgroundTintList = android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor(aqiInfo.colorHex)
                        ).withAlpha(35)
                        binding.tvAirQualityDesc.text = context.getString(aqiInfo.descRes)
                        binding.pbAirQuality.progress = aqiInfo.level
                        binding.pbAirQuality.progressTintList = android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor(aqiInfo.colorHex)
                        )
                    }

                    // 9. Mặt trời & Chu kỳ ngày
                    if (data.sunrise > 0 && data.sunset > 0) {
                        val sunriseTime = WeatherIconUtil.formatTime(data.sunrise, data.timezone)
                        val sunsetTime = WeatherIconUtil.formatTime(data.sunset, data.timezone)
                        binding.tvSunriseTime.text = sunriseTime
                        binding.tvSunsetTime.text = sunsetTime
                        binding.tvDaylightDuration.text = WeatherIconUtil.formatDaylightDuration(data.sunrise, data.sunset)

                        val nowSeconds = System.currentTimeMillis() / 1000L
                        val progress = if (data.sunset > data.sunrise && nowSeconds in data.sunrise..data.sunset) {
                            (nowSeconds - data.sunrise).toFloat() / (data.sunset - data.sunrise).toFloat()
                        } else if (nowSeconds >= data.sunset) {
                            1.0f
                        } else {
                            0.0f
                        }
                        binding.sunPathView.setSunProgress(progress)
                    }
                }
                is Resource.Error -> {
                    binding.lottieLoading.visibility = View.GONE
                    binding.lottieLoading.pauseAnimation()
                }
                null -> {
                    binding.lottieLoading.visibility = View.GONE
                    binding.lottieLoading.pauseAnimation()
                }
            }
        }
    }
}

data class AqiInfo(
    val value: Int,
    val textRes: Int,
    val descRes: Int,
    val colorHex: String,
    val level: Int
)
