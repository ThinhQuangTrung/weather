package com.example.weather.ui.forecast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.weather.databinding.FragmentForecastBinding

/**
 * ForecastFragment:
 * - Đại diện cho tab "Forecast" trên Bottom Navigation.
 * - Sử dụng ViewBinding toàn diện (không findViewById).
 * - Là màn hình placeholder sẵn sàng cho tính năng dự báo thời tiết 5 ngày tiếp theo.
 */
class ForecastFragment : Fragment() {

    private var _binding: FragmentForecastBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForecastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ForecastFragment()
    }
}
