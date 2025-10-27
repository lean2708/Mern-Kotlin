package com.lean2708.mern.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lean2708.mern.databinding.FragmentProfileBinding
import com.lean2708.mern.ui.auth.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Lấy thông tin user (tên, email, avatar) và cập nhật UI
        // binding.tvUserName.text = ...
        // binding.tvUserEmail.text = ...
        // Glide.with(this).load(user.avatarUrl).into(binding.imgAvatar)

        binding.btnLogout.setOnClickListener {
            // TODO: Xóa token đã lưu

            // Quay về màn hình Login
            val intent = Intent(requireContext(), LoginActivity::class.java)
            // Xóa tất cả các activity cũ khỏi stack
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}