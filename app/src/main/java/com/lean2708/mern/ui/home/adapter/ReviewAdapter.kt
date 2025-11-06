package com.lean2708.mern.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions // Cần import
import com.lean2708.mern.data.model.ProductReview
import com.lean2708.mern.data.model.ReviewUser // Cần import ReviewUser
import com.lean2708.mern.databinding.ItemProductReviewBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter : ListAdapter<ProductReview, ReviewAdapter.ReviewViewHolder>(DiffCallback()) {

    inner class ReviewViewHolder(private val binding: ItemProductReviewBinding) : RecyclerView.ViewHolder(binding.root) {

        private val reviewImageAdapter = ReviewImageAdapter()

        init {
            binding.rvReviewImages.adapter = reviewImageAdapter
            binding.rvReviewImages.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                binding.root.context,
                androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                false
            )
        }

        fun bind(review: ProductReview) {
            binding.tvReviewDate.text = review.createdAt.substring(0, 10)

            // --- SỬA LỖI: KHÔNG CẦN ÉP KIỂU NỮA ---
            // val userDetails = review.user as? ReviewUser (Đã xóa)

            binding.tvUserName.text = review.user.name // Truy cập trực tiếp
            binding.reviewRatingBar.rating = review.rating // API trả về 4.5 (Float)
            binding.tvReviewComment.text = review.comment

            // Tải avatar
            review.user.profilePic?.let { url ->
                Glide.with(binding.imgAvatar.context)
                    .load(url)
                    .apply(RequestOptions.circleCropTransform()) // Dùng .apply()
                    .into(binding.imgAvatar)
            }

            // Xử lý Ảnh Review Đính kèm
            val reviewImages = review.reviewImages
            if (reviewImages?.isNotEmpty() == true) {
                binding.rvReviewImages.visibility = View.VISIBLE
                reviewImageAdapter.submitList(reviewImages)
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
        val binding = ItemProductReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}