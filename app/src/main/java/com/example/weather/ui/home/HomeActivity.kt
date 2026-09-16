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
import com.example.weather.ui.base.BaseActivity
import com.example.weather.ui.favourite.FavouriteFragmaint
import com.example.weather.ui.forecast.ForecastFragment
import com.example.weather.ui.settings.SettingsFragment

/**
 * HomeActivity:
 * - Activity chính chứa Bottom Navigation và FragmentContainerView.
 * - Điều hướng mượt mà giữa các Fragment: HomeFragment, ForecastFragment, SettingsFragment.
 * - Khôi phục và quản lý trạng thái Fragment chính xác sau khi Activity recreate (đổi ngôn ngữ, đổi theme).
 */
class HomeActivity : BaseActivity() {

    private lateinit var binding: ActivityHomeBinding

    private var homeFragment: HomeFragment? = null
    private var forecastFragment: ForecastFragment? = null
    private var activeFragment: Fragment? = null

    private var favouriteFragment: FavouriteFragmaint? = null

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
            binding.bottomNavContainer.setPadding(0, 0, 0, 0)
            insets
        }

        setupFragments(savedInstanceState)
        setupBottomNavigation()
        setupBackStackListener()

        // Nếu mở từ LanguageActivity (khi đổi ngôn ngữ ở màn Cài đặt), mở lại màn Cài đặt ngay
        if (savedInstanceState == null && intent.getBooleanExtra(EXTRA_OPEN_SETTINGS, false)) {
            openSettings()
        }
    }

    fun openSettings() {
        if (supportFragmentManager.findFragmentByTag(TAG_SETTINGS) == null) {
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .add(
                    R.id.fragmentContainer,
                    SettingsFragment.newInstance(),
                    TAG_SETTINGS
                )
                .addToBackStack(TAG_SETTINGS)
                .commit()
        }
    }

    override fun recreate() {
        super.recreate()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun setupBackStackListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            val hasBackStack = supportFragmentManager.backStackEntryCount > 0
            setBottomNavVisibility(!hasBackStack)
        }
        setBottomNavVisibility(supportFragmentManager.backStackEntryCount == 0)
    }

    fun setBottomNavVisibility(visible: Boolean) {
        binding.bottomNavContainer.visibility = if (visible) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun setupFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val home = HomeFragment.newInstance()
            val forecast = ForecastFragment.newInstance()
            val favourite = FavouriteFragmaint.newInstance()

            homeFragment = home
            forecastFragment = forecast
            favouriteFragment = favourite
            activeFragment = home

            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, forecast, TAG_FORECAST)
                .hide(forecast)
                .add(R.id.fragmentContainer, favourite, TAG_FAVOURITE)
                .hide(favourite)
                .add(R.id.fragmentContainer, home, TAG_HOME)
                .commit()

            updateTabUI(0)
        } else {
            // Khôi phục lại references của Fragment đã được Android lưu trong FragmentManager sau khi Activity recreate
            homeFragment = supportFragmentManager.findFragmentByTag(TAG_HOME) as? HomeFragment
                ?: HomeFragment.newInstance().also {
                    supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, it, TAG_HOME).commit()
                }

            forecastFragment = supportFragmentManager.findFragmentByTag(TAG_FORECAST) as? ForecastFragment
                ?: ForecastFragment.newInstance().also {
                    supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, it, TAG_FORECAST).hide(it).commit()
                }

            val isForecastVisible = forecastFragment?.isVisible == true
            val isFavouriteVisible = favouriteFragment?.isVisible == true

            activeFragment = when {
                isForecastVisible -> forecastFragment
                isFavouriteVisible -> favouriteFragment
                else -> homeFragment
            }

            val selectedTab = when {
                isForecastVisible -> 1
                isFavouriteVisible -> 2
                else -> 0
            }

            updateTabUI(selectedTab)
        }
    }

    private fun setupBottomNavigation() {
        binding.tabHome.setOnClickListener {
            val target = homeFragment ?: (supportFragmentManager.findFragmentByTag(TAG_HOME) as? HomeFragment)
            if (target != null) {
                switchFragment(target, 0)
            }
        }

        binding.tabForecast.setOnClickListener {
            val target = forecastFragment ?: (supportFragmentManager.findFragmentByTag(TAG_FORECAST) as? ForecastFragment)
            if (target != null) {
                switchFragment(target, 1)
            }
        }
        binding.tapfavourite.setOnClickListener {
            val target = favouriteFragment ?: (supportFragmentManager.findFragmentByTag(TAG_FAVOURITE) as? FavouriteFragmaint)
            if (target != null) {
                switchFragment(target, 2)
            }
        }
    }

    private fun switchFragment(targetFragment: Fragment, tabIndex: Int) {
        val current = activeFragment
        if (current == targetFragment && targetFragment.isVisible) return

        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)

//        val tag = when (targetFragment) {
//            is HomeFragment -> TAG_HOME
//            is ForecastFragment -> TAG_FORECAST
//            is FavouriteFragmaint -> TAG_FAVOURITE
//            else -> return
//        }

        homeFragment?.let { if (it.isAdded && it != targetFragment) transaction.hide(it) }
        forecastFragment?.let { if (it.isAdded && it != targetFragment) transaction.hide(it) }
        favouriteFragment?.let { if (it.isAdded && it != targetFragment) { transaction.hide(it) }
        }
        transaction.show(targetFragment).commit()

        activeFragment = targetFragment
        updateTabUI(tabIndex)

        if (targetFragment is FavouriteFragmaint) {
            targetFragment.reload()
        } else if (targetFragment is HomeFragment) {
            targetFragment.refreshFavoriteState()
        }
    }

    fun navigateToHome(cityIndex: Int = -1) {
        val target = homeFragment ?: (supportFragmentManager.findFragmentByTag(TAG_HOME) as? HomeFragment)
        if (target != null) {
            switchFragment(target, 0)
            if (cityIndex >= 0) {
                target.selectCityIndex(cityIndex)
            }
        }
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


        val isFavourite  = selectedIndex == 2
        binding.pillFavourite.setBackgroundResource(if (isFavourite) R.drawable.bg_nav_active_pill else android.R.color.transparent)
        binding.ivTabFavourite.setColorFilter(if (isFavourite) activeColor else inactiveColor)
        binding.tvTabFavourite.setTextColor(if (isFavourite) activeColor else inactiveColor)
    }

    companion object {
        const val EXTRA_OPEN_SETTINGS = "extra_open_settings"
        private const val TAG_HOME = "HOME"
        private const val TAG_FORECAST = "FORECAST"
        private const val TAG_FAVOURITE = "FAVOURITE"
        const val TAG_SETTINGS = "SETTINGS"
    }
}
