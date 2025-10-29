package com.lean2708.mern.repository

import com.lean2708.mern.data.model.AddToCartRequest
import com.lean2708.mern.data.model.CategoryRequest
import com.lean2708.mern.data.model.ProductDetailRequest
import com.lean2708.mern.data.network.ApiService

class HomeRepository(private val apiService: ApiService) {

    suspend fun getCategoryProducts() = apiService.getCategoryProducts()

    suspend fun getProductsForCategory(categoryName: String) =
        apiService.getProductsForCategory(CategoryRequest(categoryName))

    suspend fun getProductDetails(productId: String) =
        apiService.getProductDetails(ProductDetailRequest(productId))

    suspend fun addToCart(productId: String) =
        apiService.addToCart(AddToCartRequest(productId))


    suspend fun searchProducts(query: String) = apiService.searchProducts(query)
}