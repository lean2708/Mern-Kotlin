package com.lean2708.mern.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// --- A. MODELS CHO THANH TOÁN (CHECKOUT) ---

@Parcelize
data class CheckoutProduct(
    val productId: String,
    val quantity: Int
) : Parcelable

data class CreateOrderRequest(
    val shippingAddressId: String,
    val paymentMethod: String,
    val products: List<CheckoutProduct>
)

// --- B. MODELS CHI TIẾT (Dùng khi XEM/GET đơn hàng) ---
// (Bao gồm các đối tượng lồng nhau đầy đủ)

@Parcelize
data class OrderProduct(
    val _id: String,
    val productName: String,
    val brandName: String? = null,
    val category: String? = null,
    val description: String? = null,
    val price: Long? = null,
    val sellingPrice: Long? = null,
    val productImage: List<String>
) : Parcelable

@Parcelize
data class ShippingAddress(
    val _id: String,
    val user: String,
    val phone: String,
    val addressDetail: String,
    val isDefault: Boolean,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int? = null
) : Parcelable

@Parcelize
data class OrderItem(
    val product: OrderProduct, // API Get trả về Object
    val quantity: Int,
    val unitPrice: Long,
    val _id: String
) : Parcelable

@Parcelize
data class Order(
    val _id: String,
    val user: String,
    val orderItems: List<OrderItem>,
    val shippingAddress: ShippingAddress?, // API Get trả về Object
    val totalPrice: Long,
    val paymentMethod: String,
    val orderStatus: String,
    val orderDate: String,
    val paidAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int? = null
) : Parcelable

// --- C. MODELS ĐƠN GIẢN (Dùng khi TẠO/CREATE đơn hàng) ---
// (Chỉ chứa String ID)

@Parcelize
data class SimpleOrderItem(
    val product: String, // API Create trả về String ID
    val quantity: Int,
    val unitPrice: Long,
    val _id: String
) : Parcelable

@Parcelize
data class SimpleOrder(
    val _id: String,
    val user: String,
    val orderItems: List<SimpleOrderItem>,
    val shippingAddress: String?, // API Create trả về String ID
    val totalPrice: Long,
    val paymentMethod: String,
    val orderStatus: String,
    val orderDate: String,
    val paidAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int? = null
) : Parcelable

// --- D. MODELS RESPONSE (Kết quả API) ---

@Parcelize
data class OrderListResponse(
    val data: List<Order>, // Dùng Order chi tiết
    val success: Boolean,
    val error: Boolean,
    val message: String
) : Parcelable


@Parcelize
data class ReviewUser(
    val _id: String,
    val name: String,
    val profilePic: String? = null // API List trả về trường này
) : Parcelable


// --- 8. MODEL PRODUCTREVIEW  ---
// (Dùng cho API @GET("review/product/{productId}"))
@Parcelize
data class ProductReview(
    val _id: String,
    val user: ReviewUser, // SỬA: Luôn là Object ReviewUser
    val product: String, // SỬA: Luôn là String ID
    val rating: Float, // API trả về 4.5 (Float/Double)
    val comment: String,
    val reviewImages: List<String>?,
    val createdAt: String,
    val updatedAt: String? = null,
    val __v: Int? = null
) : Parcelable

data class VnpayOrderResponse(
    val data: SimpleOrder, // Dùng SimpleOrder
    val message: String,
    val success: Boolean,
    val error: Boolean,
    val paymentUrl: String?
)

@Parcelize
data class OrderDetailResponse(
    val data: Order, // Dùng Order chi tiết
    val success: Boolean,
    val error: Boolean,
    val message: String
) : Parcelable