package com.lean2708.mern.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.lean2708.mern.R
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentCategoryProductListBinding
import com.lean2708.mern.repository.HomeRepository
import com.lean2708.mern.ui.home.adapter.ProductAdapter
import com.lean2708.mern.ui.viewmodel.HomeViewModel
import com.lean2708.mern.ui.viewmodel.HomeViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource
import com.lean2708.mern.ui.viewmodel.HomeDisplayItem
import com.lean2708.mern.data.model.Product
import androidx.lifecycle.lifecycleScope // Cần thiết
import kotlinx.coroutines.launch

class CategoryProductListFragment : Fragment() {

    private var _binding: FragmentCategoryProductListBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(HomeRepository(RetrofitInstance.api))
    }

    private var allCategories: List<com.lean2708.mern.data.model.Product> = emptyList()
    private var initialCategory: String? = null

    companion object {
        const val ARG_CATEGORY_NAME = "initial_category_name"
        fun newInstance(categoryName: String? = null): CategoryProductListFragment {
            val fragment = CategoryProductListFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_CATEGORY_NAME, categoryName)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialCategory = arguments?.getString(ARG_CATEGORY_NAME)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        setupRecyclerView()
        setupObservers()

        viewModel.loadHomePageData()
    }

    private fun setupRecyclerView() {
        // SỬA LỖI: ProductAdapter nhận Product -> ta truy cập ._id để lấy String
        productAdapter = ProductAdapter(onProductClick = { product ->
            navigateToProductDetail(product._id) // Truyền Product ID (String)
        })
        binding.rvCategoryProducts.adapter = productAdapter
    }

    private fun setupObservers() {
        // 1. Lắng nghe danh sách Category (để tạo Tab)
        viewModel.homeItems.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> {
                    setLoading(true)
                }
                is Resource.Success<*> -> {
                    setLoading(false)
                    val displayList = resource.data as? List<HomeDisplayItem>
                    val categoriesItem = displayList?.firstOrNull() as? HomeDisplayItem.Categories

                    categoriesItem?.let {
                        allCategories = it.representativeProducts
                        populateTabs()
                    }
                }
                is Resource.Error -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Lỗi tải danh mục.", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }

        // 2. Lắng nghe danh sách sản phẩm theo Category (LIVE DATA MỚI)
        viewModel.categoryProductList.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    // Ép kiểu an toàn và submit List
                    @Suppress("UNCHECKED_CAST")
                    val products = resource.data as? List<Product>
                    productAdapter.differ.submitList(products)
                }
                is Resource.Error -> {
                    setLoading(false)
                    productAdapter.differ.submitList(emptyList())
                    Toast.makeText(requireContext(), "Lỗi tải sản phẩm: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    private fun populateTabs() {
        if (allCategories.isEmpty()) return

        // 1. Thêm các Tab vào TabLayout
        allCategories.forEach { product ->
            binding.tabLayoutCategories.addTab(
                binding.tabLayoutCategories.newTab().setText(product.category).setTag(product.category)
            )
        }

        // 2. Thiết lập Listener cho việc chuyển Tab
        binding.tabLayoutCategories.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val category = tab.tag as String
                viewModel.fetchProductsByCategoryOnly(category) // Gọi ViewModel
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // 3. Chọn Tab ban đầu và tải dữ liệu
        val initialTab = if (initialCategory != null) {
            // Tìm kiếm đúng tab dựa trên tên category
            binding.tabLayoutCategories.getTabAt(allCategories.indexOfFirst { it.category == initialCategory })
        } else {
            binding.tabLayoutCategories.getTabAt(0)
        }

        initialTab?.select()
        // Kích hoạt fetch cho tab ban đầu
        initialTab?.tag?.let { viewModel.fetchProductsByCategoryOnly(it as String) }
            ?: allCategories.firstOrNull()?.category?.let { viewModel.fetchProductsByCategoryOnly(it) }
    }

    private fun navigateToProductDetail(productId: String) {
        val detailFragment = ProductDetailFragment.newInstance(productId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarCategoryList.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvCategoryProducts.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}