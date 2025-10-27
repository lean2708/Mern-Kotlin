package com.lean2708.mern.data.network

import com.lean2708.mern.data.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("signin")
    suspend fun signin(
        @Body loginRequest: LoginRequest
    ): Response<LoginResponse>

    @POST("signup")
    suspend fun signup(
        @Body signupRequest: SignupRequest
    ): Response<GenericResponse> // Dùng GenericResponse cho Signup

    @POST("forgot-password")
    suspend fun forgotPassword(
        @Body emailRequest: EmailRequest
    ): Response<GenericResponse>

    @POST("forgot-password/verify-otp")
    suspend fun verifyOtp(
        @Body verifyOtpRequest: VerifyOtpRequest
    ): Response<VerifyOtpResponse> // Response này trả về resetToken

    @POST("forgot-password/reset-password")
    suspend fun resetPassword(
        @Body resetPasswordRequest: ResetPasswordRequest
    ): Response<GenericResponse>


    @GET("get-categoryProduct")
    suspend fun getCategoryProducts(): Response<ProductListResponse>


    @POST("category-product")
    suspend fun getProductsForCategory(
        @Body categoryRequest: CategoryRequest
    ): Response<ProductListResponse>



}