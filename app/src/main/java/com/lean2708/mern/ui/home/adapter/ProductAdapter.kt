package com.lean2708.mern.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lean2708.mern.data.model.Product
import com.lean2708.mern.databinding.ItemProductBinding
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    // Thêm listener click vào constructor chính
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() { // <-- Đảm bảo class này không bị đóng

    inner class ProductViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            // Khởi tạo click listener ở đây
            binding.root.setOnClickListener {
                // Đảm bảo adapterPosition hợp lệ trước khi gọi
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onProductClick(differ.currentList[adapterPosition])
                }
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem._id == newItem._id
        }
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(
            ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.binding.apply {
            tvProductName.text = product.productName

            // Format tiền tệ
            val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
            tvProductPrice.text = formatter.format(product.sellingPrice)

            if (product.productImage.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(product.productImage[0])
                    .into(imgProduct)
            }
        }
    }

    override fun getItemCount() = differ.currentList.size
}