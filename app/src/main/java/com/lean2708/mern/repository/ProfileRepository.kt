package com.lean2708.mern.repository

import com.lean2708.mern.data.model.AddressRequest
import com.lean2708.mern.data.model.ChangePasswordRequest
import com.lean2708.mern.data.model.UpdateUserRequest
import com.lean2708.mern.data.network.ApiService
import okhttp3.MultipartBody

class ProfileRepository(private val apiService: ApiService) {

    // User
    suspend fun getUserDetails() = apiService.getUserDetails()
    // Sửa lỗi: Cung cấp hàm updateUserDetails
    suspend fun updateUserDetails(request: UpdateUserRequest) = apiService.updateUserDetails(request)
    // Sửa lỗi: Cung cấp hàm uploadAvatar
    suspend fun uploadAvatar(file: MultipartBody.Part) = apiService.uploadAvatar(file)

    // Password
    suspend fun changePassword(request: ChangePasswordRequest) = apiService.changePassword(request)

    // Address
    suspend fun getAddresses() = apiService.getAddresses()
    suspend fun createAddress(request: AddressRequest) = apiService.createAddress(request)
    suspend fun updateAddress(id: String, request: AddressRequest) = apiService.updateAddress(id, request)
    suspend fun deleteAddress(id: String) = apiService.deleteAddress(id)
}