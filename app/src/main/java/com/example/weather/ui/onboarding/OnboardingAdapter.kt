package com.example.weather.ui.onboarding

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.data.model.OnboardingItem
import com.example.weather.databinding.ItemOnboardingCardBinding

/**
 * Adapter hiển thị các thẻ onboarding bằng ViewBinding (thay thế hoàn toàn findViewById)
 */
class OnboardingAdapter(
    private var items: List<OnboardingItem> = emptyList()
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(
        private val binding: ItemOnboardingCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OnboardingItem) {
            val context = binding.root.context

            with(binding) {
                // Icon badge
                ivBadgeIcon.setImageResource(item.iconRes)
                ivBadgeIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, item.iconTintRes)
                )
                ivBadgeIcon.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, item.iconBgRes)
                )

                // Tag pill
                tvTag.text = item.tagText
                tvTag.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, item.tagBgRes)
                )
                tvTag.setTextColor(ContextCompat.getColor(context, item.tagTextColorRes))

                // Title & Description
                tvCardTitle.text = item.title
                tvCardDesc.text = item.description

                // Bottom stats
                val statColor = ContextCompat.getColor(context, item.statColorRes)
                val statColorStateList = ColorStateList.valueOf(statColor)

                ivStat1.setImageResource(item.stat1Icon)
                ivStat1.imageTintList = statColorStateList
                tvStat1.text = item.stat1Text
                tvStat1.setTextColor(statColor)

                ivStat2.setImageResource(item.stat2Icon)
                ivStat2.imageTintList = statColorStateList
                tvStat2.text = item.stat2Text
                tvStat2.setTextColor(statColor)

                ivStat3.setImageResource(item.stat3Icon)
                ivStat3.imageTintList = statColorStateList
                tvStat3.text = item.stat3Text
                tvStat3.setTextColor(statColor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<OnboardingItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
