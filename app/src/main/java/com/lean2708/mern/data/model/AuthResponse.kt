package com.lean2708.mern.data.model

// Dùng cho các response chung (Signup, Forgot, Reset)
data class GenericResponse(
    val message: String,
    val success: Boolean,
    val error: Boolean,
    // data có thể là object (Signup) hoặc null, nên dùng Any?
    val data: Any?
)

// Response cho Login
data class LoginResponse(
    val message: String,
    val data: String, // Đây là JWT Token (String)
    val success: Boolean,
    val error: Boolean
)

// Response cho Verify OTP
data class VerifyOtpResponse(
    val message: String,
    val resetToken: String,
    val success: Boolean,
    val error: Boolean
)