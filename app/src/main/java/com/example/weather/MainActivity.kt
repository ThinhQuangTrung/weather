package com.example.weather

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.ui.home.HomeActivity
import com.example.weather.ui.main.MainViewModel
import com.example.weather.ui.onboarding.OnboardingAdapter
import com.example.weather.utils.Resource
import kotlin.math.abs

/**
 * - Tách biệt logic vào MainViewModel.
 * - Lắng nghe và cập nhật UI thông qua LiveData Observer.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var dots: Array<View>

    private val prefs by lazy {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isOnboardingCompleted =
            prefs.getBoolean("onboarding_completed", false)

        if (isOnboardingCompleted) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }
        enableEdgeToEdge()

        // Khởi tạo ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        setupViewPager()
        observeViewModel()
    }

    private fun setupViews() {
        dots = arrayOf(
            binding.dot1,
            binding.dot2,
            binding.dot3
        )

        binding.btnGetStarted.setOnClickListener {
            val currentPage = binding.viewPagerFeatures.currentItem
            val totalPages = onboardingAdapter.itemCount

            if (currentPage < totalPages - 1) {
                // Chuyển sang slide tiếp theo
                binding.viewPagerFeatures.setCurrentItem(currentPage + 1, true)
            } else {
                //kiemr tra đã xem Onboarding
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("onboarding_completed", true)
                    .apply()

                // Đã ở slide cuối → vào Home
                val intent = Intent(
                    this,
                    com.example.weather.ui.home.HomeActivity::class.java
                )
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setupViewPager() {
        onboardingAdapter = OnboardingAdapter()
        with(binding.viewPagerFeatures) {
            adapter = onboardingAdapter
            offscreenPageLimit = 3
            (getChildAt(0) as? RecyclerView)?.overScrollMode = RecyclerView.OVER_SCROLL_NEVER

            val compositeTransformer = CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(12))
                addTransformer { page, position ->
                    val r = 1 - abs(position)
                    page.scaleY = 0.94f + r * 0.06f
                    page.alpha = 0.75f + r * 0.25f
                }
            }
            setPageTransformer(compositeTransformer)

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    viewModel.onPageChanged(position)
                }
            })
        }
    }

    private fun observeViewModel() {
        // Quan sát danh sách thẻ onboarding từ ViewModel
        viewModel.onboardingItems.observe(this) { items ->
            onboardingAdapter.submitList(items)
            updatePageIndicator(binding.viewPagerFeatures.currentItem, items.size)
        }

        // Quan sát trang hiện tại
        viewModel.currentPage.observe(this) { page ->
            updatePageIndicator(page, onboardingAdapter.itemCount)
            updateDots(page)
        }

        // Quan sát trạng thái tải thời tiết thực tế từ OpenWeatherMap API
        viewModel.weatherState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Trạng thái đang tải
                }
                is Resource.Success -> {
                    val weather = resource.data
                    val temp = weather.main?.temp?.toInt() ?: 0
                    val city = weather.cityName
                    val desc = weather.weatherList?.firstOrNull()?.description ?: ""
                    Toast.makeText(
                        this,
                        "Thời tiết tại $city: ${temp}°C, $desc",
                        Toast.LENGTH_LONG
                    ).show()
                }
                is Resource.Error -> {
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updatePageIndicator(position: Int, total: Int) {
        val totalPages = if (total > 0) total else 3
        binding.tvPageIndicator.text = getString(R.string.page_counter_format, position + 1, totalPages)
    }

    private fun updateDots(activePosition: Int) {
        val density = resources.displayMetrics.density
        val activeWidth = (20 * density).toInt()
        val inactiveWidth = (6 * density).toInt()

        for (i in dots.indices) {
            val dot = dots[i]
            val isSelected = i == activePosition

            val targetWidth = if (isSelected) activeWidth else inactiveWidth
            val targetDrawable = if (isSelected) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive

            dot.setBackgroundResource(targetDrawable)

            val layoutParams = dot.layoutParams
            if (layoutParams != null && layoutParams.width != targetWidth) {
                val anim = ValueAnimator.ofInt(layoutParams.width, targetWidth)
                anim.duration = 200
                anim.addUpdateListener { valueAnimator ->
                    layoutParams.width = valueAnimator.animatedValue as Int
                    dot.layoutParams = layoutParams
                }
                anim.start()
            }
        }
    }
}