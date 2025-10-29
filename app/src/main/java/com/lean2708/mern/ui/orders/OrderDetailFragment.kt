package com.lean2708.mern.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lean2708.mern.R
import com.lean2708.mern.data.model.Order
import com.lean2708.mern.data.model.OrderItem
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

class OrderDetailFragment : Fragment() {
    private var _binding: FragmentOrderDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var itemAdapter: OrderItemAdapter

    // TODO: Cần tạo OrderViewModelFactory
    private val viewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(OrderRepository(RetrofitInstance.api))
    }

    private var orderId: String? = null

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
            viewModel.fetchOrderDetail(orderId!!)
        } else {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        itemAdapter = OrderItemAdapter()
        binding.rvOrderItems.adapter = itemAdapter
    }

    private fun setupObservers() {
        viewModel.orderDetail.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    (resource.data as? Order)?.let { displayOrderDetails(it) }
                }
                is Resource.Error<*> -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Lỗi tải chi tiết: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun displayOrderDetails(order: Order) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        // Cập nhật tiêu đề và thông tin
        binding.tvOrderId.text = "Mã đơn hàng: #${order._id.takeLast(10)}"
        binding.tvOrderStatus.text = "Trạng thái: ${OrderStatus.fromValue(order.orderStatus)}"
        binding.tvPaymentMethod.text = "Phương thức thanh toán: ${order.paymentMethod}" // Tạm thời
        binding.tvTotalAmount.text = "Tổng thanh toán: ${formatter.format(order.totalPrice)}"

        // Cập nhật danh sách sản phẩm
        itemAdapter.submitList(order.orderItems)

        // Hiển thị/Ẩn nút HỦY ĐƠN HÀNG
        val isPending = order.orderStatus == OrderStatus.PENDING.value
        binding.bottomCancelLayout.visibility = if (isPending) View.VISIBLE else View.GONE

        if (isPending) {
            binding.btnCancelOrder.setOnClickListener { confirmCancelOrder(order._id) }
        }
    }

    private fun confirmCancelOrder(id: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận Hủy Đơn")
            .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không? Thao tác này không thể hoàn tác.")
            .setPositiveButton("Hủy Đơn") { dialog, _ ->
                viewModel.cancelOrder(id) // Gọi API Hủy
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

    // --- ADAPTER CHO ORDER ITEMS ---
    class OrderItemAdapter : androidx.recyclerview.widget.ListAdapter<OrderItem, OrderItemAdapter.ItemViewHolder>(DiffCallback()) {

        private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        inner class ItemViewHolder(private val binding: ItemOrderDetailProductBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: OrderItem) {
                binding.tvProductName.text = item.product.productName
                binding.tvQuantity.text = "SL: x${item.quantity}"
                binding.tvUnitPrice.text = formatter.format(item.unitPrice)
                binding.tvTotalPrice.text = formatter.format(item.unitPrice * item.quantity)

                item.product.productImage.firstOrNull()?.let { url ->
                    Glide.with(binding.imgProduct.context).load(url).into(binding.imgProduct)
                }
            }
        }

        private class DiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<OrderItem>() {
            override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean = oldItem._id == newItem._id
            override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean = oldItem == newItem
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            // TODO: Bạn cần tạo file layout item_order_detail_product.xml
            val binding = ItemOrderDetailProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ItemViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }
}