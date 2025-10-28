package com.lean2708.mern.data.network

import android.content.Context
import com.lean2708.mern.data.local.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "http://10.0.2.2:8080/api/"

    // Biến private để giữ sessionManager
    private var sessionManager: SessionManager? = null

    // Hàm khởi tạo SessionManager (Sẽ được gọi từ MainActivity)
    fun initialize(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context.applicationContext)
        }
    }

    // Tạo OkHttpClient với Interceptor
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                // Lấy token từ SessionManager
                val token = sessionManager?.fetchAuthToken()

                val originalRequest = chain.request()

                // Nếu có token, thêm header "Authorization"
                val newRequest = if (token != null) {
                    originalRequest.newBuilder()
                        .header("Authorization", "$token") // Thêm "Bearer " nếu API yêu cầu
                        .build()
                } else {
                    originalRequest
                }
                chain.proceed(newRequest)
            }
            .build()
    }

    val api: ApiService by lazy {
        if (sessionManager == null) {
            throw IllegalStateException("RetrofitInstance must be initialized in Application or MainActivity")
        }

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // <-- Sử dụng OkHttpClient đã tùy chỉnh
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}