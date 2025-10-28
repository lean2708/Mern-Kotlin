package com.lean2708.mern.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lean2708.mern.data.model.DetailedCartItem
import com.lean2708.mern.data.model.GenericResponse
import com.lean2708.mern.repository.CartRepository
import kotlinx.coroutines.launch

// Lớp này dùng chung cho các kết quả Update/Delete
sealed class CartActionResource(val message: String? = null) {
    object Loading : CartActionResource()
    data class Success(val msg: String) : CartActionResource(msg)
    data class Error(val msg: String) : CartActionResource(msg)
}

class CartViewModel(private val repository: CartRepository) : ViewModel() {

    // 1. Danh sách chi tiết sản phẩm trong giỏ
    private val _cartItems = MutableLiveData<Resource<List<DetailedCartItem>>>()
    val cartItems: LiveData<Resource<List<DetailedCartItem>>> = _cartItems

    // 2. Kết quả cập nhật/xóa (dùng chung)
    private val _cartActionResult = MutableLiveData<CartActionResource>()
    val cartActionResult: LiveData<CartActionResource> = _cartActionResult

    // 3. Số lượng item (cần nếu muốn hiển thị badge trên BottomNav)
    private val _cartCount = MutableLiveData<Int>()
    val cartCount: LiveData<Int> = _cartCount


    init {
        viewCartProducts()
    }

    // --- READ (Xem giỏ hàng) ---
    fun viewCartProducts() {
        _cartItems.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.viewCartProducts()
                if (response.isSuccessful && response.body()?.success == true) {
                    _cartItems.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _cartItems.postValue(Resource.Error("Lỗi tải giỏ hàng"))
                }
            } catch (e: Exception) {
                _cartItems.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    // --- UPDATE (Cập nhật số lượng) ---
    fun updateCartQuantity(cartId: String, newQuantity: Int) {
        _cartActionResult.postValue(CartActionResource.Loading)
        viewModelScope.launch {
            try {
                val response = repository.updateCartProduct(cartId, newQuantity)
                if (response.isSuccessful && response.body()?.success == true) {
                    _cartActionResult.postValue(CartActionResource.Success("Cập nhật số lượng thành công"))
                    viewCartProducts() // Tải lại giỏ hàng để cập nhật UI
                } else {
                    _cartActionResult.postValue(CartActionResource.Error(response.body()?.message ?: "Cập nhật thất bại"))
                }
            } catch (e: Exception) {
                _cartActionResult.postValue(CartActionResource.Error("Lỗi mạng khi cập nhật"))
            }
        }
    }

    // --- DELETE (Xóa sản phẩm) ---
    fun deleteCartItem(cartId: String) {
        _cartActionResult.postValue(CartActionResource.Loading)
        viewModelScope.launch {
            try {
                val response = repository.deleteCartProduct(cartId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _cartActionResult.postValue(CartActionResource.Success("Đã xóa sản phẩm khỏi giỏ hàng"))
                    viewCartProducts() // Tải lại giỏ hàng để cập nhật UI
                } else {
                    _cartActionResult.postValue(CartActionResource.Error(response.body()?.message ?: "Xóa thất bại"))
                }
            } catch (e: Exception) {
                _cartActionResult.postValue(CartActionResource.Error("Lỗi mạng khi xóa"))
            }
        }
    }

    // --- COUNT (Đếm số lượng) ---
    fun getCartCount() {
        viewModelScope.launch {
            try {
                val response = repository.getCartCount()
                if (response.isSuccessful && response.body()?.success == true) {
                    _cartCount.postValue(response.body()!!.data.count)
                } else {
                    _cartCount.postValue(0)
                }
            } catch (e: Exception) {
                _cartCount.postValue(0)
            }
        }
    }

    // Hàm tính tổng tiền (client-side)
    fun calculateTotalPrice(items: List<DetailedCartItem>): Long {
        return items.sumOf { it.productId.sellingPrice * it.quantity }
    }
}

// TODO: Cần tạo CartViewModelFactory (tương tự như các factory khác)