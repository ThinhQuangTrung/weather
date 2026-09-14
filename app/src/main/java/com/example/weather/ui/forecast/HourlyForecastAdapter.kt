package com.example.weather.ui.forecast

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weather.R
import com.example.weather.databinding.ItemHourlyForecastBinding
import com.example.weather.utils.WeatherIconUtil

/**
 * Adapter hien thi du bao thoi tiet theo gio (24 gio toi)
 */
class HourlyForecastAdapter(
    private var items: List<HourlyForecastUiModel> = emptyList(),
) : RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {


    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<HourlyForecastUiModel>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val binding = ItemHourlyForecastBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HourlyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class HourlyViewHolder(private val binding: ItemHourlyForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HourlyForecastUiModel) {
            val context = binding.root.context

            binding.tvHourlyTime.text = item.time
            binding.tvHourlyTemp.text = item.tempString
            binding.tvHourlyPop.text = item.popString

            binding.layoutHourlyContainer.setBackgroundResource(
                    R.drawable.bg_hourly_card_selected)


            // Load OpenWeather icon with Glide and local fallback
            val iconCode = item.iconCode
            Glide.with(context)
                .load(WeatherIconUtil.getIconUrl(iconCode))
                .placeholder(WeatherIconUtil.getLocalDrawableForIcon(iconCode))
                .error(WeatherIconUtil.getLocalDrawableForIcon(iconCode))
                .into(binding.ivHourlyIcon)


        }
    }
}
