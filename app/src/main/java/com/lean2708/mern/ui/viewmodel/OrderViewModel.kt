package com.lean2708.mern.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lean2708.mern.data.model.*
import com.lean2708.mern.repository.OrderRepository
import com.lean2708.mern.repository.ProfileRepository
import kotlinx.coroutines.launch
import com.google.gson.Gson
import retrofit2.Response

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

    // --- LIVE DATA CƠ BẢN ---
    private val _orders = MutableLiveData<Resource<List<Order>>>()
    val orders: LiveData<Resource<List<Order>>> = _orders

    private val _orderDetail = MutableLiveData<Resource<Order>>()
    val orderDetail: LiveData<Resource<Order>> = _orderDetail

    private val _cancelOrderResult = MutableLiveData<Resource<Order>>()
    val cancelOrderResult: LiveData<Resource<Order>> = _cancelOrderResult

    private val _selectedStatus = MutableLiveData(OrderStatus.PENDING)
    val selectedStatus: LiveData<OrderStatus> = _selectedStatus

    // --- LIVE DATA CHO CHECKOUT ---
    private val _defaultAddress = MutableLiveData<Resource<List<Address>>>() // Trả về List
    val defaultAddress: LiveData<Resource<List<Address>>> = _defaultAddress

    private val _vnpayOrderResult = MutableLiveData<Resource<VnpayOrderResponse>>()
    val vnpayOrderResult: LiveData<Resource<VnpayOrderResponse>> = _vnpayOrderResult

    private val _cashOrderResult = MutableLiveData<Resource<SimpleOrder>>() // Dùng SimpleOrder
    val cashOrderResult: LiveData<Resource<SimpleOrder>> = _cashOrderResult

    private val _vnpayCallbackResult = MutableLiveData<Resource<GenericResponse>>()
    val vnpayCallbackResult: LiveData<Resource<GenericResponse>> = _vnpayCallbackResult

    init {
        // Tải đơn hàng mặc định khi ViewModel khởi tạo
        fetchOrdersByStatus(selectedStatus.value!!.value)
    }

    fun setSelectedStatus(status: OrderStatus) {
        if (_selectedStatus.value != status) {
            _selectedStatus.value = status
            fetchOrdersByStatus(status.value)
        }
    }

    // Hàm Helper để lấy message lỗi từ ErrorBody
    private fun parseError(response: Response<*>): String {
        return try {
            val errorJson = response.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorJson, GenericResponse::class.java)
            errorResponse.message ?: response.message()
        } catch (e: Exception) {
            response.message()
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
                    _orders.postValue(Resource.Error(parseError(response)))
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
                    _orderDetail.postValue(Resource.Error(parseError(response)))
                }
            } catch (e: Exception) {
                _orderDetail.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    // --- API 3: Hủy đơn hàng ---
    fun cancelOrder(orderId: String) {
        _orderDetail.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.cancelOrder(orderId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _orderDetail.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _orderDetail.postValue(Resource.Error(parseError(response)))
                }
            } catch (e: Exception) {
                _orderDetail.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    // --- API: LẤY ĐỊA CHỈ (Trả về List<Address>) ---
    fun fetchDefaultAddress(addressRepository: ProfileRepository) {
        _defaultAddress.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = addressRepository.getAddresses()
                if (response.isSuccessful && response.body()?.success == true) {
                    // Trả về List<Address>
                    _defaultAddress.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _defaultAddress.postValue(Resource.Error(parseError(response)))
                }
            } catch (e: Exception) {
                _defaultAddress.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    // --- API: ĐẶT HÀNG CHUNG ---
    fun createOrder(request: CreateOrderRequest) {
        if (request.paymentMethod == "VNPAY") {
            createVnpayOrder(request)
        } else {
            createCashOrder(request)
        }
    }

    private fun createCashOrder(request: CreateOrderRequest) {
        _cashOrderResult.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.createCashOrder(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _cashOrderResult.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _cashOrderResult.postValue(Resource.Error(parseError(response)))
                }
            } catch (e: Exception) {
                _cashOrderResult.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    private fun createVnpayOrder(request: CreateOrderRequest) {
        _vnpayOrderResult.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.createVnpayOrder(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _vnpayOrderResult.postValue(Resource.Success(response.body()!!))
                } else {
                    _vnpayOrderResult.postValue(Resource.Error(parseError(response)))
                }
            } catch (e: Exception) {
                _vnpayOrderResult.postValue(Resource.Error(e.message ?: "Lỗi mạng VNPAY"))
            }
        }
    }

    // --- API: XỬ LÝ VNPAY CALLBACK ---
    fun handleVnPayReturn(params: Map<String, String>) {
        _vnpayCallbackResult.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.checkVnpayCallback(params)
                if (response.isSuccessful && response.body()?.success == true) {
                    _vnpayCallbackResult.postValue(Resource.Success(response.body()!!))
                } else {
                    _vnpayCallbackResult.postValue(Resource.Error(parseError(response)))
                }
            } catch (e: Exception) {
                _vnpayCallbackResult.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }
}