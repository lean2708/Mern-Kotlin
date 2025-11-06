package com.lean2708.mern.repository

import com.lean2708.mern.data.model.CreateOrderRequest
import com.lean2708.mern.data.network.ApiService
import com.lean2708.mern.data.model.CheckReviewRequest
import com.lean2708.mern.data.model.CreateReviewRequest
import com.lean2708.mern.data.model.UpdateReviewRequest
import okhttp3.MultipartBody


class OrderRepository(private val apiService: ApiService) {
    suspend fun getOrdersByStatus(status: String) = apiService.getOrdersByStatus(status)
    suspend fun getOrderDetail(orderId: String) = apiService.getOrderDetail(orderId)
    suspend fun cancelOrder(orderId: String) = apiService.cancelOrder(orderId)

    suspend fun createCashOrder(request: CreateOrderRequest) = apiService.createCashOrder(request)

    suspend fun createVnpayOrder(request: CreateOrderRequest) = apiService.createVnpayOrder(request)
    suspend fun checkVnpayCallback(params: Map<String, String>) = apiService.handleVnpayReturn(params)

    // --- Review Functions ---
    suspend fun checkReview(productId: String) =
        apiService.checkReview(CheckReviewRequest(productId))

    suspend fun addReview(request: CreateReviewRequest) = apiService.addReview(request)

    suspend fun getReviewDetail(reviewId: String) = apiService.getReviewDetail(reviewId)

    suspend fun updateReview(reviewId: String, request: UpdateReviewRequest) = apiService.updateReview(reviewId, request)
}