package com.lean2708.mern.ui.profile


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.lean2708.mern.R
import com.lean2708.mern.data.model.Address
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentAddressListBinding
import com.lean2708.mern.databinding.ItemAddressBinding
import com.lean2708.mern.repository.ProfileRepository
import com.lean2708.mern.ui.viewmodel.ProfileViewModel
import com.lean2708.mern.ui.viewmodel.ProfileViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource

class AddressListFragment : Fragment() {
    private var _binding: FragmentAddressListBinding? = null
    private val binding get() = _binding!!

    // (Giả định ProfileViewModelFactory đã được tạo)
    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(ProfileRepository(RetrofitInstance.api))
    }

    private lateinit var addressAdapter: AddressAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddressListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupRecyclerView()
        setupListeners()
        setupObservers()

        // Bắt đầu tải danh sách địa chỉ
        viewModel.fetchAddresses()
    }

    private fun setupRecyclerView() {
        addressAdapter = AddressAdapter(
            onUpdate = { address ->
                navigateToAddressForm(address) // Mở Fragment Sửa
            },
            onDelete = { address ->
                confirmAndDeleteAddress(address) // Xác nhận trước khi xóa
            }
        )
        binding.rvAddresses.adapter = addressAdapter
    }

    private fun setupListeners() {
        binding.btnAddAddress.setOnClickListener {
            navigateToAddressForm(null) // Mở Fragment Thêm mới
        }
    }

    private fun setupObservers() {
        // 1. Lắng nghe danh sách địa chỉ (READ)
        viewModel.addresses.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading -> binding.progressBarAddress.visibility = View.VISIBLE
                is Resource.Success<*> -> {
                    binding.progressBarAddress.visibility = View.GONE
                    // Ép kiểu an toàn (đảm bảo resource.data là List<Address>)
                    @Suppress("UNCHECKED_CAST")
                    addressAdapter.submitList(resource.data as? List<Address> ?: emptyList())
                }
                is Resource.Error<*> -> {
                    binding.progressBarAddress.visibility = View.GONE
                    Toast.makeText(requireContext(), "Lỗi tải địa chỉ: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // 2. Lắng nghe kết quả XÓA (DELETE)
        viewModel.deleteAddressResult.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is Resource.Loading<*> -> binding.progressBarAddress.visibility = View.VISIBLE
                is Resource.Success<*> -> {
                    binding.progressBarAddress.visibility = View.GONE
                    Toast.makeText(requireContext(), "Địa chỉ đã được xóa thành công!", Toast.LENGTH_LONG).show()
                    viewModel.fetchAddresses() // Tải lại danh sách sau khi xóa
                }
                is Resource.Error<*> -> {
                    binding.progressBarAddress.visibility = View.GONE
                    Toast.makeText(requireContext(), "Lỗi xóa: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // --- HÀM XỬ LÝ ĐIỀU HƯỚNG ---
    private fun navigateToAddressForm(address: Address?) {
        val formFragment = AddressFormFragment.newInstance(address)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, formFragment)
            .addToBackStack(null)
            .commit()
    }

    // --- HÀM XỬ LÝ XÓA ĐỊA CHỈ (DELETE LOGIC) ---
    private fun confirmAndDeleteAddress(address: Address) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa địa chỉ này không?")
            .setPositiveButton("Xóa") { dialog, _ ->
                viewModel.deleteAddress(address._id) // Gọi hàm xóa trong ViewModel
                dialog.dismiss()
            }
            .setNegativeButton("Hủy") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- ADAPTER VÀ VIEW HOLDER ---
    class AddressAdapter(
        private val onUpdate: (Address) -> Unit,
        private val onDelete: (Address) -> Unit
    ) : androidx.recyclerview.widget.ListAdapter<Address, AddressAdapter.AddressViewHolder>(DiffCallback()) {

        inner class AddressViewHolder(private val binding: ItemAddressBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(address: Address) {
                binding.tvPhone.text = address.phone
                binding.tvAddressDetail.text = address.addressDetail

                // Hiển thị 'Mặc định' (Background màu cam)
                binding.tvIsDefault.visibility = if (address.isDefault) View.VISIBLE else View.GONE

                // Nút thao tác
                binding.btnUpdateAddress.setOnClickListener { onUpdate(address) }
                binding.btnDeleteAddress.setOnClickListener { onDelete(address) }
            }
        }

        private class DiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Address>() {
            override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean = oldItem._id == newItem._id
            override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean = oldItem == newItem
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
            val binding = ItemAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return AddressViewHolder(binding)
        }

        override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }
}