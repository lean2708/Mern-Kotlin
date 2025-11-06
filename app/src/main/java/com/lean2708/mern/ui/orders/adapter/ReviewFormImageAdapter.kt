package com.lean2708.mern.ui.orders.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lean2708.mern.databinding.ItemReviewImageFormBinding

// Adapter này xử lý Uri (ảnh mới) hoặc String (ảnh cũ đã upload)
class ReviewFormImageAdapter(
    private val onAddClick: () -> Unit,
    private val onDeleteClick: (Any) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback()) {

    private val VIEW_TYPE_ADD = 0
    private val VIEW_TYPE_IMAGE = 1

    // ViewHolder cho Ảnh
    inner class ImageViewHolder(private val binding: ItemReviewImageFormBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(image: Any) {
            Glide.with(binding.imgReview.context)
                .load(image) // Glide có thể load (Uri hoặc String)
                .centerCrop()
                .into(binding.imgReview)

            binding.btnDeleteImage.setOnClickListener {
                onDeleteClick(image)
            }
        }
    }

    // ViewHolder cho nút Thêm Ảnh (Nếu bạn muốn)
    // (Tạm thời chúng ta dùng nút riêng, Adapter này chỉ hiển thị)

    private class DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemReviewImageFormBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ImageViewHolder).bind(getItem(position))
    }
}