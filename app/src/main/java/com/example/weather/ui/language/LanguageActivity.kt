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

        if (
            !isFromSettings &&
            !prefManager.isLanguageSelected
        ) {

            // Lần đầu vào app
            // Không cho quay lại
            binding.btnBack.visibility = View.GONE

        } else {

            // Đã chọn language trước đó
            // hoặc mở từ Settings
            binding.btnBack.visibility = View.VISIBLE

            binding.btnBack.setOnClickListener {
                finish()
            }
        }

        // ==============================
        // BUTTON APPLY
        // ==============================

        if (!prefManager.isLanguageSelected) {

            // Lần đầu:
            // Chưa chọn language → ẩn Apply
            binding.btnApplyLanguage.visibility = View.GONE

        } else {

            // Đã chọn language trước đó
            // → hiện Apply
            binding.btnApplyLanguage.visibility = View.VISIBLE
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

            LanguageItem("en", "English", "English", R.drawable.english),
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

        /*
         * Nếu người dùng đã chọn language trước đó
         * → lấy language hiện tại.
         *
         * Nếu là lần đầu:
         * → không chọn item nào.
         */
        val initialSelectedCode =
            if (prefManager.isLanguageSelected) {
                LocaleHelper.getLanguage(this)
            } else {
                ""
            }

        adapter = LanguageAdapter(
            languages = languageList,
            initialSelectedCode = initialSelectedCode,

            onItemClick = {

                // User vừa chọn một language
                // → hiện nút Apply
                binding.btnApplyLanguage.visibility =
                    View.VISIBLE
            }
        )

        binding.rvLanguages.layoutManager =
            LinearLayoutManager(this)

        binding.rvLanguages.adapter = adapter

        // ==============================
        // SCROLL ĐẾN LANGUAGE ĐANG CHỌN
        // ==============================

        if (initialSelectedCode.isNotEmpty()) {

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

        if (isFromSettings) {

            val intent =
                Intent(
                    this,
                    HomeActivity::class.java
                ).apply {

                    flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_NEW_TASK

                    putExtra(
                        HomeActivity.EXTRA_OPEN_SETTINGS,
                        true
                    )
                }

            startActivity(intent)

            finish()

        } else {

            val intent =
                Intent(
                    this,
                    MainActivity::class.java
                )

            startActivity(intent)

            finish()
        }
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