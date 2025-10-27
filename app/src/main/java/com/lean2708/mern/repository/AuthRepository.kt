package com.lean2708.mern.repository

import com.lean2708.mern.data.model.*
import com.lean2708.mern.data.network.ApiService

class AuthRepository(private val apiService: ApiService) {

    suspend fun login(loginRequest: LoginRequest) = apiService.signin(loginRequest)

    suspend fun signup(signupRequest: SignupRequest) = apiService.signup(signupRequest)

    suspend fun sendOtp(emailRequest: EmailRequest) = apiService.forgotPassword(emailRequest)

    suspend fun verifyOtp(verifyOtpRequest: VerifyOtpRequest) = apiService.verifyOtp(verifyOtpRequest)

    suspend fun resetPassword(resetPasswordRequest: ResetPasswordRequest) = apiService.resetPassword(resetPasswordRequest)
}