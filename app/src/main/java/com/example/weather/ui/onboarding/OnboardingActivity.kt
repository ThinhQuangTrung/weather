package com.example.weather.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.weather.core.base.BaseActivity
import com.example.weather.databinding.ActivityOnboardingBinding
import com.example.weather.ui.setup.WeatherSetupActivity
import com.example.weather.utils.DepthPageTransformer
import dagger.hilt.android.AndroidEntryPoint

/**
 * Màn hình Onboarding giới thiệu tính năng ứng dụng.
 * Sử dụng ViewPager2 với hiệu ứng DepthPageTransformer.
 */
@AndroidEntryPoint
class OnboardingActivity : BaseActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private val viewModel: OnboardingViewModel by viewModels()
    private lateinit var onboardingAdapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViewPager()
        observeViewModel()
    }

    private fun setupViewPager() {
        onboardingAdapter = OnboardingAdapter(onNextClick = { currentPage ->
            val totalPages = onboardingAdapter.itemCount
            if (currentPage < totalPages - 1) {
                binding.viewPagerFeatures.setCurrentItem(currentPage + 1, true)
            } else {
                viewModel.completeOnboarding()
            }
        })

        with(binding.viewPagerFeatures) {
            adapter = onboardingAdapter
            offscreenPageLimit = 4
            (getChildAt(0) as? RecyclerView)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setPageTransformer(DepthPageTransformer())

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.onPageChanged(position)
                }
            })
        }
    }

    private fun observeViewModel() {
        viewModel.onboardingItems.observe(this) { items ->
            onboardingAdapter.submitList(items)
        }

        viewModel.navigateToSetup.observe(this) { shouldNavigate ->
            if (shouldNavigate == true) {
                startActivity(Intent(this, WeatherSetupActivity::class.java))
                finish()
            }
        }
    }

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, OnboardingActivity::class.java)
        }
    }
}
