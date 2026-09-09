package com.example.weather.ui.language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.MainActivity
import com.example.weather.R
import com.example.weather.data.model.LanguageItem
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.databinding.ActivityLanguageBinding
import com.example.weather.utils.LocaleHelper

class LanguageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLanguageBinding
    private lateinit var adapter: LanguageAdapter
    private val prefManager by lazy { WeatherPreferenceManager(this) }

    private var isFromSettings = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.layoutLanguageRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        isFromSettings = intent.getBooleanExtra(EXTRA_FROM_SETTINGS, false)

        setupUI()
        setupLanguageList()
    }

    private fun setupUI() {
        if (!isFromSettings && !prefManager.isLanguageSelected) {
            // Lần đầu mở: có thể ẩn nút back hoặc cho phép thoát
            binding.btnBack.visibility = View.GONE
        } else {
            binding.btnBack.visibility = View.VISIBLE
            binding.btnBack.setOnClickListener {
                finish()
            }
        }

        binding.btnApplyLanguage.setOnClickListener {
            applySelectedLanguage()
        }
    }

    private fun setupLanguageList() {
        val languageList = listOf(
            LanguageItem("en", "English", "English", R.drawable.english, isDefault = true),
            LanguageItem("de", "German", "Deutsch", R.drawable.german),
            LanguageItem("fr", "French", "Français", R.drawable.french),
            LanguageItem("es", "Spanish", "Español", R.drawable.spanish),
            LanguageItem("it", "Italian", "Italiano", R.drawable.ic_flag_italy),
            LanguageItem("nl", "Dutch", "Nederlands", R.drawable.ic_flag_netherlands),
            LanguageItem("pt", "Portuguese", "Português", R.drawable.portuguese),
            LanguageItem("ar", "Arabic", "العربية", R.drawable.arabic),
            LanguageItem("ko", "Korean", "한국어", R.drawable.south_korea),
            LanguageItem("ja", "Japanese", "日本語", R.drawable.japan),
            LanguageItem("hi", "Hindi", "हिन्दी", R.drawable.hindi),
            LanguageItem("id", "Indonesia", "Bahasa Indonesia", R.drawable.indonesia),
            LanguageItem("vi", "Vietnamese", "Tiếng Việt", R.drawable.ic_flag_vietnam),
            LanguageItem("zh", "Chinese (Simplified)", "简体中文", R.drawable.china_simplified),
            LanguageItem("zh-TW", "Chinese (Traditional)", "繁體中文", R.drawable.china_traditional),
            LanguageItem("ru", "Russian", "Русский", R.drawable.russia),
            LanguageItem("tr", "Turkish", "Türkçe", R.drawable.turkey),
            LanguageItem("bn", "Bengali", "বাংলা", R.drawable.bangladesh),
            LanguageItem("pt-BR", "Portuguese (Brazil)", "Português do Brasil", R.drawable.brazil)
        )

        val currentSavedLang = LocaleHelper.getLanguage(this)

        adapter = LanguageAdapter(languageList, currentSavedLang)
        binding.rvLanguages.layoutManager = LinearLayoutManager(this)
        binding.rvLanguages.adapter = adapter

        // Tự động cuộn đến vị trí ngôn ngữ đang chọn
        val selectedIndex = languageList.indexOfFirst { it.code.equals(currentSavedLang, ignoreCase = true) }
        if (selectedIndex != -1) {
            binding.rvLanguages.scrollToPosition(selectedIndex)
        }
    }

    private fun applySelectedLanguage() {
        val selectedCode = adapter.selectedCode
        LocaleHelper.setLocale(this, selectedCode)
        prefManager.isLanguageSelected = true

        if (isFromSettings) {
            setResult(RESULT_OK)
            finish()
        } else {
            // Chuyển sang màn hình Onboarding (bước 3 theo sơ đồ)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val EXTRA_FROM_SETTINGS = "extra_from_settings"

        fun createIntent(context: Context, fromSettings: Boolean = false): Intent {
            return Intent(context, LanguageActivity::class.java).apply {
                putExtra(EXTRA_FROM_SETTINGS, fromSettings)
            }
        }
    }
}
