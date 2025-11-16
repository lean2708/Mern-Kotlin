package com.lean2708.mern.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // SỬA: Dùng activityViewModels
import com.google.android.material.tabs.TabLayout
import com.lean2708.mern.R
import com.lean2708.mern.data.model.Order
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentOrdersBinding
import com.lean2708.mern.repository.OrderRepository
import com.lean2708.mern.ui.orders.adapter.OrderAdapter
import com.lean2708.mern.ui.viewmodel.OrderViewModel
import com.lean2708.mern.ui.viewmodel.OrderStatus
import com.lean2708.mern.ui.viewmodel.OrderViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource


class OrdersFragment : Fragment() {
    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var orderAdapter: OrderAdapter

    // SỬA LỖI: Sử dụng 'activityViewModels' để chia sẻ ViewModel với Activity
    private val viewModel: OrderViewModel by activityViewModels {
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
                val selectedStatusValue = tab.tag as? String ?: OrderStatus.PENDING.value
                val selectedStatus = OrderStatus.values().firstOrNull { it.value == selectedStatusValue } ?: OrderStatus.PENDING
                viewModel.setSelectedStatus(selectedStatus)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // SỬA: Chọn tab mặc định dựa trên giá trị hiện tại của ViewModel
        val currentStatusIndex = OrderStatus.values().indexOf(viewModel.selectedStatus.value)
        binding.tabLayoutStatus.getTabAt(currentStatusIndex)?.select()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(onDetailClick = { orderId ->
            navigateToOrderDetail(orderId)
        })
        binding.rvOrders.adapter = orderAdapter
        binding.rvOrders.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
    }


    private fun setupObservers() {
        // Lắng nghe danh sách đơn hàng (từ switchMap)
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