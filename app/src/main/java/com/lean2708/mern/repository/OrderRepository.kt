package com.lean2708.mern.repository

import com.lean2708.mern.data.model.CreateOrderRequest
import com.lean2708.mern.data.network.ApiService

class OrderRepository(private val apiService: ApiService) {
    suspend fun getOrdersByStatus(status: String) = apiService.getOrdersByStatus(status)
    suspend fun getOrderDetail(orderId: String) = apiService.getOrderDetail(orderId)
    suspend fun cancelOrder(orderId: String) = apiService.cancelOrder(orderId)

    suspend fun createCashOrder(request: CreateOrderRequest) = apiService.createCashOrder(request)

    suspend fun createVnpayOrder(request: CreateOrderRequest) = apiService.createVnpayOrder(request)
    suspend fun checkVnpayCallback(params: Map<String, String>) = apiService.handleVnpayReturn(params)
}