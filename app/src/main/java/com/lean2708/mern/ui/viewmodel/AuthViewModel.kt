package com.lean2708.mern.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lean2708.mern.data.model.*
import com.lean2708.mern.repository.AuthRepository
import kotlinx.coroutines.launch

// Trạng thái cho màn hình quên mật khẩu
enum class ForgotPasswordStep {
    SEND_EMAIL,
    VERIFY_OTP,
    RESET_PASSWORD
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // LiveData cho Login
    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult

    // LiveData cho Signup
    private val _signupResult = MutableLiveData<Result<GenericResponse>>()
    val signupResult: LiveData<Result<GenericResponse>> = _signupResult

    // LiveData cho Forgot Password
    private val _forgotPasswordStep = MutableLiveData<ForgotPasswordStep>(ForgotPasswordStep.SEND_EMAIL)
    val forgotPasswordStep: LiveData<ForgotPasswordStep> = _forgotPasswordStep

    private val _sendOtpResult = MutableLiveData<Result<GenericResponse>>()
    val sendOtpResult: LiveData<Result<GenericResponse>> = _sendOtpResult

    private val _verifyOtpResult = MutableLiveData<Result<VerifyOtpResponse>>()
    val verifyOtpResult: LiveData<Result<VerifyOtpResponse>> = _verifyOtpResult

    private val _resetPasswordResult = MutableLiveData<Result<GenericResponse>>()
    val resetPasswordResult: LiveData<Result<GenericResponse>> = _resetPasswordResult

    // Biến tạm để lưu trữ thông tin giữa các bước
    var emailCache: String? = null
    var resetTokenCache: String? = null

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            try {
                val response = repository.login(LoginRequest(email, pass))
                if (response.isSuccessful && response.body() != null) {
                    _loginResult.postValue(Result.success(response.body()!!))
                } else {
                    _loginResult.postValue(Result.failure(Exception(response.message())))
                }
            } catch (e: Exception) {
                _loginResult.postValue(Result.failure(e))
            }
        }
    }

    fun signup(name: String, email: String, pass: String) {
        viewModelScope.launch {
            try {
                val response = repository.signup(SignupRequest(name, email, pass))
                if (response.isSuccessful && response.body() != null) {
                    _signupResult.postValue(Result.success(response.body()!!))
                } else {
                    _signupResult.postValue(Result.failure(Exception(response.message())))
                }
            } catch (e: Exception) {
                _signupResult.postValue(Result.failure(e))
            }
        }
    }

    // --- Logic Quên Mật Khẩu ---

    fun sendOtp(email: String) {
        emailCache = email // Lưu lại email
        viewModelScope.launch {
            try {
                val response = repository.sendOtp(EmailRequest(email))
                if (response.isSuccessful && response.body()?.success == true) {
                    _sendOtpResult.postValue(Result.success(response.body()!!))
                    _forgotPasswordStep.postValue(ForgotPasswordStep.VERIFY_OTP) // Chuyển bước
                } else {
                    _sendOtpResult.postValue(Result.failure(Exception(response.body()?.message ?: "Lỗi gửi OTP")))
                }
            } catch (e: Exception) {
                _sendOtpResult.postValue(Result.failure(e))
            }
        }
    }

    fun verifyOtp(otp: Int) {
        if (emailCache == null) {
            _verifyOtpResult.postValue(Result.failure(Exception("Email không tìm thấy")))
            return
        }
        viewModelScope.launch {
            try {
                val response = repository.verifyOtp(VerifyOtpRequest(emailCache!!, otp))
                if (response.isSuccessful && response.body()?.success == true) {
                    resetTokenCache = response.body()!!.resetToken // Lưu reset token
                    _verifyOtpResult.postValue(Result.success(response.body()!!))
                    _forgotPasswordStep.postValue(ForgotPasswordStep.RESET_PASSWORD) // Chuyển bước
                } else {
                    _verifyOtpResult.postValue(Result.failure(Exception(response.body()?.message ?: "OTP không đúng")))
                }
            } catch (e: Exception) {
                _verifyOtpResult.postValue(Result.failure(e))
            }
        }
    }

    fun resetPassword(newPass: String, confirmPass: String) {
        if (emailCache == null || resetTokenCache == null) {
            _resetPasswordResult.postValue(Result.failure(Exception("Phiên làm việc hết hạn")))
            return
        }

        val request = ResetPasswordRequest(emailCache!!, resetTokenCache!!, newPass, confirmPass)
        viewModelScope.launch {
            try {
                val response = repository.resetPassword(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _resetPasswordResult.postValue(Result.success(response.body()!!))
                } else {
                    _resetPasswordResult.postValue(Result.failure(Exception(response.body()?.message ?: "Lỗi đặt lại mật khẩu")))
                }
            } catch (e: Exception) {
                _resetPasswordResult.postValue(Result.failure(e))
            }
        }
    }
}