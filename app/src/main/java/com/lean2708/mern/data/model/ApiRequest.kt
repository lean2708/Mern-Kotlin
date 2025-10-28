package com.lean2708.mern.data.model

// Dùng cho POST /api/update-user
data class UpdateUserRequest(
    val userId: String,
    val name: String,
    val gender: String,
    val dateOfBirth: String // "2004-08-27"
)

// Dùng cho POST /api/change-password
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

// Dùng cho POST /api/address và PUT /api/address/{id}
data class AddressRequest(
    val phone: String,
    val addressDetail: String,
    val isDefault: Boolean
)