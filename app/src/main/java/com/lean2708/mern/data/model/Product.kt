package com.lean2708.mern.data.model

data class Product(
    val _id: String,
    val productName: String,
    val brandName: String,
    val category: String,
    val productImage: List<String>,
    val description: String,
    val price: Long,
    val sellingPrice: Long,
    val stock: Int? = null,
    val averageRating: Double? = null,
    val numberOfReviews: Int? = null
)


data class ReviewUser(
    val _id: String,
    val name: String,
    val profilePic: String?
)

data class ProductReview(
    val _id: String,
    val user: ReviewUser,
    val product: String,
    val rating: Int,
    val comment: String,
    val reviewImages: List<String>?,
    val createdAt: String,
    // ...
)

data class ReviewListResponse(
    val data: List<ProductReview>,
    val success: Boolean,
    val error: Boolean,
    val message: String,
    val reviewCount: Int
)