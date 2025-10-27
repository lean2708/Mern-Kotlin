package com.lean2708.mern.data.model


data class ProductListResponse(
    val message: String,
    val data: List<Product>,
    val success: Boolean,
    val error: Boolean
)