package com.lean2708.mern.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.lean2708.mern.R
import com.lean2708.mern.data.model.*
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentCheckoutBinding
import com.lean2708.mern.repository.OrderRepository
import com.lean2708.mern.repository.ProfileRepository
import com.lean2708.mern.ui.home.activity.MainActivity
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

    // KHÔNG CÒN ADAPTER cho địa chỉ (Đã chuyển sang TextView tĩnh)

    private val viewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(OrderRepository(RetrofitInstance.api))
    }
    private val profileRepository: ProfileRepository by lazy { ProfileRepository(RetrofitInstance.api) }

    private var product: Product? = null
    private var quantity: Int = 1

    private var allAddresses: List<Address> = emptyList()
    private var selectedAddress: Address? = null
    private var paymentMethod: String = "CASH"

    companion object {
        const val ARG_PRODUCT = "product_data"
        const val ARG_QUANTITY = "quantity_data"
        fun newInstance(product: Product, quantity: Int): CheckoutFragment {
            val fragment = CheckoutFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(ARG_PRODUCT, product)
                putInt(ARG_QUANTITY, quantity)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        product = arguments?.getParcelable(ARG_PRODUCT)
        quantity = arguments?.getInt(ARG_QUANTITY) ?: 1
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

        setupProductInfo()
        setupObservers()
        setupListeners()
        setupAddressResultListener() // Kích hoạt lắng nghe kết quả

        viewModel.fetchDefaultAddress(profileRepository)
    }

    private fun setupAddressResultListener() {
        parentFragmentManager.setFragmentResultListener(
            ChangeAddressFragment.REQUEST_ADDRESS_KEY,
            viewLifecycleOwner
        ) { key, bundle ->

            val newAddress: Address? = bundle.getParcelable(ChangeAddressFragment.BUNDLE_ADDRESS_KEY)

            newAddress?.let {
                // Cập nhật địa chỉ đã chọn và UI
                selectedAddress = it
                updateAddressUI(it)
                binding.btnPlaceOrder.isEnabled = true
            }
        }
    }

    private fun setupProductInfo() {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        binding.tvProductName.text = product?.productName ?: "Sản phẩm không xác định"
        binding.tvQuantity.text = "x$quantity"
        binding.tvUnitPrice.text = formatter.format(product?.sellingPrice ?: 0)

        // Tải ảnh sản phẩm
        product?.productImage?.firstOrNull()?.let { url ->
            Glide.with(this).load(url).into(binding.imgProduct)
        }

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

                    // Chọn địa chỉ mặc định (hoặc cái đầu tiên)
                    val defaultAddr = addresses.firstOrNull { it.isDefault } ?: addresses.firstOrNull()
                    selectedAddress = defaultAddr

                    // CẬP NHẬT TEXT VIEW PHỤC HỒI
                    updateAddressUI(defaultAddr)
                    binding.btnPlaceOrder.isEnabled = defaultAddr != null
                }
                is Resource.Error -> {
                    binding.btnPlaceOrder.isEnabled = false
                    Toast.makeText(requireContext(), "Lỗi tải địa chỉ. Vui lòng thử lại.", Toast.LENGTH_SHORT).show()
                    updateAddressUI(null) // Hiển thị trạng thái lỗi
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
        // Cập nhật TextView với địa chỉ đã chọn (TV ĐƠN GIẢN)
        binding.tvSelectedAddressDetail.text = address?.addressDetail ?: "Chưa có địa chỉ nào được chọn"
        binding.tvSelectedPhone.text = address?.phone?.let { "SĐT: $it" } ?: "SĐT: N/A"

        binding.btnPlaceOrder.isEnabled = address != null
    }

    private fun updateTotalSummary() {
        val total = (product?.sellingPrice ?: 0) * quantity
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        binding.tvTotalAmount.text = formatter.format(total)
    }

    private fun placeOrder() {
        if (selectedAddress == null || product == null) {
            Toast.makeText(requireContext(), "Thiếu thông tin đặt hàng.", Toast.LENGTH_SHORT).show()
            return
        }

        val productList = listOf(CheckoutProduct(product!!._id, quantity))

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