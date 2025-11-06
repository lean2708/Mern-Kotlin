package com.lean2708.mern.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions // <-- Import cho circleCrop
import com.lean2708.mern.R // Import R
import com.lean2708.mern.data.model.ProductReview
import com.lean2708.mern.data.model.ReviewUser
import com.lean2708.mern.databinding.ItemProductReviewBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter : ListAdapter<ProductReview, ReviewAdapter.ReviewViewHolder>(DiffCallback()) {

    inner class ReviewViewHolder(private val binding: ItemProductReviewBinding) : RecyclerView.ViewHolder(binding.root) {

        // Khởi tạo Adapter con cho ảnh review (Giả định ReviewImageAdapter đã tồn tại)
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

            // ÉP KIỂU 'user' (Any) sang 'ReviewUser' (Object)
            val userDetails = review.user as? ReviewUser

            binding.tvUserName.text = userDetails?.name ?: "Người dùng ẩn danh"
            binding.reviewRatingBar.rating = review.rating
            binding.tvReviewComment.text = review.comment

            // Tải avatar (Sử dụng userDetails đã ép kiểu)
            if (userDetails?.profilePic.isNullOrEmpty()) {
                // Nếu avatar rỗng hoặc null, dùng ảnh mặc định
                binding.imgAvatar.setImageResource(R.drawable.ic_default_avatar) // <-- Thay bằng drawable avatar mặc định của bạn
            } else {
                Glide.with(binding.imgAvatar.context)
                    .load(userDetails!!.profilePic)
                    .apply(RequestOptions.circleCropTransform()) // SỬA: Dùng .apply()
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