package com.lean2708.mern.ui.orders.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lean2708.mern.data.model.DetailedCartItem
import com.lean2708.mern.databinding.ItemCheckoutProductBinding
import java.text.NumberFormat
import java.util.Locale

class CheckoutProductAdapter : ListAdapter<DetailedCartItem, CheckoutProductAdapter.CheckoutProductViewHolder>(DiffCallback()) {

    inner class CheckoutProductViewHolder(private val binding: ItemCheckoutProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetailedCartItem) {
            val product = item.productId
            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

            binding.tvProductName.text = product.productName
            binding.tvQuantity.text = "x${item.quantity}"
            binding.tvUnitPrice.text = formatter.format(product.sellingPrice)

            Glide.with(binding.imgProduct.context)
                .load(product.productImage.firstOrNull())
                .into(binding.imgProduct)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DetailedCartItem>() {
        override fun areItemsTheSame(oldItem: DetailedCartItem, newItem: DetailedCartItem): Boolean = oldItem._id == newItem._id
        override fun areContentsTheSame(oldItem: DetailedCartItem, newItem: DetailedCartItem): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutProductViewHolder {
        val binding = ItemCheckoutProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CheckoutProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CheckoutProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}