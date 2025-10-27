package com.lean2708.mern.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // Thay 10.0.2.2 nếu server của bạn ở IP khác
    // 10.0.2.2 là địa chỉ IP của máy host (localhost) khi gọi từ Android Emulator
    private const val BASE_URL = "http://10.0.2.2:8080/api/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}