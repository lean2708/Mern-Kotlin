package com.lean2708.mern.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.lean2708.mern.R
import com.lean2708.mern.data.local.SessionManager
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentProfileBinding
import com.lean2708.mern.repository.ProfileRepository
import com.lean2708.mern.ui.auth.LoginActivity

// === SỬA LỖI 1: THÊM CÁC IMPORT THIẾU ===
import com.lean2708.mern.ui.home.activity.MainActivity
// =========================================

import com.lean2708.mern.ui.viewmodel.ProfileViewModel
import com.lean2708.mern.ui.viewmodel.ProfileViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val sessionManager by lazy { SessionManager(requireContext()) }

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ProfileRepository(RetrofitInstance.api))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchUserDetails()
        setupObservers()

        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.userDetails.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBarProfile.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBarProfile.visibility = View.GONE
                    resource.data?.let { user ->
                        binding.tvUserName.text = user.name
                        binding.tvUserEmail.text = user.email
                        Glide.with(this)
                            .load(user.profilePic)
                            .circleCrop()
                            .placeholder(R.drawable.ic_profile)
                            .into(binding.imgAvatar)
                    }
                }
                is Resource.Error -> {
                    binding.progressBarProfile.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnLogout.setOnClickListener {
            sessionManager.clearAuthToken()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.btnEditProfile.setOnClickListener {
            navigateTo(EditProfileFragment(), "Hồ sơ cá nhân")
        }

        binding.btnChangePassword.setOnClickListener {
            navigateTo(ChangePasswordFragment(), "Đổi mật khẩu")
        }

        binding.btnAddress.setOnClickListener {
            navigateTo(AddressListFragment(), "Địa chỉ của tôi")
        }
    }

    // === SỬA LỖI 2: CẬP NHẬT HÀM navigateTo ĐỂ CHẤP NHẬN THAM SỐ title ===
    private fun navigateTo(fragment: Fragment, title: String) {
        // Gọi hàm helper trong MainActivity để cập nhật tiêu đề (nếu cần)
        (activity as? MainActivity)?.updateToolbarTitle(title)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}