package com.example.weather.ui.language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.R
import com.example.weather.data.model.LanguageItem
import com.example.weather.databinding.ItemLanguageBinding

class LanguageAdapter(
    private val languages: List<LanguageItem>,
    initialSelectedCode: String,
    private val onItemClick: ((LanguageItem) -> Unit)? = null
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    var selectedCode: String = initialSelectedCode
        private set

    inner class LanguageViewHolder(val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LanguageItem) {
            val context = binding.root.context
            val isSelected = item.code.equals(selectedCode, ignoreCase = true)

            binding.tvLanguageName.text = if (item.isDefault) {
                "${item.displayName}${context.getString(R.string.language_default_suffix)}"
            } else {
                item.displayName
            }

            binding.ivFlag.setImageResource(item.flagRes)

            if (isSelected) {
                binding.layoutLanguageItem.setBackgroundResource(R.drawable.bg_language_item_selected)
                binding.ivRadioStatus.setImageResource(R.drawable.ic_radio_checked)
                binding.ivRadioStatus.setColorFilter(ContextCompat.getColor(context, R.color.stat_primary))
                binding.tvLanguageName.setTextColor(ContextCompat.getColor(context, R.color.card_title))
            } else {
                binding.layoutLanguageItem.setBackgroundResource(R.drawable.bg_language_item_normal)
                binding.ivRadioStatus.setImageResource(R.drawable.ic_radio_unchecked)
                binding.ivRadioStatus.clearColorFilter()
                binding.tvLanguageName.setTextColor(ContextCompat.getColor(context, R.color.telemetry_value))
            }

            binding.layoutLanguageItem.setOnClickListener {
                if (selectedCode != item.code) {
                    val oldPosition = languages.indexOfFirst { it.code.equals(selectedCode, ignoreCase = true) }
                    selectedCode = item.code
                    val newPosition = adapterPosition

                    if (oldPosition != -1) notifyItemChanged(oldPosition)
                    if (newPosition != -1) notifyItemChanged(newPosition)

                    onItemClick?.invoke(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding = ItemLanguageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.bind(languages[position])
    }

    override fun getItemCount(): Int = languages.size

    fun getSelectedItem(): LanguageItem? {
        return languages.find { it.code.equals(selectedCode, ignoreCase = true) }
    }
}
