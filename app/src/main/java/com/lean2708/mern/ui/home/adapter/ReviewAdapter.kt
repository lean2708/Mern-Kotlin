package com.lean2708.mern.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lean2708.mern.data.model.ProductReview
import com.lean2708.mern.databinding.ItemProductReviewBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter : ListAdapter<ProductReview, ReviewAdapter.ReviewViewHolder>(DiffCallback()) {

    inner class ReviewViewHolder(private val binding: ItemProductReviewBinding) : RecyclerView.ViewHolder(binding.root) {

        // Khởi tạo Adapter con cho ảnh review
        private val reviewImageAdapter = ReviewImageAdapter()

        init {
            // Thiết lập LayoutManager và Adapter cho ảnh review
            binding.rvReviewImages.adapter = reviewImageAdapter
            binding.rvReviewImages.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                binding.root.context,
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                false
            )
        }

        fun bind(review: ProductReview) {
            // Định dạng ngày (Chuyển ISO sang định dạng dễ đọc - Yêu cầu logic Date)
            // Tạm thời hiển thị 10 ký tự đầu tiên của chuỗi ISO
            binding.tvReviewDate.text = review.createdAt.substring(0, 10)

            binding.tvUserName.text = review.user.name
            binding.reviewRatingBar.rating = review.rating.toFloat()
            binding.tvReviewComment.text = review.comment

            // Tải avatar
            review.user.profilePic?.let { url ->
                Glide.with(binding.imgAvatar.context).load(url).circleCrop().into(binding.imgAvatar)
            }

            // Xử lý Ảnh Review Đính kèm
            val reviewImages = review.reviewImages
            if (reviewImages?.isNotEmpty() == true) {
                binding.rvReviewImages.visibility = View.VISIBLE
                reviewImageAdapter.submitList(reviewImages) // Gán danh sách ảnh cho Adapter con
            } else {
                binding.rvReviewImages.visibility = View.GONE
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ProductReview>() {
        override fun areItemsTheSame(oldItem: ProductReview, newItem: ProductReview): Boolean = oldItem._id == newItem._id
        override fun areContentsTheSame(oldItem: ProductReview, newItem: ProductReview): Boolean = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        // Layout item_product_review.xml phải tồn tại
        val binding = ItemProductReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}