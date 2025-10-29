package com.lean2708.mern.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lean2708.mern.data.model.Order
import com.lean2708.mern.repository.OrderRepository
import kotlinx.coroutines.launch

// Trạng thái đơn hàng (để mapping tiếng Việt)
enum class OrderStatus(val value: String, val vietnamese: String) {
    PENDING("PENDING", "Chờ xác nhận"),
    PROCESSING("PROCESSING", "Đang xử lý"),
    SHIPPED("SHIPPED", "Đã gửi hàng"),
    DELIVERED("DELIVERED", "Đã giao hàng"),
    CANCELLED("CANCELLED", "Đã hủy");

    companion object {
        fun fromValue(value: String) = values().firstOrNull { it.value == value }?.vietnamese ?: value
    }
}

class OrderViewModel(private val repository: OrderRepository) : ViewModel() {

    // Danh sách đơn hàng theo trạng thái
    private val _orders = MutableLiveData<Resource<List<Order>>>()
    val orders: LiveData<Resource<List<Order>>> = _orders

    // Chi tiết một đơn hàng
    private val _orderDetail = MutableLiveData<Resource<Order>>()
    val orderDetail: LiveData<Resource<Order>> = _orderDetail

    // Kết quả hủy đơn
    private val _cancelOrderResult = MutableLiveData<Resource<Order>>()
    val cancelOrderResult: LiveData<Resource<Order>> = _cancelOrderResult

    // Trạng thái hiện tại được chọn (default: PENDING)
    private val _selectedStatus = MutableLiveData(OrderStatus.PENDING)
    val selectedStatus: LiveData<OrderStatus> = _selectedStatus

    init {
        fetchOrdersByStatus(selectedStatus.value!!.value)
    }

    fun setSelectedStatus(status: OrderStatus) {
        if (_selectedStatus.value != status) {
            _selectedStatus.value = status
            fetchOrdersByStatus(status.value)
        }
    }

    // --- API 1: Lấy danh sách đơn hàng ---
    fun fetchOrdersByStatus(status: String) {
        _orders.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.getOrdersByStatus(status)
                if (response.isSuccessful && response.body()?.success == true) {
                    _orders.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _orders.postValue(Resource.Error(response.message()))
                }
            } catch (e: Exception) {
                _orders.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    // --- API 2: Lấy chi tiết đơn hàng ---
    fun fetchOrderDetail(orderId: String) {
        _orderDetail.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.getOrderDetail(orderId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _orderDetail.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _orderDetail.postValue(Resource.Error(response.message()))
                }
            } catch (e: Exception) {
                _orderDetail.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    // --- API 3: Hủy đơn hàng ---
    fun cancelOrder(orderId: String) {
        // Dùng LiveData chung cho chi tiết đơn hàng để cập nhật trạng thái hủy
        _orderDetail.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.cancelOrder(orderId)
                if (response.isSuccessful && response.body()?.success == true) {
                    // Cập nhật chi tiết đơn hàng thành CANCELLED
                    _orderDetail.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _orderDetail.postValue(Resource.Error(response.message()))
                }
            } catch (e: Exception) {
                _orderDetail.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }
}
