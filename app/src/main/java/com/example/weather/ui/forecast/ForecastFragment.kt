package com.example.weather.ui.forecast

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather.databinding.FragmentForecastBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

/**
 * ForecastFragment:
 * - Hiển thị dự báo thời tiết 5 ngày và dự báo theo giờ (24 giờ tới).
 * - Đã được refactor: logic xử lý dữ liệu chuyển sang ForecastViewModel.
 * - Fragment chỉ quan sát LiveData và cập nhật UI.
 */
@AndroidEntryPoint
class ForecastFragment : Fragment() {

    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForecastViewModel by viewModels()

    private lateinit var hourlyAdapter: HourlyForecastAdapter
    private lateinit var dailyAdapter: DailyForecastAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupHourlyTouch()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                binding.tvErrorMessage.visibility = View.VISIBLE
                binding.tvErrorMessage.text = error
            } else {
                binding.tvErrorMessage.visibility = View.GONE
            }
        }

        viewModel.locationHeader.observe(viewLifecycleOwner) { header ->
            binding.tvForecastLocation.text = header
            binding.layoutForecastContent.visibility = View.VISIBLE
        }

        viewModel.hourlyList.observe(viewLifecycleOwner) { list ->
            hourlyAdapter.submitList(list)
        }

        viewModel.dailyList.observe(viewLifecycleOwner) { list ->
            dailyAdapter.submitList(list)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupHourlyTouch() {
        var downX = 0f
        var downY = 0f

        binding.rvHourlyForecast.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    downY = event.y
                    view.parent.requestDisallowInterceptTouchEvent(true)
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - downX
                    val dy = event.y - downY
                    view.parent.requestDisallowInterceptTouchEvent(abs(dx) > abs(dy))
                    false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    view.parent.requestDisallowInterceptTouchEvent(false)
                    false
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            viewModel.loadForecast()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.loadForecast()
        }
    }

    private fun setupRecyclerViews() {
        hourlyAdapter = HourlyForecastAdapter()
        binding.rvHourlyForecast.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = hourlyAdapter
        }

        dailyAdapter = DailyForecastAdapter()
        binding.rvDailyForecast.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = dailyAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ForecastFragment()
    }
}
