package com.example.weather.ui.setup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weather.R
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.databinding.ActivityWeatherSetupBinding
import com.example.weather.ui.home.HomeActivity
import com.example.weather.utils.LocaleHelper


class WeatherSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherSetupBinding
    private val prefManager by lazy { WeatherPreferenceManager(this) }

    private var isFromSettings = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityWeatherSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutSetupRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        isFromSettings = intent.getBooleanExtra(EXTRA_FROM_SETTINGS, false)

        initValues()
        setupListeners()
    }

    private fun initValues() {
        if (isFromSettings) {
            binding.btnBackSetup.visibility = View.VISIBLE
            binding.btnBackSetup.setOnClickListener { finish() }
            binding.btnContinueSetup.text = getString(R.string.btn_continue)
        }

        // Tải các giá trị đã lưu
        binding.cbOptionTemp.isChecked = prefManager.showTemperature
        binding.cbOptionHumidity.isChecked = prefManager.showHumidity
        binding.cbOptionWind.isChecked = prefManager.showWind
        binding.cbOptionVisibility.isChecked = prefManager.showVisibility
        binding.cbOptionPressure.isChecked = prefManager.showPressure
        binding.cbOptionAirQuality.isChecked = prefManager.showAirQuality
        binding.cbOptionCloud.isChecked = prefManager.showCloudCover
        binding.cbOptionSunCycle.isChecked = prefManager.showSunCycle

        updateCardUI()
    }

    private fun updateCardUI() {
        updateSingleCard(binding.cardOptionTemp, binding.cbOptionTemp.isChecked)
        updateSingleCard(binding.cardOptionHumidity, binding.cbOptionHumidity.isChecked)
        updateSingleCard(binding.cardOptionWind, binding.cbOptionWind.isChecked)
        updateSingleCard(binding.cardOptionVisibility, binding.cbOptionVisibility.isChecked)
        updateSingleCard(binding.cardOptionPressure, binding.cbOptionPressure.isChecked)
        updateSingleCard(binding.cardOptionAirQuality, binding.cbOptionAirQuality.isChecked)
        updateSingleCard(binding.cardOptionCloud, binding.cbOptionCloud.isChecked)
        updateSingleCard(binding.cardOptionSunCycle, binding.cbOptionSunCycle.isChecked)
    }

    private fun updateSingleCard(card: View, isChecked: Boolean) {
        if (isChecked) {
            card.setBackgroundResource(R.drawable.bg_setup_card_selected)
        } else {
            card.setBackgroundResource(R.drawable.bg_setup_card_normal)
        }
    }

    private fun setupListeners() {
        binding.cardOptionTemp.setOnClickListener {
            binding.cbOptionTemp.isChecked = !binding.cbOptionTemp.isChecked
            updateSingleCard(binding.cardOptionTemp, binding.cbOptionTemp.isChecked)
        }

        binding.cardOptionHumidity.setOnClickListener {
            binding.cbOptionHumidity.isChecked = !binding.cbOptionHumidity.isChecked
            updateSingleCard(binding.cardOptionHumidity, binding.cbOptionHumidity.isChecked)
        }

        binding.cardOptionWind.setOnClickListener {
            binding.cbOptionWind.isChecked = !binding.cbOptionWind.isChecked
            updateSingleCard(binding.cardOptionWind, binding.cbOptionWind.isChecked)
        }

        binding.cardOptionVisibility.setOnClickListener {
            binding.cbOptionVisibility.isChecked = !binding.cbOptionVisibility.isChecked
            updateSingleCard(binding.cardOptionVisibility, binding.cbOptionVisibility.isChecked)
        }

        binding.cardOptionPressure.setOnClickListener {
            binding.cbOptionPressure.isChecked = !binding.cbOptionPressure.isChecked
            updateSingleCard(binding.cardOptionPressure, binding.cbOptionPressure.isChecked)
        }

        binding.cardOptionAirQuality.setOnClickListener {
            binding.cbOptionAirQuality.isChecked = !binding.cbOptionAirQuality.isChecked
            updateSingleCard(binding.cardOptionAirQuality, binding.cbOptionAirQuality.isChecked)
        }

        binding.cardOptionCloud.setOnClickListener {
            binding.cbOptionCloud.isChecked = !binding.cbOptionCloud.isChecked
            updateSingleCard(binding.cardOptionCloud, binding.cbOptionCloud.isChecked)
        }

        binding.cardOptionSunCycle.setOnClickListener {
            binding.cbOptionSunCycle.isChecked = !binding.cbOptionSunCycle.isChecked
            updateSingleCard(binding.cardOptionSunCycle, binding.cbOptionSunCycle.isChecked)
        }

        binding.btnContinueSetup.setOnClickListener {
            savePreferencesAndProceed()
        }
    }

    private fun savePreferencesAndProceed() {
        // Lưu cấu hình
        prefManager.showTemperature = binding.cbOptionTemp.isChecked
        prefManager.showHumidity = binding.cbOptionHumidity.isChecked
        prefManager.showWind = binding.cbOptionWind.isChecked
        prefManager.showVisibility = binding.cbOptionVisibility.isChecked
        prefManager.showPressure = binding.cbOptionPressure.isChecked
        prefManager.showAirQuality = binding.cbOptionAirQuality.isChecked
        prefManager.showCloudCover = binding.cbOptionCloud.isChecked
        prefManager.showSunCycle = binding.cbOptionSunCycle.isChecked
        prefManager.isWeatherSetupCompleted = true

        if (isFromSettings) {
            setResult(RESULT_OK)
            finish()
        } else {
            // Bước 5: Chuyển sang HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val EXTRA_FROM_SETTINGS = "extra_from_settings"

        fun createIntent(context: Context, fromSettings: Boolean = false): Intent {
            return Intent(context, WeatherSetupActivity::class.java).apply {
                putExtra(EXTRA_FROM_SETTINGS, fromSettings)
            }
        }
    }
}
