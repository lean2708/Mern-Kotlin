package com.lean2708.mern.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.lean2708.mern.R
import com.lean2708.mern.data.model.Address
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentChangeAddressBinding
import com.lean2708.mern.repository.OrderRepository
import com.lean2708.mern.repository.ProfileRepository
import com.lean2708.mern.ui.orders.adapter.CheckoutAddressAdapter
import com.lean2708.mern.ui.viewmodel.OrderViewModel
import com.lean2708.mern.ui.viewmodel.OrderViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource

class ChangeAddressFragment : Fragment() {
    private var _binding: FragmentChangeAddressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(OrderRepository(RetrofitInstance.api))
    }
    private val profileRepository: ProfileRepository by lazy { ProfileRepository(RetrofitInstance.api) }
    private lateinit var addressAdapter: CheckoutAddressAdapter

    companion object {
        const val REQUEST_ADDRESS_KEY = "request_selected_address"
        const val BUNDLE_ADDRESS_KEY = "bundle_selected_address"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChangeAddressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        setupRecyclerView()
        setupObservers()
        viewModel.fetchDefaultAddress(profileRepository) // Tải tất cả địa chỉ
    }

    private fun setupRecyclerView() {
        addressAdapter = CheckoutAddressAdapter(onAddressSelected = { address ->
            // Khi người dùng chọn địa chỉ, gửi kết quả và quay lại CheckoutFragment
            sendResultAndPop(address)
        })
        binding.rvAddresses.adapter = addressAdapter

        // Thiết lập LayoutManager theo chiều dọc (để hiển thị danh sách)
        binding.rvAddresses.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
    }

    private fun setupObservers() {
        // Lắng nghe danh sách địa chỉ
        viewModel.defaultAddress.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBarForm.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBarForm.visibility = View.GONE
                    @Suppress("UNCHECKED_CAST")
                    val allAddresses = resource.data as? List<Address> ?: emptyList()
                    addressAdapter.submitList(allAddresses)

                    if (allAddresses.isEmpty()) {
                        Toast.makeText(requireContext(), "Không tìm thấy địa chỉ nào.", Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Error -> {
                    binding.progressBarForm.visibility = View.GONE
                    Toast.makeText(requireContext(), "Lỗi tải địa chỉ: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }
    }

    /**
     * Gửi địa chỉ đã chọn về CheckoutFragment và đóng Fragment hiện tại.
     */
    private fun sendResultAndPop(address: Address) {
        // Gửi kết quả về Fragment Cha (CheckoutFragment)
        parentFragmentManager.setFragmentResult(
            REQUEST_ADDRESS_KEY,
            Bundle().apply {
                putParcelable(BUNDLE_ADDRESS_KEY, address) // Address phải là Parcelable
            }
        )
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}