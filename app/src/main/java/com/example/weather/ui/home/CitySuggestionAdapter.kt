package com.example.weather.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.data.model.GeocodingItem
import com.example.weather.databinding.ItemCitySuggestionBinding

/**
 * Adapter cho danh sach goi y thanh pho tu Geocoding API
 */
class CitySuggestionAdapter(
    private val onItemClick: (GeocodingItem) -> Unit
) : ListAdapter<GeocodingItem, CitySuggestionAdapter.SuggestionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = ItemCitySuggestionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SuggestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SuggestionViewHolder(
        private val binding: ItemCitySuggestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GeocodingItem) {
            binding.tvSuggestionName.text = item.name
            binding.tvSuggestionDetail.text = item.locationDetail

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GeocodingItem>() {
            override fun areItemsTheSame(old: GeocodingItem, new: GeocodingItem): Boolean {
                return old.lat == new.lat && old.lon == new.lon
            }

            override fun areContentsTheSame(old: GeocodingItem, new: GeocodingItem): Boolean {
                return old == new
            }
        }
    }
}
