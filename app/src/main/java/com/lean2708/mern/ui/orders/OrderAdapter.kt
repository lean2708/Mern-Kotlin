package com.lean2708.mern.ui.orders.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lean2708.mern.data.model.Order
import com.lean2708.mern.data.model.OrderProduct
import com.lean2708.mern.databinding.ItemOrderSummaryBinding
import com.lean2708.mern.ui.viewmodel.OrderStatus
import java.text.NumberFormat
import java.util.Locale

class OrderAdapter(
    private val onDetailClick: (String) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(DiffCallback()) {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    inner class OrderViewHolder(private val binding: ItemOrderSummaryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Kích hoạt click trên toàn bộ thẻ (itemView)
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onDetailClick(getItem(adapterPosition)._id)
                }
            }
        }

        fun bind(order: Order) {
            val firstItem = order.orderItems.firstOrNull()
            val totalQuantity = order.orderItems.sumOf { it.quantity }

            // Cập nhật Header và Footer
            binding.tvStatus.text = OrderStatus.fromValue(order.orderStatus)
            binding.tvTotalPrice.text = formatter.format(order.totalPrice)
            binding.tvOrderId.text = "Mã ĐH: #${order._id.takeLast(10)}"

            // ⚠️ LOGIC ĐÃ SỬA: Xử lý trường product (Object OrderProduct hoặc String ID)
            val productDetails = firstItem?.product as? OrderProduct

            binding.tvItemCount.text = "$totalQuantity sản phẩm"
            binding.imgProduct.visibility = View.VISIBLE // Mặc định hiển thị

            if (productDetails != null) {
                // Trường hợp 1: Product là Object (Đã populate)
                binding.tvProductName.text = productDetails.productName

                val imageUrl = productDetails.productImage.firstOrNull()
                if (imageUrl != null) {
                    Glide.with(binding.imgProduct.context)
                        .load(imageUrl)
                        .into(binding.imgProduct)
                } else {
                    binding.imgProduct.visibility = View.INVISIBLE
                }
            } else if (firstItem?.product is String) {
                // Trường hợp 2: Product là String ID (Chưa populate)
                val productIdString = firstItem.product as String

                // Hiển thị thông báo rõ ràng hơn về tình trạng thiếu chi tiết
                binding.tvProductName.text = "Chi tiết sản phẩm đang được tải..."

                // Ẩn ảnh nếu không có chi tiết (để tránh lỗi tải ảnh)
                binding.imgProduct.visibility = View.INVISIBLE

            } else {
                // Trường hợp 3: Đơn hàng rỗng hoặc lỗi dữ liệu khác
                binding.tvProductName.text = "(Không có sản phẩm)"
                binding.imgProduct.visibility = View.INVISIBLE
            }

            binding.btnViewDetail.setOnClickListener { onDetailClick(order._id) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean =
            oldItem._id == newItem._id

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderSummaryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}