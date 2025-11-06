package com.lean2708.mern.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lean2708.mern.data.model.DetailedCartItem
import com.lean2708.mern.data.model.GenericResponse
import com.lean2708.mern.repository.CartRepository
import kotlinx.coroutines.launch
import android.util.Log

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

    // 4. DANH SÁCH CÁC ITEM ĐÃ CHỌN (Lưu trữ bằng Cart Item ID)
    private val _selectedItems = MutableLiveData<Set<String>>(emptySet())
    val selectedItems: LiveData<Set<String>> = _selectedItems

    init {
        // SỬA LỖI "LOAD MÃI": Xóa hàm "viewCartProducts()" khỏi init()
        _cartCount.postValue(0)
        _selectedItems.postValue(emptySet())
    }

    // --- READ (Xem giỏ hàng) ---
    // Hàm này sẽ chỉ được gọi bởi Fragment khi cần (onResume)
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

    // --- HÀM XỬ LÝ CHỌN ITEM ---
    fun toggleItemSelection(cartItemId: String, isChecked: Boolean) {
        val currentSet = _selectedItems.value ?: emptySet()
        val newSet = currentSet.toMutableSet()
        if (isChecked) {
            newSet.add(cartItemId)
        } else {
            newSet.remove(cartItemId)
        }
        _selectedItems.postValue(newSet)
    }

    // --- XÓA CÁC LỰA CHỌN KHI RỜI KHỎI MÀN HÌNH ---
    fun clearSelections() {
        _selectedItems.postValue(emptySet())
    }

    // --- TÍNH TỔNG TIỀN DỰA TRÊN CÁC LỰA CHỌN ---
    fun calculateTotalPrice(items: List<DetailedCartItem>, selectedIds: Set<String>): Long {
        return items
            .filter { it._id in selectedIds } // Chỉ tính tiền các item được chọn
            .sumOf { it.productId.sellingPrice * it.quantity }
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

    // --- HÀM MỚI: XÓA CÁC SẢN PHẨM ĐÃ CHỌN (SAU KHI THANH TOÁN) ---
    fun clearSelectedItemsFromCart() {
        val selectedIds = _selectedItems.value
        if (selectedIds.isNullOrEmpty()) return

        Log.d("CartViewModel", "Đang xóa ${selectedIds.size} sản phẩm đã chọn khỏi giỏ hàng...")

        viewModelScope.launch {
            try {
                // Gọi API xóa cho từng item đã chọn
                selectedIds.forEach { cartId ->
                    repository.deleteCartProduct(cartId)
                    Log.d("CartViewModel", "Đã gửi yêu cầu xóa cho: $cartId")
                }
                // Sau khi xóa xong, tải lại giỏ hàng và xóa lựa chọn
                viewCartProducts()
                clearSelections()
            } catch (e: Exception) {
                _cartActionResult.postValue(CartActionResource.Error("Lỗi khi xóa sản phẩm đã mua"))
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
}