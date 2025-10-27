package com.lean2708.mern.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.ActivityForgotPasswordBinding
import com.lean2708.mern.repository.AuthRepository
import com.lean2708.mern.ui.viewmodel.AuthViewModel
import com.lean2708.mern.ui.viewmodel.AuthViewModelFactory
import com.lean2708.mern.ui.viewmodel.ForgotPasswordStep

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(RetrofitInstance.api))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        // Bước 1
        binding.btnSendOtp.setOnClickListener {
            val email = binding.etEmailForgot.text.toString().trim()
            if (email.isNotEmpty()) {
                setLoading(true)
                viewModel.sendOtp(email)
            } else {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
            }
        }

        // Bước 2
        binding.btnVerifyOtp.setOnClickListener {
            val otpStr = binding.etOtp.text.toString().trim()
            if (otpStr.isNotEmpty()) {
                setLoading(true)
                viewModel.verifyOtp(otpStr.toInt())
            } else {
                Toast.makeText(this, "Vui lòng nhập OTP", Toast.LENGTH_SHORT).show()
            }
        }

        // Bước 3
        binding.btnResetPassword.setOnClickListener {
            val newPass = binding.etNewPassword.text.toString().trim()
            val confirmPass = binding.etConfirmPassword.text.toString().trim()

            if (newPass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (newPass == confirmPass) {
                    setLoading(true)
                    viewModel.resetPassword(newPass, confirmPass)
                } else {
                    Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        // Lắng nghe thay đổi bước
        viewModel.forgotPasswordStep.observe(this) { step ->
            updateUiForStep(step)
        }

        // Lắng nghe kết quả từng bước
        viewModel.sendOtpResult.observe(this) { result ->
            setLoading(false)
            result.onSuccess {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
            result.onFailure {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.verifyOtpResult.observe(this) { result ->
            setLoading(false)
            result.onSuccess {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
            result.onFailure {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.resetPasswordResult.observe(this) { result ->
            setLoading(false)
            result.onSuccess {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                // Xong, đóng Activity
                finish()
            }
            result.onFailure {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUiForStep(step: ForgotPasswordStep) {
        binding.layoutStep1.visibility = if (step == ForgotPasswordStep.SEND_EMAIL) View.VISIBLE else View.GONE
        binding.layoutStep2.visibility = if (step == ForgotPasswordStep.VERIFY_OTP) View.VISIBLE else View.GONE
        binding.layoutStep3.visibility = if (step == ForgotPasswordStep.RESET_PASSWORD) View.VISIBLE else View.GONE
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarForgot.visibility = if (isLoading) View.VISIBLE else View.GONE
        // Vô hiệu hóa tất cả các nút khi tải
        binding.btnSendOtp.isEnabled = !isLoading
        binding.btnVerifyOtp.isEnabled = !isLoading
        binding.btnResetPassword.isEnabled = !isLoading
    }
}