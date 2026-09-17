package com.example.weather.ui.favourite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weather.R
import com.example.weather.databinding.ItemFavouriteBinding
import com.example.weather.core.common.TemperatureUnit
import com.example.weather.utils.WeatherIconUtil
import kotlin.math.roundToInt
// hiển thành phố lên item RecyclerView
class FavouriteAdapter(
    private var items: List<FavouriteUiModel>,
    private var tempUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    private val onItemClick: (FavouriteUiModel, Int) -> Unit,
    private val onFavoriteClick: (FavouriteUiModel, Int) -> Unit
) : RecyclerView.Adapter<FavouriteAdapter.FavouriteViewHolder>() {

    fun updateData(newItems: List<FavouriteUiModel>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    fun setTempUnit(unit: TemperatureUnit) {
        this.tempUnit = unit
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val binding = ItemFavouriteBinding.inflate( // tạo ra cái khi để chuẩn bị cho viêc đưa view
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavouriteViewHolder(binding)
    }
// thực hiện nhiêm vụ đữ liệu vào khung
    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class FavouriteViewHolder(private val binding: ItemFavouriteBinding) :
        RecyclerView.ViewHolder(binding.root) {
//thực hiện dưa dữ liệu vào xml
        fun bind(item: FavouriteUiModel, position: Int) {
            val context = binding.root.context
            val isFahrenheit = tempUnit == TemperatureUnit.FAHRENHEIT
            val unitSymbol = if (isFahrenheit) "°F" else "°C"

            // 1. Tên thành phố và Mã quốc gia
            binding.tvCityName.text = item.cityName
            binding.tvCountryBadge.text = item.countryCode.ifEmpty { "VN" }

            // 2. Mô tả thời tiết & Gió
            val desc = if (item.weatherId != null) {
                context.getString(WeatherIconUtil.getWeatherDescription(item.weatherId))
            } else if (item.weatherDesc.isNotEmpty()) {
                item.weatherDesc.replaceFirstChar { it.uppercase() }
            } else {
                context.getString(R.string.weather_default)
            }

            val windKmH = (item.windSpeed * 3.6).roundToInt()
            val windText = if (windKmH > 0) " • Gió ${windKmH} km/h" else ""
            binding.tvWeatherDesc.text = "$desc$windText"

            // 3. Nhiệt độ & Nhiệt độ thấp nhất
            val tempValue = if (isFahrenheit) {
                WeatherIconUtil.celsiusToFahrenheit(item.temp)
            } else {
                item.temp
            }
            binding.tvTemperature.text = "${tempValue.roundToInt()}$unitSymbol"

            val tempMinValue = if (isFahrenheit) {
                WeatherIconUtil.celsiusToFahrenheit(item.tempMin)
            } else {
                item.tempMin
            }
            binding.tvTempSub.text = "Thấp: ${tempMinValue.roundToInt()}°"

            // 4. Icon thời tiết
            if (item.iconCode.isNotEmpty()) {
                Glide.with(context)
                    .load(WeatherIconUtil.getIconUrl4x(item.iconCode))
                    .placeholder(WeatherIconUtil.getLocalDrawableForIcon(item.iconCode))
                    .error(WeatherIconUtil.getLocalDrawableForIcon(item.iconCode))
                    .into(binding.ivWeatherIcon)
            } else {
                binding.ivWeatherIcon.setImageResource(R.drawable.ic_cloud)
            }

            // 5. Sự kiện Click vào thẻ để chuyển sang Home
            binding.root.setOnClickListener {
                onItemClick(item, position)
            }

            // 6. Sự kiện Click vào nút Trái tim để bỏ yêu thích
            binding.ivFavorite.setOnClickListener {
                onFavoriteClick(item, position)
            }
        }
    }
}
