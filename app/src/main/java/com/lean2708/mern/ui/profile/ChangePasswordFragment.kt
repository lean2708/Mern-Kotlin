package com.lean2708.mern.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lean2708.mern.data.model.ChangePasswordRequest
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentChangePasswordBinding
import com.lean2708.mern.repository.ProfileRepository
import com.lean2708.mern.ui.viewmodel.ProfileViewModel
import com.lean2708.mern.ui.viewmodel.ProfileViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource

class ChangePasswordFragment : Fragment() {
    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ProfileRepository(RetrofitInstance.api))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Nút quay lại (Back)
        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        // Lắng nghe kết quả ĐỔI MẬT KHẨU
        viewModel.changePasswordResult.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading<*> -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), resource.message ?: "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show()
                    // Quay lại màn hình Profile sau khi đổi thành công
                    parentFragmentManager.popBackStack()
                }
                is Resource.Error<*> -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Lỗi: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }
    }

    private fun setupListeners() {
        binding.btnChangePassword.setOnClickListener {
            val currentPass = binding.etCurrentPassword.text.toString().trim()
            val newPass = binding.etNewPassword.text.toString().trim()
            val confirmPass = binding.etConfirmPassword.text.toString().trim()

            if (newPass != confirmPass) {
                Toast.makeText(requireContext(), "Mật khẩu mới không khớp!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentPass.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ các trường", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = ChangePasswordRequest(currentPass, newPass, confirmPass)
            // GỌI HÀM ĐỔI MẬT KHẨU
            viewModel.changePassword(request)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBarChangePass.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnChangePassword.isEnabled = !isLoading
        binding.etCurrentPassword.isEnabled = !isLoading
        binding.etNewPassword.isEnabled = !isLoading
        binding.etConfirmPassword.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}