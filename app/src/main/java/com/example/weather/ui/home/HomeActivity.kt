package com.example.weather.ui.home

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.weather.R
import com.example.weather.databinding.ActivityHomeBinding
import com.example.weather.ui.forecast.ForecastFragment

/**
 * HomeActivity:
 * - Activity chính chứa Bottom Navigation và FragmentContainerView.
 * - Điều hướng mượt mà giữa các Fragment: HomeFragment, ForecastFragment, SearchSavedFragment.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    private val homeFragment by lazy { HomeFragment.newInstance() }
    private val forecastFragment by lazy { ForecastFragment.newInstance() }

    private var activeFragment: Fragment = homeFragment

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(com.example.weather.utils.LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.fragmentContainer.setPadding(0, systemBars.top, 0, 0)
//            binding.bottomNavContainer.setPadding(0, 0, 0, systemBars.bottom)
            binding.bottomNavContainer.setPadding(0, 0, 0, 0
            )
            insets
        }

        setupFragments(savedInstanceState)
        setupBottomNavigation()
    }

    private fun setupFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, forecastFragment, "FORECAST")
                .hide(forecastFragment)
                .add(R.id.fragmentContainer, homeFragment, "HOME")
                .commit()
            activeFragment = homeFragment
        }
    }

    private fun setupBottomNavigation() {
        binding.tabHome.setOnClickListener {
            switchFragment(homeFragment, 0)
        }

        binding.tabForecast.setOnClickListener {
            switchFragment(forecastFragment, 1)
        }

    }

    private fun switchFragment(targetFragment: Fragment, tabIndex: Int) {
        if (activeFragment == targetFragment) return

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .hide(activeFragment)
            .show(targetFragment)
            .commit()

        activeFragment = targetFragment
        updateTabUI(tabIndex)
    }

    private fun updateTabUI(selectedIndex: Int) {
        val activeColor = ContextCompat.getColor(this, R.color.bottom_nav_active_tint)
        val inactiveColor = ContextCompat.getColor(this, R.color.bottom_nav_inactive_tint)

        // Tab 0: Home
        val isHome = selectedIndex == 0
        binding.pillHome.setBackgroundResource(if (isHome) R.drawable.bg_nav_active_pill else android.R.color.transparent)
        binding.ivTabHome.setColorFilter(if (isHome) activeColor else inactiveColor)
        binding.tvTabHome.setTextColor(if (isHome) activeColor else inactiveColor)

        // Tab 1: Forecast
        val isForecast = selectedIndex == 1
        binding.pillForecast.setBackgroundResource(if (isForecast) R.drawable.bg_nav_active_pill else android.R.color.transparent)
        binding.ivTabForecast.setColorFilter(if (isForecast) activeColor else inactiveColor)
        binding.tvTabForecast.setTextColor(if (isForecast) activeColor else inactiveColor)


    }
}
