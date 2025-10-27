package com.lean2708.mern.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentHomeBinding // Sửa tên binding
import com.lean2708.mern.repository.HomeRepository
import com.lean2708.mern.ui.home.adapter.HomeAdapter
import com.lean2708.mern.ui.viewmodel.HomeViewModel
import com.lean2708.mern.ui.viewmodel.HomeViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource

// Kế thừa từ Fragment thay vì AppCompatActivity
class HomeFragment : Fragment() {

    // Thay đổi cách khởi tạo binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeAdapter: HomeAdapter
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(HomeRepository(RetrofitInstance.api))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Chuyển code từ onCreate (Activity) sang onViewCreated (Fragment)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        homeAdapter = HomeAdapter()
        binding.rvHome.adapter = homeAdapter
    }

    private fun setupObservers() {
        // Dùng viewLifecycleOwner thay vì this
        viewModel.homeItems.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    resource.data?.let {
                        homeAdapter.differ.submitList(it)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    // Dùng requireContext() thay vì this
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Thêm hàm này để tránh memory leak
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}