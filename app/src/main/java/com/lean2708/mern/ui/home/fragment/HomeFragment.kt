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
import com.lean2708.mern.ui.home.fragment.SearchFragment // Cần import SearchFragment
import com.lean2708.mern.ui.home.fragment.CategoryProductListFragment // Cần import CategoryProductListFragment
import com.lean2708.mern.ui.home.fragment.ProductDetailFragment // Cần import ProductDetailFragment
import android.view.inputmethod.EditorInfo
import android.widget.TextView

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
        setupSearchBoxListener() // <--- GỌI HÀM KÍCH HOẠT TÌM KIẾM

        // Hàm loadHomePageData() được gọi trong init của ViewModel
    }

    private fun setupRecyclerView() {
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

    private fun setupSearchBoxListener() {
        // 1. Khi người dùng nhấn vào EditText, chuyển sang màn hình tìm kiếm chuyên biệt
        binding.etSearchBox.setOnClickListener {
            navigateToSearch()
        }

        // 2. Ngăn EditText trên HomeFragment mở bàn phím và chỉ kích hoạt khi click
        binding.etSearchBox.isFocusable = false
        binding.etSearchBox.isLongClickable = false
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

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
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

    /**
     * Hàm điều hướng tới màn hình Tìm kiếm chuyên biệt
     */
    private fun navigateToSearch() {
        // Sử dụng SearchFragment.newInstance() (giả định nó tồn tại và không cần tham số khởi tạo)
        val searchFragment = SearchFragment() // Tạo instance SearchFragment

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, searchFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}