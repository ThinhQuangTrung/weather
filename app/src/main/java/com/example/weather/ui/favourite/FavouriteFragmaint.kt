package com.example.weather.ui.favourite

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.R
import com.example.weather.data.preference.WeatherPreferenceManager
import com.example.weather.databinding.FragmaintFavouriteBinding
import com.example.weather.ui.home.CityManagementFragment
import com.example.weather.ui.home.HomeActivity

class FavouriteFragmaint : Fragment() {

    private var _binding: FragmaintFavouriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FavouriteViewModel
    private lateinit var adapter: FavouriteAdapter
    private val prefManager by lazy { WeatherPreferenceManager(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmaintFavouriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[FavouriteViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.loadFavourites()
    }

    private fun setupRecyclerView() {
        adapter = FavouriteAdapter(
            items = emptyList(),
            tempUnit = viewModel.tempUnit.value ?: com.example.weather.ui.home.TemperatureUnit.CELSIUS,
            onItemClick = { item, _ ->
                // Khi bấm vào thẻ, thêm vào savedCities nếu chưa có và chuyển sang Home
                val savedCities = prefManager.getSavedCities().toMutableList()
                val targetName = item.originalCityKey.ifEmpty { item.cityName }
                var index = savedCities.indexOfFirst { it.equals(targetName, ignoreCase = true) }
                if (index == -1) {
                    prefManager.addCity(targetName)
                    val updated = prefManager.getSavedCities()
                    index = updated.indexOfFirst { it.equals(targetName, ignoreCase = true) }
                }
                if (index >= 0) {
                    prefManager.selectedCityIndex = index
                    (activity as? HomeActivity)?.navigateToHome(index)
                }
            },
            onFavoriteClick = { item, _ ->
                // Bỏ yêu thích ngay lập tức trong 1 lần nhấn
                viewModel.removeFavourite(item.originalCityKey.ifEmpty { item.cityName })
            }
        )

        binding.recyFavourite.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FavouriteFragmaint.adapter
        }
    }

    private fun setupListeners() {
        // Nút thêm thành phố
        binding.btnAddCity.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .add(
                    R.id.fragmentContainer,
                    CityManagementFragment.newInstance(),
                    "CITY_MGMT"
                )
                .addToBackStack("CITY_MGMT")
                .commit()
        }

        // Tìm kiếm thành phố trong danh sách yêu thích
        binding.etSearchCity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                binding.ivClearSearch.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                viewModel.searchFavourites(query)
            }
        })

        binding.ivClearSearch.setOnClickListener {
            binding.etSearchCity.text?.clear()
        }
    }

    private fun observeViewModel() {
        // Quan sát danh sách yêu thích
        viewModel.favouriteList.observe(viewLifecycleOwner) { list ->
            adapter.updateData(list)
            binding.tvFavouriteCount.text = getString(R.string.favourite_count, list.size)

            val isEmpty = list.isEmpty()
            binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.recyFavourite.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        // Quan sát trạng thái tải
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbFavouriteLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Quan sát đơn vị nhiệt độ
        viewModel.tempUnit.observe(viewLifecycleOwner) { unit ->
            adapter.setTempUnit(unit)
        }

        // Quan sát thông báo người dùng
        viewModel.userMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearUserMessage()
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && isAdded && ::viewModel.isInitialized) {
            viewModel.loadFavourites()
        }
    }

    fun reload() {
        if (isAdded && ::viewModel.isInitialized) {
            viewModel.loadFavourites()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadFavourites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = FavouriteFragmaint()
    }
}
