package com.lean2708.mern.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.switchMap // Import cho switchMap
import androidx.lifecycle.liveData      // Import cho liveData builder
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

    // --- 1. BIẾN LIVE DATA ĐẦU VÀO (Input) ---
    private val _selectedStatus = MutableLiveData(OrderStatus.PENDING)
    val selectedStatus: LiveData<OrderStatus> = _selectedStatus

    // --- 2. LIVE DATA ĐẦU RA (Output - Dùng switchMap) ---
    // Fragment sẽ lắng nghe LiveData này
    val orders: LiveData<Resource<List<Order>>> = _selectedStatus.switchMap { status ->
        // Tự động gọi hàm này mỗi khi _selectedStatus thay đổi
        fetchOrdersByStatusReactive(status.value)
    }

    // --- CÁC LIVE DATA CƠ BẢN KHÁC ---
    private val _orderDetail = MutableLiveData<Resource<Order>>()
    val orderDetail: LiveData<Resource<Order>> = _orderDetail

    private val _cancelOrderResult = MutableLiveData<Resource<Order>>()
    val cancelOrderResult: LiveData<Resource<Order>> = _cancelOrderResult

    // --- LIVE DATA CHO CHECKOUT ---
    private val _defaultAddress = MutableLiveData<Resource<List<Address>>>() // Trả về List
    val defaultAddress: LiveData<Resource<List<Address>>> = _defaultAddress

    private val _vnpayOrderResult = MutableLiveData<Resource<VnpayOrderResponse>>()
    val vnpayOrderResult: LiveData<Resource<VnpayOrderResponse>> = _vnpayOrderResult

    private val _cashOrderResult = MutableLiveData<Resource<SimpleOrder>>() // Dùng SimpleOrder
    val cashOrderResult: LiveData<Resource<SimpleOrder>> = _cashOrderResult

    private val _vnpayCallbackResult = MutableLiveData<Resource<GenericResponse>>()
    val vnpayCallbackResult: LiveData<Resource<GenericResponse>> = _vnpayCallbackResult

    // --- LIVE DATA CHO REVIEW ---
    private val _reviewStatusMap = MutableLiveData<Resource<Map<String, CheckReviewResponse>>>()
    val reviewStatusMap: LiveData<Resource<Map<String, CheckReviewResponse>>> = _reviewStatusMap

    private val _reviewDetail = MutableLiveData<Resource<DetailedProductReview>>() // Dùng Detailed
    val reviewDetail: LiveData<Resource<DetailedProductReview>> = _reviewDetail

    private val _reviewSubmitResult = MutableLiveData<Resource<SimpleProductReview>>() // Dùng Simple
    val reviewSubmitResult: LiveData<Resource<SimpleProductReview>> = _reviewSubmitResult

    init {
        // KHÔNG CẦN GỌI GÌ TRONG INIT NỮA
        // switchMap sẽ tự động kích hoạt khi _selectedStatus có giá trị ban đầu
    }

    // --- HÀM THAY ĐỔI TRẠNG THÁI (Input) ---
    fun setSelectedStatus(status: OrderStatus) {
        // Chỉ cần thay đổi giá trị, switchMap sẽ tự làm phần còn lại
        if (_selectedStatus.value != status) {
            _selectedStatus.value = status
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


    // --- API 1: Lấy danh sách đơn hàng (Reactive Version) ---
    private fun fetchOrdersByStatusReactive(status: String): LiveData<Resource<List<Order>>> = liveData {
        emit(Resource.Loading()) // <-- Gửi trạng thái Loading
        try {
            val response = repository.getOrdersByStatus(status)
            if (response.isSuccessful && response.body()?.success == true) {
                emit(Resource.Success(response.body()!!.data)) // <-- Gửi Success
            } else {
                emit(Resource.Error(parseError(response)))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Lỗi mạng"))
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
        _cancelOrderResult.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.cancelOrder(orderId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _cancelOrderResult.postValue(Resource.Success(response.body()!!.data))
                    // Buộc tải lại danh sách
                    _selectedStatus.value?.let { _selectedStatus.postValue(it) }
                } else {
                    _cancelOrderResult.postValue(Resource.Error(parseError(response)))
                }
            } catch (e: Exception) {
                _cancelOrderResult.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
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
                    // Sau khi thanh toán thành công, buộc tải lại danh sách đơn hàng
                    _selectedStatus.value?.let { _selectedStatus.postValue(it) }
                } else {
                    _vnpayCallbackResult.postValue(Resource.Error(parseError(response)))
                }
            } catch (e: Exception) {
                _vnpayCallbackResult.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }


    // --- API: KIỂM TRA TRẠNG THÁI REVIEW (API 1) ---
    fun checkReviewStatusForItems(items: List<OrderItem>) {
        _reviewStatusMap.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val statusMap = mutableMapOf<String, CheckReviewResponse>()
                items.forEach { item ->
                    val product = item.product as? OrderProduct
                    product?.let {
                        val response = repository.checkReview(it._id)
                        if (response.isSuccessful && response.body() != null) {
                            statusMap[it._id] = response.body()!!
                        }
                    }
                }
                _reviewStatusMap.postValue(Resource.Success(statusMap))
            } catch (e: Exception) {
                _reviewStatusMap.postValue(Resource.Error(e.message ?: "Lỗi kiểm tra review"))
            }
        }
    }

}