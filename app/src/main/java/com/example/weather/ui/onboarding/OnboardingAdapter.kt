package com.example.weather.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weather.data.model.OnboardingItem
import com.example.weather.databinding.ItemOnboardingCardBinding

/**
 * Adapter hiển thị các thẻ onboarding bằng ViewPager2.
 *
 * @param onNextClick Callback khi người dùng bấm Next — nhận vào vị trí trang hiện tại.
 */
class OnboardingAdapter(
    private var items: List<OnboardingItem> = emptyList(),
    private val onNextClick: (currentPage: Int) -> Unit = {}
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(
        private val binding: ItemOnboardingCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OnboardingItem, position: Int) {
            with(binding) {
                tvName.setText(item.titleRes)
                tvIntroduction.setText(item.descriptionRes)
                ivIllustration.setImageResource(item.imageRes)
                ivDotIndicator.setImageResource(item.dotRes)

                tvNext.setOnClickListener {
                    onNextClick(position)
                }
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
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<OnboardingItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
