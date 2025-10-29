package com.lean2708.mern.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lean2708.mern.R
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentHomeBinding
import com.lean2708.mern.repository.HomeRepository
import com.lean2708.mern.ui.home.adapter.HomeAdapter
import com.lean2708.mern.ui.viewmodel.HomeViewModel
import com.lean2708.mern.ui.viewmodel.HomeViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource

class HomeFragment : Fragment() {

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        // Hàm loadHomePageData() được gọi trong init của ViewModel
    }

    private fun setupRecyclerView() {
        // Cập nhật: Truyền cả hai listener cho sản phẩm chi tiết và danh mục
        homeAdapter = HomeAdapter(
            onProductClick = { productId ->
                navigateToProductDetail(productId)
            },
            onCategoryClick = { categoryName ->
                navigateToCategoryProductList(categoryName) // Điều hướng tới trang list sản phẩm theo category
            }
        )
        binding.rvHome.adapter = homeAdapter
    }

    private fun setupObservers() {
        viewModel.homeItems.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    resource.data?.let {
                        // Cần ép kiểu an toàn khi sử dụng Resource.Success<*>
                        @Suppress("UNCHECKED_CAST")
                        homeAdapter.differ.submitList(it as? List<com.lean2708.mern.ui.viewmodel.HomeDisplayItem>)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }
    }

    /**
     * Hàm điều hướng tới màn hình chi tiết sản phẩm
     */
    private fun navigateToProductDetail(productId: String) {
        val detailFragment = ProductDetailFragment.newInstance(productId)

        // Sử dụng fragment_container ID từ MainActivity để chuyển Fragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null) // Cho phép người dùng nhấn nút Back để quay lại HomeFragment
            .commit()
    }

    /**
     * Hàm điều hướng tới màn hình danh sách sản phẩm theo Category
     */
    private fun navigateToCategoryProductList(categoryName: String) {
        val listFragment = CategoryProductListFragment.newInstance(categoryName)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, listFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}