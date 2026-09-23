package com.example.weather.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.databinding.ItemCityBinding

class CityAdapter(
    private var cities: List<String>,
    private val onCityClick: (String, Int) -> Unit,
    private val onCityLongClick: (String, Int) -> Unit,
    private val onDeleteClick: (String, Int) -> Unit
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {
/*khi giao diện add city thây đối */
    fun updateCities(newCities: List<String>) {
        this.cities = newCities
        notifyDataSetChanged()//ép giao diện vẽ lại toàn bộ danh sách
    }
/*tao ra vung để dưa các item vào */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val binding = ItemCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.bind(cities[position], position)
    }

    override fun getItemCount(): Int = cities.size/*báo cho RecyclerView viết bao nhiêu dể chuẩn bị danh sách */

    inner class CityViewHolder(private val binding: ItemCityBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cityName: String, position: Int) {
            binding.tvCityName.text = cityName

            binding.root.setOnClickListener {
                onCityClick(cityName, position)
            }

            binding.root.setOnLongClickListener {
                onCityLongClick(cityName, position)
                true
            }

            binding.ivActionDelete.setOnClickListener {
                onDeleteClick(cityName, position)
            }
        }
    }
}
