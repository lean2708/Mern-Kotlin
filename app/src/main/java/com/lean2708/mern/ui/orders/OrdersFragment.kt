package com.lean2708.mern.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.lean2708.mern.R
import com.lean2708.mern.data.model.Order
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentOrdersBinding
import com.lean2708.mern.repository.OrderRepository
import com.lean2708.mern.ui.orders.OrderAdapter // Đã sửa và tách biệt
import com.lean2708.mern.ui.viewmodel.OrderViewModel
import com.lean2708.mern.ui.viewmodel.OrderStatus
import com.lean2708.mern.ui.viewmodel.OrderViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource


class OrdersFragment : Fragment() {
    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderAdapter: OrderAdapter

    private val viewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(OrderRepository(RetrofitInstance.api))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabLayout()
        setupRecyclerView()
        setupObservers()
    }

    private fun setupTabLayout() {
        // Thêm các tab trạng thái
        OrderStatus.values().forEach { status ->
            binding.tabLayoutStatus.addTab(binding.tabLayoutStatus.newTab().setText(status.vietnamese).setTag(status.value))
        }

        binding.tabLayoutStatus.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                // Chuyển trạng thái khi tab được chọn
                val selectedStatusValue = tab.tag as? String ?: OrderStatus.PENDING.value
                val selectedStatus = OrderStatus.values().firstOrNull { it.value == selectedStatusValue } ?: OrderStatus.PENDING
                viewModel.setSelectedStatus(selectedStatus)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Chọn mặc định tab đầu tiên (PENDING)
        binding.tabLayoutStatus.getTabAt(0)?.select()
    }

    private fun setupRecyclerView() {
        // Khởi tạo Adapter với Listener điều hướng
        orderAdapter = OrderAdapter(onDetailClick = { orderId ->
            navigateToOrderDetail(orderId)
        })
        binding.rvOrders.adapter = orderAdapter
    }


    private fun setupObservers() {
        // Lắng nghe trạng thái hiện tại (chọn tab)
        viewModel.selectedStatus.observe(viewLifecycleOwner) { status ->
            // Khi trạng thái thay đổi, ViewModel sẽ tự động gọi fetchOrdersByStatus
        }

        // Lắng nghe danh sách đơn hàng
        viewModel.orders.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBarOrders.visibility = View.VISIBLE
                is Resource.Success<*> -> {
                    binding.progressBarOrders.visibility = View.GONE
                    @Suppress("UNCHECKED_CAST")
                    val orders = resource.data as? List<Order> ?: emptyList()
                    orderAdapter.submitList(orders)
                }
                is Resource.Error<*> -> {
                    binding.progressBarOrders.visibility = View.GONE
                    Toast.makeText(requireContext(), "Lỗi: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                else -> Unit
            }
        }
    }

    private fun navigateToOrderDetail(orderId: String) {
        val detailFragment = OrderDetailFragment.newInstance(orderId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null) // Cho phép nhấn Back để quay lại danh sách
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}