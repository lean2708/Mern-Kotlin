package com.lean2708.mern.data.model
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Request Body cho POST /api/addtoCart
data class AddToCartRequest(
    val productId: String
    // Backend của bạn chỉ cần productId, UserID được lấy từ Token.
)

// Response Data cho Cart Item
data class CartItem(
    val productId: String,
    val quantity: Int,
    val userId: String,
    val _id: String
    // (Bao gồm các trường khác nếu cần)
)

// Response chung cho AddToCart
data class AddToCartResponse(
    val data: CartItem,
    val message: String,
    val success: Boolean,
    val error: Boolean
)

data class CartCountData(val count: Int)
data class CartCountResponse(
    val data: CartCountData,
    val message: String,
    val error: Boolean,
    val success: Boolean
)

@Parcelize
data class CartProduct(
    val _id: String,
    val productName: String,
    val brandName: String,
    val category: String,
    val productImage: List<String>,
    val price: Long,
    val sellingPrice: Long
) : Parcelable

// SỬA LỖI: THÊM @Parcelize VÀ : Parcelable
@Parcelize
data class DetailedCartItem(
    val _id: String,
    val productId: CartProduct, // <-- Class này cũng phải là Parcelable
    val quantity: Int,
    val userId: String
) : Parcelable

data class ViewCartResponse(
    val data: List<DetailedCartItem>,
    val success: Boolean,
    val error: Boolean
)

// Request cho Update Cart (API 3)
data class UpdateCartRequest(
    val _id: String,
    val quantity: Int
)

// Request/Response cho Delete Cart (API 4)
data class DeleteCartRequest(
    val _id: String
)