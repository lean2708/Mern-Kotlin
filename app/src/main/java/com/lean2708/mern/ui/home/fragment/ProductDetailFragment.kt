package com.lean2708.mern.ui.home.fragment

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.lean2708.mern.R
import com.lean2708.mern.data.model.Product
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentProductDetailBinding
import com.lean2708.mern.repository.HomeRepository
import com.lean2708.mern.ui.home.adapter.ProductAdapter
import com.lean2708.mern.ui.viewmodel.HomeViewModel
import com.lean2708.mern.ui.viewmodel.HomeViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource
import java.text.NumberFormat
import java.util.Locale

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var suggestedAdapter: ProductAdapter

    private var currentProduct: Product? = null

    // Sử dụng HomeViewModel để lấy chi tiết và sản phẩm đề xuất
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(HomeRepository(RetrofitInstance.api))
    }

    private var productId: String? = null

    companion object {
        const val ARG_PRODUCT_ID = "product_id"
        fun newInstance(productId: String): ProductDetailFragment {
            val fragment = ProductDetailFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_PRODUCT_ID, productId)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = arguments?.getString(ARG_PRODUCT_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        if (productId != null) {
            setupRecyclerView()
            setupObservers()
            setupListeners()

            // GỌI API CHÍNH XÁC
            viewModel.fetchProductDetails(productId!!)
        } else {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy ID sản phẩm", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        // Khi click vào sản phẩm đề xuất, điều hướng tới màn hình chi tiết mới
        suggestedAdapter = ProductAdapter(onProductClick = { product ->
            navigateToProductDetail(product._id)
        })
        binding.rvSuggestedProducts.adapter = suggestedAdapter
    }

    private fun setupObservers() {
        // 1. Lắng nghe chi tiết sản phẩm
        viewModel.productDetails.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    (resource.data as? Product)?.let {
                        displayProductDetails(it)
                        currentProduct = it // LƯU SẢN PHẨM HIỆN TẠI
                    }
                }
                is Resource.Error<*> -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                    binding.rvSuggestedProducts.visibility = View.GONE
                    binding.bottomActionLayout.visibility = View.GONE
                }
                else -> Unit
            }
        }

        // 2. Lắng nghe sản phẩm đề xuất
        viewModel.suggestedProducts.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success<*> -> {
                    // Cập nhật Adapter sản phẩm đề xuất
                    @Suppress("UNCHECKED_CAST")
                    (resource.data as? List<Product>)?.let { list ->
                        suggestedAdapter.differ.submitList(list)
                    }
                }
                is Resource.Error<*> -> {
                    binding.tvSuggestedTitle.visibility = View.GONE
                }
                else -> Unit
            }
        }

        // 3. Lắng nghe kết quả THÊM VÀO GIỎ HÀNG
        viewModel.addToCartResult.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> {
                    setLoadingButtons(true) // Vô hiệu hóa nút
                }
                is Resource.Success<*> -> {
                    setLoadingButtons(false)
                    Toast.makeText(requireContext(), resource.message ?: "Thêm giỏ hàng thành công!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error<*> -> {
                    setLoadingButtons(false)
                    Toast.makeText(requireContext(), "Lỗi: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }
    }

    private fun displayProductDetails(product: Product) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        binding.toolbar.title = product.productName // Cập nhật tiêu đề Toolbar

        binding.tvProductName.text = product.productName
        binding.tvBrandName.text = "Thương hiệu: ${product.brandName}"
        binding.tvDescription.text = product.description

        // Giá bán (sellingPrice)
        binding.tvSellingPrice.text = formatter.format(product.sellingPrice)

        // Giá gốc (price) - Gạch ngang
        binding.tvPrice.text = formatter.format(product.price)
        binding.tvPrice.paintFlags = binding.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        // Ảnh
        if (product.productImage.isNotEmpty()) {
            Glide.with(this)
                .load(product.productImage[0])
                .into(binding.imgProduct)
        }
        binding.bottomActionLayout.visibility = View.VISIBLE
    }

    private fun navigateToProductDetail(newProductId: String) {
        // Tạo Fragment mới và thay thế Fragment hiện tại
        val detailFragment = newInstance(newProductId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null) // Cho phép nhấn Back để quay lại chi tiết sản phẩm cũ
            .commit()
    }

    private fun setupListeners() {
        binding.btnBuyNow.setOnClickListener {
            // Kiểm tra sản phẩm trước khi mua
            currentProduct?.let { product ->
                Toast.makeText(requireContext(), "Đang xử lý mua ngay: ${product.productName}", Toast.LENGTH_SHORT).show()
                // TODO: Xử lý logic thanh toán
            } ?: Toast.makeText(requireContext(), "Sản phẩm chưa sẵn sàng.", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddToCart.setOnClickListener {
            currentProduct?.let { product ->
                viewModel.addToCart(product._id) // GỌI HÀM ADD TO CART
            } ?: Toast.makeText(requireContext(), "Sản phẩm chưa sẵn sàng.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
        // Ẩn thanh thao tác khi đang tải
        binding.bottomActionLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun setLoadingButtons(isLoading: Boolean) {
        // Chỉ vô hiệu hóa nút khi xử lý giỏ hàng
        binding.btnAddToCart.isEnabled = !isLoading
        binding.btnBuyNow.isEnabled = !isLoading
        binding.btnAddToCart.text = if (isLoading) "Đang thêm..." else "Thêm vào giỏ"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}