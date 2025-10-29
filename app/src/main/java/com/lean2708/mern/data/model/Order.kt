package com.lean2708.mern.data.model

import android.os.Parcelable // Cần thiết nếu bạn muốn truyền Order/ShippingAddress qua Bundle
import kotlinx.parcelize.Parcelize
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


// --- 2. MODEL CHO TỪNG ITEM CỦA ĐƠN HÀNG ---
@Parcelize
data class OrderItem(
    val product: OrderProduct,
    val quantity: Int,
    val unitPrice: Long,
    val _id: String
) : Parcelable


// --- 3. MODEL CHO ĐỊA CHỈ GIAO HÀNG ---
@Parcelize
data class ShippingAddress(
    val _id: String,
    val user: String,
    val phone: String,
    val addressDetail: String,
    val isDefault: Boolean,

    // Các trường tùy chọn từ database
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int? = null,
) : Parcelable


// --- 4. MODEL CHO ĐƠN HÀNG CHUNG ---
@Parcelize
data class Order(
    val _id: String,
    val user: String,
    val orderItems: List<OrderItem>,

    // ĐÃ SỬA LỖI: Sử dụng OBJECT ShippingAddress? thay vì String?
    val shippingAddress: ShippingAddress?,

    val totalPrice: Long,
    val paymentMethod: String,
    val orderStatus: String, // PENDING, SHIPPED, etc.
    val orderDate: String,

    // Thêm paidAt nếu nó xuất hiện trong response (như log bạn cung cấp)
    val paidAt: String? = null,
) : Parcelable


// --- 5. RESPONSE CHO DANH SÁCH ĐƠN HÀNG ---
@Parcelize
data class OrderListResponse(
    val data: List<Order>,
    val success: Boolean,
    val error: Boolean,
    val message: String
) : Parcelable


// --- 6. RESPONSE CHO CHI TIẾT ĐƠN HÀNG ---
@Parcelize
data class OrderDetailResponse(
    val data: Order, // Dùng lại Order model
    val success: Boolean,
    val error: Boolean,
    val message: String
) : Parcelable