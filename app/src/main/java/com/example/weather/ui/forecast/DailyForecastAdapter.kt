package com.example.weather.ui.forecast

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.weather.databinding.ItemDailyForecastBinding
import com.example.weather.utils.WeatherIconUtil

/**
 * Adapter hien thi danh sach du bao 5 ngay tiep theo
 */
class DailyForecastAdapter(
    private var items: List<DailyForecastUiModel> = emptyList(),
    private val onItemClick: ((DailyForecastUiModel) -> Unit)? = null
) : RecyclerView.Adapter<DailyForecastAdapter.DailyViewHolder>() {

    fun submitList(newItems: List<DailyForecastUiModel>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val binding = ItemDailyForecastBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DailyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class DailyViewHolder(private val binding: ItemDailyForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DailyForecastUiModel) {
            val context = binding.root.context
            binding.tvDailyDay.text = item.dayName
            binding.tvDailyDesc.text = item.description
            binding.tvDailyTempRange.text = item.tempRangeString

            // Load OpenWeather icon with Glide and local fallback
            val iconCode = item.iconCode
            Glide.with(context)
                .load(WeatherIconUtil.getIconUrl(iconCode))
                .placeholder(WeatherIconUtil.getLocalDrawableForIcon(iconCode))
                .error(WeatherIconUtil.getLocalDrawableForIcon(iconCode))
                .into(binding.ivDailyIcon)

            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }
    }
}
