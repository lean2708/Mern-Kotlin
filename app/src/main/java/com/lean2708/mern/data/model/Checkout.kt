package com.lean2708.mern.data.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

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

// Response cho VNPAY (Chứa URL)
data class VnpayOrderResponse(
    val data: Order,
    val message: String,
    val success: Boolean,
    val error: Boolean,
    val paymentUrl: String?
)

// Response cho VNPAY Callback (API 4 - Đơn giản là GenericResponse)