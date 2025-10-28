package com.lean2708.mern.data.model

// Dùng chung cho các response trả về 1 đối tượng (User, Address)
data class DataResponse<T>(
    val data: T,
    val message: String,
    val success: Boolean,
    val error: Boolean
)

// Dùng chung cho các response trả về 1 danh sách (List<Address>)
data class ListDataResponse<T>(
    val data: List<T>,
    val message: String,
    val success: Boolean,
    val error: Boolean
)

// Response cho POST /api/upload-avatar (hơi khác)
data class AvatarResponse(
    val code: Int,
    val message: String,
    val data: User,
    val success: Boolean,
    val error: Boolean
)


data class ProductListResponse(
    val message: String,
    val data: List<Product>,
    val success: Boolean,
    val error: Boolean
)