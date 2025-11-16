package com.lean2708.mern.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // SỬA: Dùng activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lean2708.mern.R
import com.lean2708.mern.data.model.*
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentOrderDetailBinding
import com.lean2708.mern.databinding.ItemOrderDetailProductBinding
import com.lean2708.mern.repository.OrderRepository
import com.lean2708.mern.ui.viewmodel.OrderViewModel
import com.lean2708.mern.ui.viewmodel.OrderStatus
import com.lean2708.mern.ui.viewmodel.OrderViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource
import java.text.NumberFormat
import java.util.Locale
import android.util.Log

class OrderDetailFragment : Fragment() {
    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemAdapter: OrderItemAdapter

    // SỬA LỖI: Sử dụng 'activityViewModels'
    private val viewModel: OrderViewModel by activityViewModels {
        OrderViewModelFactory(OrderRepository(RetrofitInstance.api))
    }

    private var orderId: String? = null
    private var reviewStatusMap: Map<String, CheckReviewResponse> = emptyMap()
    private var currentOrder: Order? = null

    companion object {
        const val ARG_ORDER_ID = "order_id"
        fun newInstance(orderId: String): OrderDetailFragment {
            val fragment = OrderDetailFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_ORDER_ID, orderId)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderId = arguments?.getString(ARG_ORDER_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        setupRecyclerView()
        setupObservers()

        if (orderId != null) {
            viewModel.fetchOrderDetail(orderId!!) // Gọi API chi tiết
        } else {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        itemAdapter = OrderItemAdapter(
            onReviewClick = { productId, reviewId ->
                navigateToReviewForm(productId, reviewId)
            }
        )
        binding.rvOrderItems.adapter = itemAdapter
        binding.rvOrderItems.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.rvOrderItems.isNestedScrollingEnabled = false
    }

    private fun setupObservers() {
        // 1. Lắng nghe chi tiết đơn hàng
        viewModel.orderDetail.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    (resource.data as? Order)?.let { order ->
                        currentOrder = order
                        displayOrderDetails(order)

                        if (order.orderStatus == OrderStatus.DELIVERED.value) {
                            Log.i("ReviewCheck", "Đơn hàng ĐÃ GIAO HÀNG. Đang gọi API Check Review...")
                            viewModel.checkReviewStatusForItems(order.orderItems)
                        } else {
                            Log.w("ReviewCheck", "Đơn hàng CHƯA GIAO. Không gọi API Check Review.")
                        }
                    }
                }
                is Resource.Error<*> -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Lỗi tải chi tiết: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }

        // 2. Lắng nghe KẾT QUẢ CHECK REVIEW (API 1)
        viewModel.reviewStatusMap.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                Log.i("ReviewCheck", "Đã nhận kết quả Check Review. Cập nhật Adapter.")
                reviewStatusMap = resource.data ?: emptyMap()
                itemAdapter.setReviewStatusMap(reviewStatusMap)
                itemAdapter.notifyDataSetChanged()
            } else if (resource is Resource.Error) {
                Log.e("ReviewCheck", "Lỗi khi Check Review: ${resource.message}")
            }
        }

        // 3. Lắng nghe kết quả Hủy đơn hàng (API 3)
        viewModel.cancelOrderResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Đơn hàng đã hủy thành công!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                is Resource.Error -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Không thể hủy: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }
    }

    private fun displayOrderDetails(order: Order) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        binding.tvOrderId.text = "Mã đơn hàng: #${order._id.takeLast(10)}"
        binding.tvOrderStatus.text = "Trạng thái: ${OrderStatus.fromValue(order.orderStatus)}"
        binding.tvPaymentMethod.text = "Phương thức thanh toán: ${order.paymentMethod}"
        binding.tvTotalAmount.text = "Tổng thanh toán: ${formatter.format(order.totalPrice)}"

        val shippingAddressObject = order.shippingAddress as? ShippingAddress
        if (shippingAddressObject != null) {
            binding.tvAddressDetail.text = shippingAddressObject.addressDetail
            binding.tvPhone.text = "SĐT: ${shippingAddressObject.phone}"
        } else {
            val addressId = order.shippingAddress as? String
            binding.tvAddressDetail.text = if (addressId != null) "Địa chỉ ID: $addressId" else "Địa chỉ không xác định"
            binding.tvPhone.text = "SĐT: N/A"
        }

        itemAdapter.setOrderStatus(order.orderStatus)
        itemAdapter.setReviewStatusMap(reviewStatusMap)
        itemAdapter.submitList(order.orderItems)

        val isPending = order.orderStatus == OrderStatus.PENDING.value
        binding.bottomCancelLayout.visibility = if (isPending) View.VISIBLE else View.GONE

        if (isPending) {
            binding.btnCancelOrder.setOnClickListener { confirmCancelOrder(order._id) }
        }
    }

    private fun navigateToReviewForm(productId: String, reviewId: String?) {
        // TODO: Tạo ReviewFormFragment
        Toast.makeText(requireContext(), "Mở Form Review cho Product: $productId (ReviewID: $reviewId)", Toast.LENGTH_SHORT).show()
    }

    private fun confirmCancelOrder(id: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận Hủy Đơn")
            .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không? Thao tác này không thể hoàn tác.")
            .setPositiveButton("Hủy Đơn") { dialog, _ ->
                viewModel.cancelOrder(id)
                dialog.dismiss()
            }
            .setNegativeButton("Không") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarDetail.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- ADAPTER CHO ORDER ITEMS (ĐÃ BỔ SUNG LOGIC REVIEW) ---
    class OrderItemAdapter(
        private val onReviewClick: (productId: String, reviewId: String?) -> Unit
    ) : androidx.recyclerview.widget.ListAdapter<OrderItem, OrderItemAdapter.ItemViewHolder>(DiffCallback()) {

        private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
        private var orderStatus: String = "PENDING"
        private var reviewStatusMap: Map<String, CheckReviewResponse> = emptyMap()

        fun setOrderStatus(status: String) {
            orderStatus = status
        }
        fun setReviewStatusMap(map: Map<String, CheckReviewResponse>) {
            reviewStatusMap = map
        }

        inner class ItemViewHolder(private val binding: ItemOrderDetailProductBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: OrderItem) {
                val productDetails = item.product as? OrderProduct ?: return

                binding.tvProductName.text = productDetails.productName
                binding.tvQuantity.text = "SL: x${item.quantity}"
                binding.tvUnitPrice.text = formatter.format(item.unitPrice)
                binding.tvTotalPrice.text = formatter.format(item.unitPrice * item.quantity)

                productDetails.productImage.firstOrNull()?.let { url ->
                    Glide.with(binding.imgProduct.context).load(url).into(binding.imgProduct)
                }

                // --- LOGIC HIỂN THỊ NÚT ĐÁNH GIÁ (SỬA LỖI) ---
                if (orderStatus == OrderStatus.DELIVERED.value) {
                    val reviewInfo = reviewStatusMap[productDetails._id]

                    if (reviewInfo != null) {
                        binding.btnReview.visibility = View.VISIBLE

                        // SỬA LỖI: Lấy reviewId từ reviewInfo.data._id
                        val existingReviewId = reviewInfo.data?._id

                        binding.btnReview.text = if (reviewInfo.hasReviewed) "Sửa đánh giá" else "Đánh giá ngay"

                        binding.btnReview.setOnClickListener {
                            // Truyền ID sản phẩm và ID review (nếu có)
                            onReviewClick(productDetails._id, existingReviewId)
                        }
                    } else {
                        binding.btnReview.visibility = View.GONE
                    }
                } else {
                    binding.btnReview.visibility = View.GONE
                }
            }
        }

        private class DiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<OrderItem>() {
            override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean = oldItem._id == newItem._id
            override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean = oldItem == newItem
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val binding = ItemOrderDetailProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ItemViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }
}