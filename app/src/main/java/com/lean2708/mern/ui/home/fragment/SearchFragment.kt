package com.lean2708.mern.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.lean2708.mern.R
import com.lean2708.mern.data.model.Product // Cần cho việc ép kiểu an toàn
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentSearchBinding
import com.lean2708.mern.repository.HomeRepository
import com.lean2708.mern.ui.home.adapter.ProductAdapter
import com.lean2708.mern.ui.viewmodel.HomeViewModel
import com.lean2708.mern.ui.viewmodel.HomeViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource
import androidx.core.widget.doAfterTextChanged
import android.view.inputmethod.EditorInfo

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(HomeRepository(RetrofitInstance.api))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarSearch.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        setupRecyclerView()
        setupListeners()
        setupObservers()

        // Tự động focus vào ô tìm kiếm khi mở Fragment (tùy chọn)
        binding.etSearchQuery.requestFocus()
    }

    private fun setupRecyclerView() {
        // ProductAdapter nhận Product -> ta truy cập ._id để điều hướng
        productAdapter = ProductAdapter(onProductClick = { product ->
            navigateToProductDetail(product._id)
        })

        // Sử dụng GridLayoutManager với spanCount = 2
        binding.rvSearchResults.layoutManager = GridLayoutManager(context, 2)
        binding.rvSearchResults.adapter = productAdapter
    }

    private fun setupListeners() {
        // 1. Xử lý sự kiện nhấn nút Enter trên bàn phím (Action Search)
        binding.etSearchQuery.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(v.text.toString())
                return@OnEditorActionListener true
            }
            false
        })

        // 2. Logic Live Search (Tìm kiếm khi text thay đổi)
        binding.etSearchQuery.doAfterTextChanged { editable ->
            val query = editable.toString()
            // Chỉ tìm kiếm ngay sau khi gõ nếu chuỗi có ít nhất 3 ký tự hoặc trống
            if (query.length >= 3 || query.isEmpty()) {
                search(query)
            }
        }
    }

    private fun search(query: String) {
        if (query.trim().isNotEmpty()) {
            // Gọi hàm ViewModel đã sửa
            viewModel.searchProducts(query.trim())
        } else {
            // Xóa kết quả nếu ô tìm kiếm trống
            productAdapter.differ.submitList(emptyList())
        }
    }

    private fun setupObservers() {
        // Lắng nghe LiveData searchResult từ HomeViewModel
        viewModel.searchResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    @Suppress("UNCHECKED_CAST")
                    val results = resource.data as? List<Product>
                    productAdapter.differ.submitList(results)

                    if (results?.isEmpty() == true && binding.etSearchQuery.text.isNotEmpty()) {
                        Toast.makeText(requireContext(), "Không tìm thấy kết quả.", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Lỗi tìm kiếm: ${resource.message}", Toast.LENGTH_LONG).show()
                    productAdapter.differ.submitList(emptyList())
                }
                else -> Unit
            }
        }
    }

    private fun navigateToProductDetail(productId: String) {
        val detailFragment = ProductDetailFragment.newInstance(productId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarSearch.visibility = if (isLoading) View.VISIBLE else View.GONE
        // Ẩn RecyclerView khi đang tải lần đầu
        binding.rvSearchResults.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}