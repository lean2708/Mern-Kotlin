package com.lean2708.mern.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
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
) : Parcelable
