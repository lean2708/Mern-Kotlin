package com.lean2708.mern.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize



// --- 2. MODEL REVIEW CHI TIẾT (Dùng cho API 3 Get Detail) ---
@Parcelize
data class DetailedProductReview(
    val _id: String,
    val user: ReviewUser, // LÀ OBJECT
    val product: String, // LÀ STRING ID
    val rating: Int,
    val comment: String,
    val reviewImages: List<String>?,
    val createdAt: String,
    val updatedAt: String? = null,
    val __v: Int? = null
) : Parcelable

// --- 3. MODEL REVIEW ĐƠN GIẢN (Dùng cho API 2 Create & 4 Update) ---
@Parcelize
data class SimpleProductReview(
    val _id: String,
    val user: String, // LÀ STRING ID
    val product: String, // LÀ STRING ID
    val rating: Int,
    val comment: String,
    val reviewImages: List<String>?,
    val createdAt: String,
    val updatedAt: String? = null,
    val __v: Int? = null
) : Parcelable

// --- 4. MODEL LỒNG CHO DATA TRONG CHECK REVIEW (API 1) ---
@Parcelize
data class ReviewCheckData(
    val _id: String,
    val rating: Int,
    val comment: String,
    val createdAt: String
) : Parcelable

// --- 5. API 1: CHECK REVIEW ---
data class CheckReviewRequest(val productId: String)
data class CheckReviewResponse(
    val message: String,
    val hasReviewed: Boolean,
    val success: Boolean,
    val error: Boolean,
    val data: ReviewCheckData? = null
)

// --- 6. API 2 & 4: CREATE/UPDATE REVIEW ---
data class CreateReviewRequest(
    val product: String, // Product ID
    val rating: Int,
    val comment: String,
    val reviewImages: List<String>
)
data class UpdateReviewRequest(
    val rating: Int,
    val comment: String,
    val reviewImages: List<String>
)

// --- 7. API 5: UPLOAD IMAGE ---
data class ImageUploadResult(
    val publicId: String,
    val fileName: String,
    val imageUrl: String
)
data class UploadImageResponse(
    val code: Int,
    val message: String,
    val result: List<ImageUploadResult>,
    val success: Boolean
)

// --- 9. RESPONSE CHO API GetProductReviews ---
data class ReviewListResponse(
    val data: List<ProductReview>, // SỬA: Dùng ProductReview
    val success: Boolean,
    val error: Boolean,
    val message: String,
    val reviewCount: Int
)