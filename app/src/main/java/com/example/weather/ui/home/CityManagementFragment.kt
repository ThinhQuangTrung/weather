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
import com.example.weather.core.common.Resource
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.databinding.FragmentCityManagementBinding
import com.example.weather.domain.model.CityLocation
import com.example.weather.domain.usecase.SearchCityUseCase
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CityManagementFragment : Fragment() {

    private var _binding: FragmentCityManagementBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var prefManager: WeatherPreferenceManager
    @Inject lateinit var searchCityUseCase: SearchCityUseCase

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
            onCityLongClick = { cityName, _ ->
                showDeleteConfirmDialog(cityName)
            },
            onDeleteClick = { cityName, _ ->
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
        for (cityName in suggestedCities) {
            val chip = Chip(requireContext()).apply {
                text = cityName
                isClickable = true
                isCheckable = false
                setOnClickListener {
                    onSuggestedCitySelected(cityName)
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

        // TextWatcher để gọi API tìm kiếm sau 500ms debounce
        binding.etCityInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                searchRunnable?.let { searchHandler.removeCallbacks(it) }

                if (query.length < 2) {
                    hideSuggestions()
                    return
                }

                searchRunnable = Runnable {
                    performCitySearch(query)
                }
                searchHandler.postDelayed(searchRunnable!!, 500)
            }
        })
    }

    /**
     * Gọi Geocoding UseCase để tìm kiếm thành phố theo từ khóa
     */
    private fun performCitySearch(query: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            binding.progressSearch.visibility = View.VISIBLE
            hideSuggestions()

            when (val result = searchCityUseCase(query)) {
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
                    // Do nothing
                }
            }
        }
    }

    private fun hideSuggestions() {
        binding.rvSearchSuggestions.visibility = View.GONE
        suggestionAdapter.submitList(emptyList())
    }

    /**
     * Xử lý khi người dùng chọn một gợi ý từ danh sách tìm kiếm
     */
    private fun onSuggestionSelected(item: CityLocation) {
        val cityName = item.name
        hideSuggestions()
        binding.etCityInput.setText(cityName)
        binding.etCityInput.clearFocus()
        addCityAndSelect(cityName, item.displayName)
    }

    private fun onSuggestedCitySelected(cityName: String) {
        addCityAndSelect(cityName, cityName)
    }

    private fun addCityAndSelect(cityName: String, displayName: String) {
        val added = prefManager.addCity(cityName)
        if (added) {
            val updated = prefManager.getSavedCities()
            cityAdapter.updateCities(updated)
            val index = updated.indexOf(cityName).coerceAtLeast(0)
            Toast.makeText(
                requireContext(),
                getString(R.string.city_added_success, displayName),
                Toast.LENGTH_SHORT
            ).show()
            selectCityAndReturn(cityName, index)
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
                dialog.dismiss()// tắt hộp thoại sau khi bấm
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
