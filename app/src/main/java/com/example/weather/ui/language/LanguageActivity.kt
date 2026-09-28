package com.example.weather.ui.language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.MainActivity
import com.example.weather.R
import com.example.weather.data.model.LanguageItem
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.databinding.ActivityLanguageBinding
import com.example.weather.core.base.BaseActivity
import com.example.weather.ui.home.HomeActivity
import com.example.weather.utils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LanguageActivity : BaseActivity() {

    private lateinit var binding: ActivityLanguageBinding
    private lateinit var adapter: LanguageAdapter

    @Inject
    lateinit var prefManager: WeatherPreferenceManager

    private var isFromSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.layoutLanguageRoot
        ) { v, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // Kiểm tra LanguageActivity được mở từ Settings hay không
        isFromSettings = intent.getBooleanExtra(
            EXTRA_FROM_SETTINGS,
            false
        )

        setupUI()
        setupLanguageList()
    }

    /**
     * Thiết lập UI của màn hình Language.
     */
    private fun setupUI() {

        // ==============================
        // BUTTON BACK
        // ==============================

        if (isFromSettings) {
        // mởi cài đặt cho quay lại
            binding.btnBack.visibility = View.VISIBLE
            binding.btnBack.setOnClickListener {
                finish()
            }

        } else {
            //còn không -> thoát
            binding.btnBack.visibility = View.GONE
        }

        // ==============================
        // BUTTON APPLY
        // ==============================

        if (isFromSettings) {
            // Mở từ Cài đặt -> hiển thị sẵn nút Apply
            binding.btnApplyLanguage.visibility = View.VISIBLE
        } else {
            // Lần đầu mở app / chưa chọn ngôn ngữ -> ẩn nút Apply, chỉ hiện khi người dùng chọn 1 ngôn ngữ
            binding.btnApplyLanguage.visibility = View.GONE
        }

        binding.btnApplyLanguage.setOnClickListener {
            applySelectedLanguage()
        }
    }

    /**
     * Thiết lập danh sách ngôn ngữ.
     */
    private fun setupLanguageList() {

        val languageList = listOf(
            LanguageItem("en", "English (United States)", "English", "flag_language/english.webp"),
            LanguageItem("de", "Deutsch (Germany)", "Deutsch", "flag_language/german.webp"),
            LanguageItem("fr", "Français (France)", "Français", "flag_language/french.webp"),
            LanguageItem("es", "Español (Spain)", "Español", "flag_language/spanish.webp"),
            LanguageItem("pt", "Português (Portugal)", "Português", "flag_language/portuguese.webp"),
            LanguageItem("pt-BR", "Português (Brazil)", "Português do Brasil", "flag_language/brazil.webp"),
            LanguageItem("ar", "العربية (Arabic)", "العربية", "flag_language/arabic.webp"),
            LanguageItem("ko", "한국어 (South Korea)", "한국어", "flag_language/south_korea.webp"),
            LanguageItem("ja", "日本語 (Japan)", "日本語", "flag_language/japan.webp"),
            LanguageItem("hi", "हिन्दी (India)", "हिन्दी", "flag_language/hindi.webp"),
            LanguageItem("id", "Bahasa Indonesia (Indonesia)", "Bahasa Indonesia", "flag_language/indonesia.webp"),
            LanguageItem("zh", "简体中文 (China)", "简体中文", "flag_language/china_simplified.webp"),
            LanguageItem("zh-TW", "繁體中文 (Taiwan)", "繁體中文", "flag_language/china_traditional.webp"),
            LanguageItem("ru", "Русский (Russia)", "Русский", "flag_language/russia.webp"),
            LanguageItem("tr", "Türkçe (Turkey)", "Türkçe", "flag_language/turkey.webp"),
            LanguageItem("bn", "বাংলা (Bangladesh)", "বাংলা", "flag_language/bangladesh.webp")
        )

        /*
         * Nếu mở từ Cài đặt:
         * → lấy language hiện tại đang dùng.
         *
         * Nếu là lần đầu mở app:
         * → không chọn item nào sẵn, để trống để người dùng bấm chọn.
         */
        val initialSelectedCode =
            if (isFromSettings) {
                LocaleHelper.getLanguage(this)
            } else {
                ""
            }

        adapter = LanguageAdapter(
            languages = languageList,
            initialSelectedCode = initialSelectedCode,

            onItemClick = { selectedLanguage ->

                // User vừa chọn một language
                binding.tvSelectLanguageTitle.text = selectedLanguage.displayName
                // → hiện nút Apply
                binding.btnApplyLanguage.visibility = View.VISIBLE
            }
        )

        binding.rvLanguages.layoutManager =
            LinearLayoutManager(this)

        binding.rvLanguages.adapter = adapter

        // ==============================
        // SCROLL ĐẾN LANGUAGE ĐANG CHỌN
        // ==============================

        if (initialSelectedCode.isNotEmpty()) {

            val selectedLanguage =
                languageList.firstOrNull {
                    it.code.equals( initialSelectedCode, ignoreCase = true ) }
            if (selectedLanguage != null)
            { binding.tvSelectLanguageTitle.text = selectedLanguage.displayName}

            val selectedIndex =
                languageList.indexOfFirst {

                    it.code.equals(
                        initialSelectedCode,
                        ignoreCase = true
                    )
                }

            if (selectedIndex != -1) {

                binding.rvLanguages.scrollToPosition(
                    selectedIndex
                )
            }
        }
    }

    /**
     * Áp dụng language mà người dùng đã chọn.
     */
    private fun applySelectedLanguage() {

        val selectedCode = adapter.selectedCode

        // Không có language được chọn
        if (selectedCode.isEmpty()) {
            return
        }

        // ==============================
        // ĐỔI LANGUAGE
        // ==============================

        LocaleHelper.setLocale(
            this,
            selectedCode
        )

        // Đánh dấu app đã chọn language
        prefManager.isLanguageSelected = true

        // ==============================
        // CHUYỂN MÀN HÌNH
        // ==============================

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    companion object {

        const val EXTRA_FROM_SETTINGS =
            "extra_from_settings"

        /**
         * Tạo Intent để mở LanguageActivity.
         */
        fun createIntent(
            context: Context,
            fromSettings: Boolean = false
        ): Intent {

            return Intent(
                context,
                LanguageActivity::class.java
            ).apply {

                putExtra(
                    EXTRA_FROM_SETTINGS,
                    fromSettings
                )
            }
        }
    }
}