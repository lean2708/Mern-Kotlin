package com.lean2708.mern.data.model

data class Product(
    val _id: String,
    val productName: String,
    val brandName: String,
    val category: String,
    val productImage: List<String>, // Mảng các URL ảnh
    val description: String,
    val price: Long,
    val sellingPrice: Long
)