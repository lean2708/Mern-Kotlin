package com.lean2708.mern.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.lean2708.mern.data.local.SessionManager
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.ActivityLoginBinding
import com.lean2708.mern.repository.AuthRepository
import com.lean2708.mern.ui.home.activity.MainActivity
import com.lean2708.mern.ui.viewmodel.AuthViewModel
import com.lean2708.mern.ui.viewmodel.AuthViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val sessionManager by lazy { SessionManager(this) }
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(RetrofitInstance.api))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                setLoading(true)
                viewModel.login(email, password)
            } else {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    // Bên trong file ui/auth/LoginActivity.kt

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { result ->
            setLoading(false)
            result.onSuccess { response ->
                Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()

                // TODO: Lưu token (response.data)
                sessionManager.saveAuthToken(response.data)

                val intent = Intent(this, MainActivity::class.java) // Dòng mới
                startActivity(intent)
                finish()
            }
            result.onFailure {
                Toast.makeText(this, "Đăng nhập thất bại: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
    }
}