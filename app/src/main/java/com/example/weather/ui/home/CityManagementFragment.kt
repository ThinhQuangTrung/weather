package com.example.weather.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.data.model.GeocodingItem
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.data.repository.WeatherRepository
import com.example.weather.databinding.FragmentCityManagementBinding
import com.example.weather.utils.Resource
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class CityManagementFragment : Fragment() {

    private var _binding: FragmentCityManagementBinding? = null
    private val binding get() = _binding!!

    private val prefManager by lazy { WeatherPreferenceManager(requireContext()) }
    private val repository by lazy { WeatherRepository() }
    private lateinit var cityAdapter: CityAdapter
    private lateinit var suggestionAdapter: CitySuggestionAdapter

    private var searchJob: Job? = null
    private val searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    private val suggestedCities = listOf(
        "Hanoi", "Ho Chi Minh", "Da Nang", "Vinh",
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
        setupSuggestionRecyclerView()
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

    private fun setupSuggestionRecyclerView() {
        suggestionAdapter = CitySuggestionAdapter { item ->
            onSuggestionSelected(item)
        }
        binding.rvSearchSuggestions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = suggestionAdapter
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

        // TextWatcher de goi API tim kiem sau 500ms debounce
        binding.etCityInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                // Huy runnable cu neu co
                searchRunnable?.let { searchHandler.removeCallbacks(it) }

                if (query.length < 2) {
                    hideSuggestions()
                    return
                }

                // Dat debounce 500ms de tranh goi API lien tuc
                searchRunnable = Runnable {
                    performCitySearch(query)
                }
                searchHandler.postDelayed(searchRunnable!!, 500)
            }
        })
    }

    /**
     * Goi Geocoding API de tim kiem thanh pho theo tu khoa
     */
    private fun performCitySearch(query: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            binding.progressSearch.visibility = View.VISIBLE
            hideSuggestions()

            when (val result = repository.searchCity(query)) {
                is Resource.Success -> {
                    binding.progressSearch.visibility = View.GONE
                    val items = result.data
                    if (items.isNotEmpty()) {
                        suggestionAdapter.submitList(items)
                        binding.rvSearchSuggestions.visibility = View.VISIBLE
                    } else {
                        hideSuggestions()
                    }
                }
                is Resource.Error -> {
                    binding.progressSearch.visibility = View.GONE
                    hideSuggestions()
                }
                is Resource.Loading -> {
                    // Do nothing, loading indicator already showing
                }
            }
        }
    }

    private fun hideSuggestions() {
        binding.rvSearchSuggestions.visibility = View.GONE
        suggestionAdapter.submitList(emptyList())
    }

    /**
     * Xu ly khi nguoi dung chon mot goi y tu API search
     */
    private fun onSuggestionSelected(item: GeocodingItem) {
        val cityName = item.name
        hideSuggestions()
        binding.etCityInput.setText(cityName)
        binding.etCityInput.clearFocus()

        val added = prefManager.addCity(cityName)
        if (added) {
            val updated = prefManager.getSavedCities()
            cityAdapter.updateCities(updated)
            val index = updated.indexOf(cityName)
            Toast.makeText(
                requireContext(),
                getString(R.string.city_added_success, item.displayName),
                Toast.LENGTH_SHORT
            ).show()
            selectCityAndReturn(cityName, if (index >= 0) index else 0)
        } else {
            val cities = prefManager.getSavedCities()
            val index = cities.indexOfFirst { it.equals(cityName, ignoreCase = true) }
            if (index != -1) {
                selectCityAndReturn(cityName, index)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.city_already_exists),
                    Toast.LENGTH_SHORT
                ).show()
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

        hideSuggestions()
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
        searchRunnable?.let { searchHandler.removeCallbacks(it) }
        searchJob?.cancel()
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
