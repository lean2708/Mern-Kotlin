package com.lean2708.mern.repository

import com.lean2708.mern.data.model.DeleteCartRequest
import com.lean2708.mern.data.model.UpdateCartRequest
import com.lean2708.mern.data.network.ApiService

class CartRepository(private val apiService: ApiService) {
    suspend fun getCartCount() = apiService.getCartCount()
    suspend fun viewCartProducts() = apiService.viewCartProducts()
    suspend fun updateCartProduct(cartId: String, quantity: Int) =
        apiService.updateCartProduct(UpdateCartRequest(cartId, quantity))
    suspend fun deleteCartProduct(cartId: String) =
        apiService.deleteCartProduct(DeleteCartRequest(cartId))
}