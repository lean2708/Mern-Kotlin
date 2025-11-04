package com.lean2708.mern.data.network

import com.lean2708.mern.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ApiService {

    // --- API AUTH ---
    @POST("signin")
    suspend fun signin(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @POST("signup")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<GenericResponse>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body emailRequest: EmailRequest): Response<GenericResponse>

    @POST("forgot-password/verify-otp")
    suspend fun verifyOtp(@Body verifyOtpRequest: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("forgot-password/reset-password")
    suspend fun resetPassword(@Body resetPasswordRequest: ResetPasswordRequest): Response<GenericResponse>

    // --- API HOME & SẢN PHẨM ---
    @GET("get-categoryProduct")
    suspend fun getCategoryProducts(): Response<ProductListResponse>

    @POST("category-product")
    suspend fun getProductsForCategory(@Body categoryRequest: CategoryRequest): Response<ProductListResponse>

    @POST("product-details")
    suspend fun getProductDetails(@Body request: ProductDetailRequest): Response<DataResponse<Product>>

    @GET("search")
    suspend fun searchProducts(@Query("q") query: String): Response<ProductListResponse>

    @GET("review/product/{productId}")
    suspend fun getProductReviews(@Path("productId") productId: String): Response<ReviewListResponse>

    // --- API PROFILE & ADDRESS ---
    @GET("user-details")
    suspend fun getUserDetails(): Response<DataResponse<User>>

    @POST("update-user")
    suspend fun updateUserDetails(@Body request: UpdateUserRequest): Response<DataResponse<User>>

    @Multipart
    @POST("upload-avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<AvatarResponse>

    @POST("change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<GenericResponse>

    @POST("address")
    suspend fun createAddress(@Body request: AddressRequest): Response<DataResponse<Address>>

    @GET("address")
    suspend fun getAddresses(): Response<ListDataResponse<Address>>

    @PUT("address/{addressId}")
    suspend fun updateAddress(
        @Path("addressId") addressId: String,
        @Body request: AddressRequest
    ): Response<DataResponse<Address>>

    @DELETE("address/{addressId}")
    suspend fun deleteAddress(@Path("addressId") addressId: String): Response<GenericResponse>

    // --- API GIỎ HÀNG (CART) ---
    @POST("addtoCart")
    suspend fun addToCart(@Body request: AddToCartRequest): Response<AddToCartResponse>

    @GET("countAddToCartProduct")
    suspend fun getCartCount(): Response<CartCountResponse>

    @GET("view-cart-product")
    suspend fun viewCartProducts(): Response<ViewCartResponse>

    @POST("update-cart-product")
    suspend fun updateCartProduct(@Body request: UpdateCartRequest): Response<GenericResponse>

    @POST("delete-cart-product")
    suspend fun deleteCartProduct(@Body request: DeleteCartRequest): Response<GenericResponse>

    // --- API ĐƠN HÀNG (ORDER) ---
    @GET("order/by-status")
    suspend fun getOrdersByStatus(@Query("status") status: String): Response<OrderListResponse>

    @GET("order/{orderId}")
    suspend fun getOrderDetail(@Path("orderId") orderId: String): Response<OrderDetailResponse>

    @PUT("order/{orderId}/cancel")
    suspend fun cancelOrder(@Path("orderId") orderId: String): Response<OrderDetailResponse>

    // --- BỔ SUNG CÁC HÀM TẠO ĐƠN HÀNG VÀ CALLBACK (API 1, 2, 4) ---

    // API 1: Tạo đơn hàng CASH (Trả về SimpleOrder)
    @POST("order")
    suspend fun createCashOrder(@Body request: CreateOrderRequest): Response<DataResponse<SimpleOrder>>

    // API 2: Tạo đơn hàng VNPAY (Response VnpayOrderResponse chứa SimpleOrder)
    @POST("order")
    suspend fun createVnpayOrder(@Body request: CreateOrderRequest): Response<VnpayOrderResponse>

    // API 4: Xử lý VNPAY Callback
    @GET("order/vnpay-return")
    suspend fun handleVnpayReturn(@QueryMap params: Map<String, String>): Response<GenericResponse>
}