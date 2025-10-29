package com.lean2708.mern.ui.orders.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lean2708.mern.data.model.Order
import com.lean2708.mern.databinding.ItemOrderSummaryBinding
import com.lean2708.mern.ui.viewmodel.OrderStatus
import java.text.NumberFormat
import java.util.Locale


class OrderAdapter(
    private val onDetailClick: (String) -> Unit
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(DiffCallback()) {

    private val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    inner class OrderViewHolder(private val binding: ItemOrderSummaryBinding) : RecyclerView.ViewHolder(binding.root) {

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

            // Cập nhật Header
            binding.tvStatus.text = OrderStatus.fromValue(order.orderStatus)
            binding.tvTotalPrice.text = formatter.format(order.totalPrice)

            // Cập nhật Footer
            binding.tvOrderId.text = "Mã ĐH: #${order._id.takeLast(10)}"

            // LƯU Ý: Đã XÓA tham chiếu đến binding.tvOrderDate

            // Cập nhật thông tin sản phẩm đại diện
            if (firstItem != null) {
                // 1. Tên sản phẩm
                binding.tvProductName.text = firstItem.product.productName

                // 2. Số lượng và Tổng tiền
                binding.tvItemCount.text = "$totalQuantity sản phẩm"

                // 3. Ảnh
                firstItem.product.productImage.firstOrNull()?.let { url ->
                    Glide.with(binding.imgProduct.context)
                        .load(url)
                        .into(binding.imgProduct)
                }
            } else {
                binding.tvProductName.text = "(Không có sản phẩm)"
            }

            // Gán Listener cho nút xem chi tiết (mũi tên)
            binding.btnViewDetail.setOnClickListener { onDetailClick(order._id) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean = oldItem._id == newItem._id
        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}