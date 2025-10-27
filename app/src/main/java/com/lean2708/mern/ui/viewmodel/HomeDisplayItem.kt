package com.lean2708.mern.ui.viewmodel

import com.lean2708.mern.data.model.Product

sealed class HomeDisplayItem {
    // Đại diện cho RecyclerView ngang đầu tiên (danh sách Categories)
    data class Categories(val representativeProducts: List<Product>) : HomeDisplayItem()

    // Đại diện cho một mục "Sản phẩm theo Category" (gồm Tiêu đề + RecyclerView ngang)
    data class ProductSection(val categoryName: String, val products: List<Product>) : HomeDisplayItem()
}