package com.lean2708.mern.data.model

import com.google.gson.annotations.SerializedName

// Dùng chung cho nhiều request
data class EmailRequest(
    val email: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: Int // API của bạn nhận Int
)

data class ResetPasswordRequest(
    val email: String,
    val resetToken: String,
    val newPassword: String,
    val confirmPassword: String
)