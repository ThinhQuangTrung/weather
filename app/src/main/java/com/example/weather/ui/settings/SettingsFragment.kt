package com.example.weather.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.weather.R
import com.example.weather.databinding.FragmentSettingsBinding

/**
 * SettingsFragment:
 * - Màn hình Cài đặt tuân thủ mô hình MVVM và ViewBinding.
 * - Cho phép người dùng tùy chỉnh: Ngôn ngữ, Chế độ giao diện (Sáng / Tối / Theo hệ thống), Đơn vị nhiệt độ (°C / °F).
 * - Tương tác trực tiếp và lưu dữ liệu thông qua SettingsViewModel.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[SettingsViewModel::class.java]

        setupListeners()
        observeViewModel()
    }

    private val languageLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            requireActivity().recreate()
        }
    }

    private val setupLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) {
        // Cập nhật lại UI sau khi cấu hình
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Chọn Ngôn ngữ
        binding.layoutLanguage.setOnClickListener {
            val intent = com.example.weather.ui.language.LanguageActivity.createIntent(
                requireContext(),
                fromSettings = true
            )
            languageLauncher.launch(intent)
        }

        // Tùy chỉnh thông số thời tiết -> Mở WeatherSetupActivity
        binding.layoutWeatherWidgets.setOnClickListener {
            val intent = com.example.weather.ui.setup.WeatherSetupActivity.createIntent(
                requireContext(),
                fromSettings = true
            )
            setupLauncher.launch(intent)
        }

        // Chọn Giao diện
        binding.layoutTheme.setOnClickListener {
            showThemeDialog()
        }
    }

    private fun observeViewModel() {
        val currentLang = com.example.weather.utils.LocaleHelper.getLanguage(requireContext())
        binding.tvLanguageValue.text = when (currentLang.lowercase()) {
            "vi" -> "Tiếng Việt"
            "en" -> "English"
            "de" -> "Deutsch (German)"
            "fr" -> "Français (French)"
            "es" -> "Español (Spanish)"
            "it" -> "Italiano (Italian)"
            "nl" -> "Nederlands (Dutch)"
            "pt" -> "Português (Portuguese)"
            "pt-br" -> "Português do Brasil"
            "ar" -> "العربية (Arabic)"
            "ko" -> "한국어 (Korean)"
            "ja" -> "日本語 (Japanese)"
            "hi" -> "हिन्दी (Hindi)"
            "id", "in" -> "Bahasa Indonesia"
            "zh", "zh-cn" -> "简体中文 (Chinese)"
            "zh-tw" -> "繁體中文 (Chinese Trad.)"
            "ru" -> "Русский (Russian)"
            "tr" -> "Türkçe (Turkish)"
            "bn" -> "বাংলা (Bengali)"
            else -> "English"
        }

        viewModel.theme.observe(viewLifecycleOwner) { theme ->
            binding.tvThemeValue.text = when (theme) {
                "light" -> "Sáng (Light)"
                "dark" -> "Tối (Dark)"
                else -> "Theo hệ thống (System)"
            }
        }
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Theo hệ thống", "Sáng (Light)", "Tối (Dark)")
        val currentTheme = viewModel.theme.value
        val checkedItem = when (currentTheme) {
            "light" -> 1
            "dark" -> 2
            else -> 0
        }
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Chọn giao diện")
            .setSingleChoiceItems(themes, checkedItem) { dialog, which ->
                val selected = when (which) {
                    1 -> "light"
                    2 -> "dark"
                    else -> "system"
                }
                dialog.dismiss()
                // Tắt tất cả animation của cửa sổ TRƯỚC khi đổi theme
                // để tránh hiện tượng nhấp nháy trắng/đen khi Activity recreate
                requireActivity().window.setWindowAnimations(0)
                viewModel.setTheme(selected)
            }
            .setNegativeButton("Hủy", null)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_bg)

        dialog.show()
    }

    private fun showUnitDialog() {
        val units = arrayOf("Độ C (°C)", "Độ F (°F)")
        val currentUnit = viewModel.unit.value
        val checkedItem = if (currentUnit == "fahrenheit") 1 else 0

        AlertDialog.Builder(requireContext())
            .setTitle("Chọn đơn vị nhiệt độ")
            .setSingleChoiceItems(units, checkedItem) { dialog, which ->
                val selected = if (which == 1) "fahrenheit" else "celsius"
                viewModel.setUnit(selected)
                dialog.dismiss()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        (activity as? com.example.weather.ui.home.HomeActivity)?.setBottomNavVisibility(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? com.example.weather.ui.home.HomeActivity)?.setBottomNavVisibility(true)
        _binding = null
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}