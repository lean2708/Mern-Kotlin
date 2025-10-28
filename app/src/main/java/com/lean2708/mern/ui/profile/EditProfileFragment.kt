package com.lean2708.mern.ui.profile

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.lean2708.mern.R
import com.lean2708.mern.data.model.UpdateUserRequest
import com.lean2708.mern.data.model.User
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentEditProfileBinding
import com.lean2708.mern.repository.ProfileRepository
import com.lean2708.mern.ui.viewmodel.ProfileViewModel
import com.lean2708.mern.ui.viewmodel.ProfileViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Locale

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    // Lấy ViewModel qua Factory (Đã sửa lỗi Unresolved reference)
    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ProfileRepository(RetrofitInstance.api))
    }

    // Khởi chạy Gallery để chọn ảnh
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                binding.imgProfileAvatar.setImageURI(it)
                uploadAvatar(it) // Gọi hàm upload
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        viewModel.fetchUserDetails()
        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        // Lắng nghe dữ liệu người dùng (userDetails)
        viewModel.userDetails.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    (resource.data as? User)?.let { user ->
                        binding.etName.setText(user.name)
                        binding.tvEmail.text = user.email
                        // Đã sửa lỗi 'it'
                        user.dateOfBirth?.let { isoDate -> binding.etDateOfBirth.setText(if (isoDate.length >= 10) isoDate.substring(0, 10) else isoDate) }
                        when(user.gender) {
                            "Nam" -> binding.rgGender.check(binding.rbMale.id)
                            "Nữ" -> binding.rgGender.check(binding.rbFemale.id)
                            "Khác" -> binding.rgGender.check(binding.rbOther.id)
                        }
                        Glide.with(this).load(user.profilePic).circleCrop().into(binding.imgProfileAvatar)
                    }
                }
                is Resource.Error<*> -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Lỗi tải thông tin: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }

        // Lắng nghe kết quả CẬP NHẬT INFO (updateUserResult)
        viewModel.updateUserResult.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading<*> -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error<*> -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Lỗi cập nhật: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }

        // Lắng nghe kết quả UPLOAD AVATAR (uploadAvatarResult)
        viewModel.uploadAvatarResult.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading<*> -> setLoading(true)
                is Resource.Success<*> -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Đổi avatar thành công!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error<*> -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Lỗi đổi avatar: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }
    }

    private fun setupListeners() {
        binding.etDateOfBirth.setOnClickListener {
            showDatePickerDialog()
        }

        binding.btnSaveProfile.setOnClickListener {
            updateUserInfo()
        }

        binding.tvChangeAvatar.setOnClickListener {
            openImagePicker()
        }
    }

    // --- Logic Cập nhật Info (API 2) ---
    private fun updateUserInfo() {
        val userId = viewModel.userDetails.value?.data?._id
        if (userId == null) {
            Toast.makeText(requireContext(), "Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show()
            return
        }

        val name = binding.etName.text.toString().trim()
        val dob = binding.etDateOfBirth.text.toString().trim()
        val genderId = binding.rgGender.checkedRadioButtonId
        val gender = when(genderId) {
            binding.rbMale.id -> "Nam"
            binding.rbFemale.id -> "Nữ"
            else -> "Khác"
        }

        if (name.isEmpty() || dob.isEmpty()) {
            Toast.makeText(requireContext(), "Không được để trống tên hoặc ngày sinh", Toast.LENGTH_SHORT).show()
            return
        }

        val request = UpdateUserRequest(userId, name, gender, dob)
        viewModel.updateUserDetails(request)
    }

    // --- Logic Date Picker ---
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val monthFormatted = String.format("%02d", selectedMonth + 1)
            val dayFormatted = String.format("%02d", selectedDay)
            val selectedDate = "$selectedYear-$monthFormatted-$dayFormatted"
            binding.etDateOfBirth.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    // --- Logic Upload Avatar (API 1) ---
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun uploadAvatar(uri: Uri) {
        setLoading(true)
        val filePart = createMultipartFile(uri)
        if (filePart != null) {
            viewModel.uploadAvatar(filePart)
        } else {
            Toast.makeText(requireContext(), "Không thể tạo file ảnh", Toast.LENGTH_SHORT).show()
            setLoading(false)
        }
    }

    // Chuyển URI thành MultipartBody.Part (dùng cho form-data)
    private fun createMultipartFile(uri: Uri): MultipartBody.Part? {
        val context = requireContext()
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        val fileName = "avatar_upload_${System.currentTimeMillis()}.jpg"
        val tempFile = File(context.cacheDir, fileName)

        FileOutputStream(tempFile).use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()

        val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())

        // Tên trường trong form-data phải là 'files' theo API bạn cung cấp
        return MultipartBody.Part.createFormData("files", tempFile.name, requestFile)
    }


    private fun setLoading(isLoading: Boolean) {
        binding.progressBarEditProfile.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSaveProfile.isEnabled = !isLoading
        binding.tvChangeAvatar.isEnabled = !isLoading

        // Vô hiệu hóa input khi đang tải
        binding.etName.isEnabled = !isLoading
        binding.etDateOfBirth.isEnabled = !isLoading
        // Kiểm tra an toàn trước khi lặp qua RadioGroup
        if (binding.rgGender.childCount > 0) {
            for (i in 0 until binding.rgGender.childCount) {
                binding.rgGender.getChildAt(i).isEnabled = !isLoading
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}