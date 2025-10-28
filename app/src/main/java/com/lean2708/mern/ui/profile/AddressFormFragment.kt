package com.lean2708.mern.ui.profile


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lean2708.mern.R
import com.lean2708.mern.data.model.Address
import com.lean2708.mern.data.model.AddressRequest
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentAddressFormBinding
import com.lean2708.mern.repository.ProfileRepository
import com.lean2708.mern.ui.viewmodel.ProfileViewModel
import com.lean2708.mern.ui.viewmodel.ProfileViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource

class AddressFormFragment : Fragment() {
    private var _binding: FragmentAddressFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ProfileRepository(RetrofitInstance.api))
    }

    // Biến để xác định chế độ (Thêm mới: null, Cập nhật: Address object)
    private var addressToEdit: Address? = null

    companion object {
        const val ARG_ADDRESS = "address_to_edit"
        fun newInstance(address: Address?): AddressFormFragment {
            val fragment = AddressFormFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(ARG_ADDRESS, address) // Address phải là Parcelable
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lấy dữ liệu địa chỉ nếu đang ở chế độ Cập nhật
        addressToEdit = arguments?.getParcelable(ARG_ADDRESS)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddressFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        loadDataForEditMode()
        setupObservers()

        binding.btnSaveAddress.setOnClickListener {
            saveAddress()
        }
    }

    private fun setupToolbar() {
        // Thêm Toolbar programmatically (vì form form này được dùng cho cả 2 mode)
        val toolbar = com.google.android.material.appbar.MaterialToolbar(requireContext()).apply {
            id = R.id.toolbar // Dùng ID có sẵn
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(context.getColor(R.color.white))
            elevation = 4f
            setTitleTextColor(context.getColor(R.color.colorTextPrimary))
            setNavigationIcon(R.drawable.ic_arrow_back) // Icon Back
            setOnClickListener { parentFragmentManager.popBackStack() }
        }
        // Thêm toolbar vào layout ngoài cùng (giả định root layout là ViewGroup)
        (binding.root as? ViewGroup)?.addView(toolbar, 0)

        // Cập nhật tiêu đề dựa trên mode
        toolbar.title = if (addressToEdit == null) "Thêm địa chỉ mới" else "Chỉnh sửa địa chỉ"

        // Thao tác Back
        toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
    }

    private fun loadDataForEditMode() {
        if (addressToEdit != null) {
            binding.tvTitle.text = "Chỉnh sửa địa chỉ"
            binding.btnSaveAddress.text = "LƯU THAY ĐỔI"

            addressToEdit?.let { address ->
                binding.etPhone.setText(address.phone)
                binding.etAddressDetail.setText(address.addressDetail)
                binding.cbIsDefault.isChecked = address.isDefault
            }
        }
    }

    private fun saveAddress() {
        val phone = binding.etPhone.text.toString().trim()
        val detail = binding.etAddressDetail.text.toString().trim()
        val isDefault = binding.cbIsDefault.isChecked

        if (phone.isEmpty() || detail.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng điền đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }

        val request = AddressRequest(phone, detail, isDefault)

        if (addressToEdit == null) {
            // MODE TẠO MỚI (POST)
            viewModel.createAddress(request)
        } else {
            // MODE CẬP NHẬT (PUT)
            viewModel.updateAddress(addressToEdit!!._id, request)
        }
    }

    private fun setupObservers() {
        // Observer chung cho Create và Update
        // Dùng viewLifecycleOwner để Observe
        viewModel.createAddressResult.observe(viewLifecycleOwner) { handleAddressResult(it, "Địa chỉ đã được tạo thành công!") }
        viewModel.updateAddressResult.observe(viewLifecycleOwner) { handleAddressResult(it, "Địa chỉ đã được cập nhật thành công!") }
        // TODO: Thêm observer cho Delete API
    }

    private fun handleAddressResult(resource: Resource<*>, successMessage: String) {
        when(resource) {
            is Resource.Loading<*> -> binding.progressBarForm.visibility = View.VISIBLE
            is Resource.Success<*> -> {
                binding.progressBarForm.visibility = View.GONE
                Toast.makeText(requireContext(), successMessage, Toast.LENGTH_LONG).show()
                // ... (logic quay lại) ...
                parentFragmentManager.popBackStack()
            }
            is Resource.Error<*> -> {
                binding.progressBarForm.visibility = View.GONE
                // Sửa lỗi Unresolved reference 'it' thành 'resource'
                Toast.makeText(requireContext(), "Lỗi: ${resource.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}