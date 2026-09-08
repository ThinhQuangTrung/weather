package com.example.weather.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.weather.databinding.FragmentSearchSavedBinding

/**
 * SearchSavedFragment:
 * - Đại diện cho tab "Search & Saved" trên Bottom Navigation.
 * - Sử dụng ViewBinding toàn diện (không findViewById).
 * - Là màn hình placeholder sẵn sàng cho tính năng tìm kiếm địa điểm và quản lý danh sách yêu thích.
 */
class SearchSavedFragment : Fragment() {

    private var _binding: FragmentSearchSavedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = SearchSavedFragment()
    }
}
