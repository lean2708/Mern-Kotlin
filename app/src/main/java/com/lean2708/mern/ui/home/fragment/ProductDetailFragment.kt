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
import com.google.android.material.tabs.TabLayoutMediator
import com.lean2708.mern.R
import com.lean2708.mern.data.model.Product
import com.lean2708.mern.data.model.ProductReview
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentProductDetailBinding
import com.lean2708.mern.repository.HomeRepository
import com.lean2708.mern.ui.home.adapter.ImageSliderAdapter
import com.lean2708.mern.ui.home.adapter.ProductAdapter
import com.lean2708.mern.ui.home.adapter.ReviewAdapter
import com.lean2708.mern.ui.viewmodel.HomeViewModel
import com.lean2708.mern.ui.viewmodel.HomeViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource
import java.text.NumberFormat
import java.util.Locale
import com.lean2708.mern.data.local.SessionManager
import com.lean2708.mern.ui.auth.LoginActivity
import android.content.Intent
import com.lean2708.mern.ui.orders.CheckoutFragment // CẦN IMPORT

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var suggestedAdapter: ProductAdapter
    private lateinit var imageSliderAdapter: ImageSliderAdapter
    private lateinit var reviewAdapter: ReviewAdapter

    private var currentProduct: Product? = null
    private val sessionManager by lazy { SessionManager(requireContext()) }

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
            setupImageSlider()
            setupReviewRecyclerView()
            setupObservers()
            setupListeners()

            viewModel.fetchProductDetails(productId!!)
        } else {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy ID sản phẩm", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupImageSlider() {
        imageSliderAdapter = ImageSliderAdapter(emptyList())
        binding.vpImageSlider.adapter = imageSliderAdapter

        TabLayoutMediator(binding.tabIndicator, binding.vpImageSlider) { tab, position ->
            // Logic gắn indicator
        }.attach()
    }

    private fun setupReviewRecyclerView() {
        reviewAdapter = ReviewAdapter()
        binding.rvReviews.isNestedScrollingEnabled = false
        binding.rvReviews.adapter = reviewAdapter
    }


    private fun setupRecyclerView() {
        suggestedAdapter = ProductAdapter(onProductClick = { product ->
            navigateToProductDetail(product._id)
        })
        binding.rvSuggestedProducts.adapter = suggestedAdapter
    }

    private fun setupObservers() {
        // ... (Lắng nghe các LiveData giữ nguyên) ...

        // 1. Lắng nghe chi tiết sản phẩm
        viewModel.productDetails.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    (resource.data as? Product)?.let {
                        displayProductDetails(it)
                        currentProduct = it
                        imageSliderAdapter = ImageSliderAdapter(it.productImage)
                        binding.vpImageSlider.adapter = imageSliderAdapter
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

        // 3. Lắng nghe Reviews
        viewModel.productReviews.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val reviews = resource.data as? List<ProductReview>
                    reviewAdapter.submitList(reviews)
                    if (reviews?.isEmpty() == true) {
                        binding.tvReviewTitle.text = "Đánh giá và Nhận xét (Chưa có)"
                    }
                }
                is Resource.Error -> binding.tvReviewTitle.visibility = View.GONE
                else -> Unit
            }
        }

        // 4. Lắng nghe kết quả THÊM VÀO GIỎ HÀNG
        viewModel.addToCartResult.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> setLoadingButtons(true)
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
        binding.toolbar.title = product.productName
        binding.tvProductName.text = product.productName
        binding.tvBrandName.text = "Thương hiệu: ${product.brandName}"
        binding.tvDescription.text = product.description
        binding.tvSellingPrice.text = formatter.format(product.sellingPrice)
        binding.tvPrice.text = formatter.format(product.price)
        binding.tvPrice.paintFlags = binding.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        binding.ratingBar.rating = product.averageRating?.toFloat() ?: 0f
        binding.tvReviewCount.text = "(${product.numberOfReviews ?: 0} đánh giá)"
        binding.tvReviewTitle.text = "Đánh giá và Nhận xét (${product.numberOfReviews ?: 0})"
        val stockCount = product.stock ?: 0
        val stockText = if (stockCount > 0) "Tồn kho: $stockCount" else "Hết hàng"
        binding.tvStock.text = stockText
        val stockColor = if (stockCount > 0) R.color.colorTextSecondary else R.color.colorPrimaryDark
        binding.tvStock.setTextColor(requireContext().getColor(stockColor))
        binding.bottomActionLayout.visibility = View.VISIBLE
    }

    private fun navigateToProductDetail(newProductId: String) {
        val detailFragment = newInstance(newProductId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToCheckout(product: Product, quantity: Int) {
        val checkoutFragment = CheckoutFragment.newInstance(product, quantity)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, checkoutFragment)
            .addToBackStack(null)
            .commit()
    }
    // ------------------------------------------------

    private fun isUserLoggedIn(): Boolean {
        return sessionManager.fetchAuthToken() != null
    }

    private fun showLoginAndRedirect() {
        Toast.makeText(requireContext(), "Vui lòng đăng nhập để thực hiện giao dịch.", Toast.LENGTH_LONG).show()
        startActivity(Intent(requireContext(), LoginActivity::class.java))
    }

    private fun setupListeners() {
        binding.btnBuyNow.setOnClickListener {
            if (!isUserLoggedIn()) {
                showLoginAndRedirect()
                return@setOnClickListener
            }
            currentProduct?.let { product ->
                // CHUYỂN SANG CHECKOUT
                navigateToCheckout(product, quantity = 1)
            } ?: Toast.makeText(requireContext(), "Sản phẩm chưa sẵn sàng.", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddToCart.setOnClickListener {
            if (!isUserLoggedIn()) {
                showLoginAndRedirect()
                return@setOnClickListener
            }
            currentProduct?.let { product ->
                viewModel.addToCart(product._id)
            } ?: Toast.makeText(requireContext(), "Sản phẩm chưa sẵn sàng.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.bottomActionLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun setLoadingButtons(isLoading: Boolean) {
        binding.btnAddToCart.isEnabled = !isLoading
        binding.btnBuyNow.isEnabled = !isLoading
        binding.btnAddToCart.text = if (isLoading) "Đang thêm..." else "Thêm vào giỏ"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}