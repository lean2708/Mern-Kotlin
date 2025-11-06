package com.lean2708.mern.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // SỬA: Dùng activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.lean2708.mern.R
import com.lean2708.mern.data.model.*
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentCheckoutBinding
import com.lean2708.mern.repository.CartRepository // MỚI
import com.lean2708.mern.repository.OrderRepository
import com.lean2708.mern.repository.ProfileRepository
import com.lean2708.mern.ui.home.activity.MainActivity
import com.lean2708.mern.ui.orders.adapter.CheckoutProductAdapter
import com.lean2708.mern.ui.viewmodel.CartViewModel // MỚI
import com.lean2708.mern.ui.viewmodel.CartViewModelFactory // MỚI
import com.lean2708.mern.ui.viewmodel.OrderViewModel
import com.lean2708.mern.ui.viewmodel.OrderViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource
import java.text.NumberFormat
import java.util.Locale
import com.bumptech.glide.Glide
import android.content.Intent
import android.util.Log

class CheckoutFragment : Fragment() {
    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var checkoutProductAdapter: CheckoutProductAdapter

    // ViewModel cho logic Đặt hàng (API CreateOrder)
    private val viewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(OrderRepository(RetrofitInstance.api))
    }
    // ViewModel cho logic Giỏ hàng (Để xóa item sau khi đặt)
    private val cartViewModel: CartViewModel by activityViewModels {
        CartViewModelFactory(CartRepository(RetrofitInstance.api))
    }

    private val profileRepository: ProfileRepository by lazy { ProfileRepository(RetrofitInstance.api) }

    // Dữ liệu truyền vào (Chấp nhận cả Mua ngay và Giỏ hàng)
    private var itemsToCheckout: ArrayList<DetailedCartItem> = arrayListOf()

    private var allAddresses: List<Address> = emptyList()
    private var selectedAddress: Address? = null
    private var paymentMethod: String = "CASH"

    companion object {
        const val ARG_CART_ITEMS = "cart_items_data"

        // HÀM 1: Dùng cho "Mua Ngay" (1 sản phẩm)
        fun newInstance(product: Product, quantity: Int): CheckoutFragment {
            val fragment = CheckoutFragment()
            fragment.arguments = Bundle().apply {
                // Chuyển đổi 1 Product sang 1 List<DetailedCartItem>
                val cartItem = DetailedCartItem(
                    _id = product._id, // Dùng tạm ID sản phẩm làm Cart ID
                    productId = CartProduct( // Tạo CartProduct từ Product
                        _id = product._id,
                        productName = product.productName,
                        brandName = product.brandName,
                        category = product.category,
                        productImage = product.productImage,
                        price = product.price,
                        sellingPrice = product.sellingPrice
                    ),
                    quantity = quantity,
                    userId = ""
                )
                putParcelableArrayList(ARG_CART_ITEMS, arrayListOf(cartItem))
            }
            return fragment
        }

        // HÀM 2: Dùng cho "Thanh toán Giỏ hàng"
        fun newInstance(items: ArrayList<DetailedCartItem>): CheckoutFragment {
            val fragment = CheckoutFragment()
            fragment.arguments = Bundle().apply {
                putParcelableArrayList(ARG_CART_ITEMS, items)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemsToCheckout = arguments?.getParcelableArrayList(ARG_CART_ITEMS) ?: arrayListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        setupCheckoutRecyclerView() // Setup RecyclerView sản phẩm
        setupObservers()
        setupListeners()
        setupAddressResultListener()

        viewModel.fetchDefaultAddress(profileRepository)
    }

    private fun setupAddressResultListener() {
        parentFragmentManager.setFragmentResultListener(
            ChangeAddressFragment.REQUEST_ADDRESS_KEY,
            viewLifecycleOwner
        ) { key, bundle ->
            val newAddress: Address? = bundle.getParcelable(ChangeAddressFragment.BUNDLE_ADDRESS_KEY)
            newAddress?.let {
                selectedAddress = it
                updateAddressUI(it)
                binding.btnPlaceOrder.isEnabled = true
            }
        }
    }

    // Hiển thị danh sách sản phẩm (Mua ngay hoặc Giỏ hàng)
    private fun setupCheckoutRecyclerView() {
        checkoutProductAdapter = CheckoutProductAdapter()
        binding.rvCheckoutItems.adapter = checkoutProductAdapter
        binding.rvCheckoutItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCheckoutItems.isNestedScrollingEnabled = false

        checkoutProductAdapter.submitList(itemsToCheckout)
        updateTotalSummary()
    }

    private fun setupObservers() {
        // 1. Lắng nghe địa chỉ (Tải toàn bộ List)
        viewModel.defaultAddress.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    @Suppress("UNCHECKED_CAST")
                    val addresses = resource.data as? List<Address> ?: emptyList()
                    allAddresses = addresses
                    val defaultAddr = addresses.firstOrNull { it.isDefault } ?: addresses.firstOrNull()
                    selectedAddress = defaultAddr
                    updateAddressUI(defaultAddr)
                    binding.btnPlaceOrder.isEnabled = defaultAddr != null
                }
                is Resource.Error -> {
                    binding.btnPlaceOrder.isEnabled = false
                    Toast.makeText(requireContext(), "Lỗi tải địa chỉ. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                    updateAddressUI(null)
                }
                is Resource.Loading -> { /* ... */ }
            }
        }

        // 2. Lắng nghe kết quả ĐẶT HÀNG (CASH)
        viewModel.cashOrderResult.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Đặt hàng thành công!", Toast.LENGTH_LONG).show()

                    // SỬA LỖI: GỌI HÀM XÓA GIỎ HÀNG (Nếu là thanh toán từ Giỏ hàng)
                    // (Chỉ xóa nếu 'Mua ngay' không dùng chung logic item)
                    // Để an toàn, ta luôn gọi hàm xóa các item đã chọn
                    cartViewModel.clearSelectedItemsFromCart()

                    (activity as? MainActivity)?.selectBottomNavItem(R.id.nav_orders)
                    parentFragmentManager.popBackStack()
                }
                is Resource.Error -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Lỗi đặt hàng: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }

        // 3. Lắng nghe kết quả ĐẶT HÀNG (VNPAY)
        viewModel.vnpayOrderResult.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    val vnpayResponse = resource.data as? VnpayOrderResponse
                    vnpayResponse?.paymentUrl?.let { url ->
                        navigateToVnpay(url)
                    }
                }
                is Resource.Error -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Lỗi VNPAY: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }
    }

    private fun setupListeners() {
        binding.btnPlaceOrder.setOnClickListener { placeOrder() }

        binding.rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            paymentMethod = when(checkedId) {
                binding.rbCash.id -> "CASH"
                binding.rbVnpay.id -> "VNPAY"
                else -> "CASH"
            }
        }

        binding.btnChangeAddress.setOnClickListener {
            navigateToChangeAddress()
        }
    }

    private fun updateAddressUI(address: Address?) {
        binding.tvSelectedAddressDetail.text = address?.addressDetail ?: "Chưa có địa chỉ nào được chọn"
        binding.tvSelectedPhone.text = address?.phone?.let { "SĐT: $it" } ?: "SĐT: N/A"
        binding.btnPlaceOrder.isEnabled = address != null
    }

    private fun updateTotalSummary() {
        // SỬA: Tính tổng tiền từ danh sách
        val total = itemsToCheckout.sumOf { (it.productId.sellingPrice) * it.quantity }
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        binding.tvTotalAmount.text = formatter.format(total)
    }

    private fun placeOrder() {
        if (selectedAddress == null || itemsToCheckout.isEmpty()) {
            Toast.makeText(requireContext(), "Thiếu thông tin đặt hàng.", Toast.LENGTH_SHORT).show()
            return
        }

        // SỬA: Map từ List<DetailedCartItem> sang List<CheckoutProduct>
        val productList = itemsToCheckout.map {
            CheckoutProduct(productId = it.productId._id, quantity = it.quantity)
        }

        val request = CreateOrderRequest(
            shippingAddressId = selectedAddress!!._id,
            paymentMethod = paymentMethod,
            products = productList
        )

        viewModel.createOrder(request)
    }

    // --- ĐIỀU HƯỚNG SANG CÁC FRAGMENT KHÁC ---
    private fun navigateToVnpay(url: String) {
        val vnpayFragment = VnPayWebViewFragment.newInstance(url)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, vnpayFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToChangeAddress() {
        val changeAddressFragment = ChangeAddressFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, changeAddressFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarForm.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnPlaceOrder.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}