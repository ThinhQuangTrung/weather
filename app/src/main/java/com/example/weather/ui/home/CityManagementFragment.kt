package com.example.weather.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.databinding.FragmentCityManagementBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CityManagementFragment : Fragment() {

    private var _binding: FragmentCityManagementBinding? = null
    private val binding get() = _binding!!

    private val prefManager by lazy { WeatherPreferenceManager(requireContext()) }
    private lateinit var cityAdapter: CityAdapter

    private val suggestedCities = listOf(
        "Hanoi", "Ho Chi Minh", "Da Nang", "Vinh", "Hai Phong",
        "Can Tho", "Hue", "Nha Trang", "Tokyo", "Seoul",
        "London", "Paris", "New York", "Singapore", "Bangkok"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCityManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSuggestedChips()
        setupListeners()
    }

    private fun setupRecyclerView() {
        val cities = prefManager.getSavedCities()
        cityAdapter = CityAdapter(
            cities = cities,
            onCityClick = { cityName, position ->
                selectCityAndReturn(cityName, position)
            },
            onCityLongClick = { cityName, position ->
                showDeleteConfirmDialog(cityName)
            },
            onDeleteClick = { cityName, position ->
                showDeleteConfirmDialog(cityName)
            }
        )

        binding.rvSavedCities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cityAdapter
        }
    }

    private fun setupSuggestedChips() {
        binding.chipGroupSuggested.removeAllViews()
        for (city in suggestedCities) {
            val chip = Chip(requireContext()).apply {
                text = city
                isCheckable = false
                isClickable = true
                setChipBackgroundColorResource(R.color.badge_blue_bg)
                setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.stat_primary))
                setOnClickListener {
                    onSuggestedCitySelected(city)
                }
            }
            binding.chipGroupSuggested.addView(chip)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.cardCurrentLocation.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY_CITY_SELECTED,
                bundleOf(KEY_USE_CURRENT_LOCATION to true)
            )
            parentFragmentManager.popBackStack()
        }

        binding.btnAddCity.setOnClickListener {
            addNewCityFromInput()
        }

        binding.etCityInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addNewCityFromInput()
                true
            } else {
                false
            }
        }
    }

    private fun onSuggestedCitySelected(cityName: String) {
        val added = prefManager.addCity(cityName)
        if (added) {
            val updated = prefManager.getSavedCities()
            cityAdapter.updateCities(updated)
            val index = updated.indexOf(cityName)
            Toast.makeText(
                requireContext(),
                getString(R.string.city_added_success, cityName),
                Toast.LENGTH_SHORT
            ).show()
            selectCityAndReturn(cityName, index)
        } else {
            val cities = prefManager.getSavedCities()
            val index = cities.indexOfFirst { it.equals(cityName, ignoreCase = true) }
            if (index != -1) {
                selectCityAndReturn(cityName, index)
            }
        }
    }

    private fun addNewCityFromInput() {
        val input = binding.etCityInput.text.toString().trim()
        if (input.isEmpty()) {
            binding.etCityInput.error = getString(R.string.add_city_hint)
            return
        }

        val added = prefManager.addCity(input)
        if (added) {
            binding.etCityInput.text?.clear()
            val updated = prefManager.getSavedCities()
            cityAdapter.updateCities(updated)
            Toast.makeText(
                requireContext(),
                getString(R.string.city_added_success, input),
                Toast.LENGTH_SHORT
            ).show()
            val newIndex = updated.indexOf(input)
            selectCityAndReturn(input, if (newIndex >= 0) newIndex else updated.size - 1)
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.city_already_exists),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showDeleteConfirmDialog(cityName: String) {
        val cities = prefManager.getSavedCities()
        if (cities.size <= 1) {
            Toast.makeText(
                requireContext(),
                getString(R.string.cannot_delete_last_city),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_delete)
            .setTitle(getString(R.string.delete_city_title))
            .setMessage(getString(R.string.delete_city_confirm, cityName))
            .setPositiveButton(getString(R.string.btn_delete)) { dialog, _ ->
                dialog.dismiss()
                val removed = prefManager.removeCity(cityName)
                if (removed) {
                    val updated = prefManager.getSavedCities()
                    cityAdapter.updateCities(updated)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.city_deleted_success, cityName),
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.setFragmentResult(
                        REQUEST_KEY_CITY_CHANGED,
                        bundleOf(KEY_CITY_LIST_MODIFIED to true)
                    )
                }
            }
            .setNegativeButton(getString(R.string.btn_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun selectCityAndReturn(cityName: String, index: Int) {
        prefManager.selectedCityIndex = index
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY_CITY_SELECTED,
            bundleOf(
                KEY_SELECTED_CITY_NAME to cityName,
                KEY_SELECTED_CITY_INDEX to index,
                KEY_CITY_LIST_MODIFIED to true
            )
        )
        parentFragmentManager.popBackStack()
    }

    override fun onResume() {
        super.onResume()
        (activity as? HomeActivity)?.setBottomNavVisibility(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? HomeActivity)?.setBottomNavVisibility(true)
        _binding = null
    }

    companion object {
        const val REQUEST_KEY_CITY_SELECTED = "request_key_city_selected"
        const val REQUEST_KEY_CITY_CHANGED = "request_key_city_changed"
        const val KEY_SELECTED_CITY_NAME = "key_selected_city_name"
        const val KEY_SELECTED_CITY_INDEX = "key_selected_city_index"
        const val KEY_CITY_LIST_MODIFIED = "key_city_list_modified"
        const val KEY_USE_CURRENT_LOCATION = "key_use_current_location"

        fun newInstance() = CityManagementFragment()
    }
}
