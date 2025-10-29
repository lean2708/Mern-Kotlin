package com.lean2708.mern.repository

import com.lean2708.mern.data.network.ApiService

class OrderRepository(private val apiService: ApiService) {
    suspend fun getOrdersByStatus(status: String) = apiService.getOrdersByStatus(status)
    suspend fun getOrderDetail(orderId: String) = apiService.getOrderDetail(orderId)
    suspend fun cancelOrder(orderId: String) = apiService.cancelOrder(orderId)
}