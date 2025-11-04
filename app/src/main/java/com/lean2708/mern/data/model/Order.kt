package com.lean2708.mern.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

// --- TỪ FILE CHECKOUT.KT CŨ ---
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
// ---------------------------------


// --- 1. MODEL SẢN PHẨM LỒNG (Dùng cho API Get) ---
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

// --- 2. MODEL ĐỊA CHỈ LỒNG (Dùng cho API Get) ---
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

// --- 3. MODEL ITEM CHI TIẾT (Dùng cho API Get List/Detail) ---
@Parcelize
data class OrderItem(
    val product: OrderProduct,
    val quantity: Int,
    val unitPrice: Long,
    val _id: String
) : Parcelable

// --- 4. MODEL ĐƠN HÀNG CHI TIẾT (Dùng cho API Get List/Detail) ---
@Parcelize
data class Order(
    val _id: String,
    val user: String,
    val orderItems: List<OrderItem>,
    val shippingAddress: ShippingAddress?,
    val totalPrice: Long,
    val paymentMethod: String,
    val orderStatus: String,
    val orderDate: String,
    val paidAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int? = null
) : Parcelable

// --- 5. MODEL ITEM ĐƠN GIẢN (Dùng cho API Create Order) ---
@Parcelize
data class SimpleOrderItem(
    val product: String, // Chỉ là String ID
    val quantity: Int,
    val unitPrice: Long,
    val _id: String
) : Parcelable

// --- 6. MODEL ĐƠN HÀNG ĐƠN GIẢN (Dùng cho API Create Order) ---
@Parcelize
data class SimpleOrder(
    val _id: String,
    val user: String,
    val orderItems: List<SimpleOrderItem>,
    val shippingAddress: String?, // Chỉ là String ID
    val totalPrice: Long,
    val paymentMethod: String,
    val orderStatus: String,
    val orderDate: String,
    val paidAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int? = null
) : Parcelable

// --- 7. RESPONSE CHO API GET ---
@Parcelize
data class OrderListResponse(
    val data: List<Order>,
    val success: Boolean,
    val error: Boolean,
    val message: String
) : Parcelable

@Parcelize
data class OrderDetailResponse(
    val data: Order,
    val success: Boolean,
    val error: Boolean,
    val message: String
) : Parcelable

// --- 8. RESPONSE CHO API CREATE (VNPAY) ---
// (Đã gộp từ Checkout.kt)
data class VnpayOrderResponse(
    val data: SimpleOrder, // <-- Sửa: Dùng SimpleOrder
    val message: String,
    val success: Boolean,
    val error: Boolean,
    val paymentUrl: String?
)