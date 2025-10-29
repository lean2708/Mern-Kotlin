package com.lean2708.mern.data.network

import android.content.Context
import com.lean2708.mern.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor // CẦN IMPORT NÀY
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit // CẦN IMPORT NÀY

object RetrofitInstance {

    private const val BASE_URL = "http://10.0.2.2:8080/api/"

    private var sessionManager: SessionManager? = null

    fun initialize(context: Context) {
        if (sessionManager == null) {
            sessionManager = SessionManager(context.applicationContext)
        }
    }

    // Interceptor để ghi log (Rất quan trọng để debug)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Xem request body và response
    }

    // Tạo OkHttpClient với các tinh chỉnh mạng
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()

            // 1. THIẾT LẬP TIMEOUT: Cho phép kết nối lâu hơn (30 giây)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)

            // 2. XỬ LÝ CHUYỂN HƯỚNG: Đảm bảo OkHttp theo dõi các HTTPS redirects (rất quan trọng với CDN)
            .followRedirects(true)
            .followSslRedirects(true)

            // 3. LOGGING (ĐỂ THEO DÕI GIAO THỨC)
            .addInterceptor(loggingInterceptor)

            // 4. INTERCEPTOR TOKEN
            .addInterceptor { chain ->
                val token = sessionManager?.fetchAuthToken()
                val originalRequest = chain.request()

                val newRequest = if (token != null) {
                    originalRequest.newBuilder()
                        // Giữ lại token thô (không 'Bearer') nếu backend yêu cầu
                        .header("Authorization", "$token")
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
            .client(okHttpClient) // <-- Sử dụng cấu hình OkHttpClient MỚI
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}