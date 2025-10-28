package com.lean2708.mern.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lean2708.mern.data.model.*
import com.lean2708.mern.repository.ProfileRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

// Dùng lại Resource<T> từ HomeViewModel
class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    // --- User Details ---
    private val _userDetails = MutableLiveData<Resource<User>>()
    val userDetails: LiveData<Resource<User>> = _userDetails


    private val _updateUserResult = MutableLiveData<Resource<User>>()
    val updateUserResult: LiveData<Resource<User>> = _updateUserResult

    // LIVE DATA CHO KẾT QUẢ UPLOAD AVATAR (SỬA LỖI Unresolved reference 'uploadAvatarResult')
    private val _uploadAvatarResult = MutableLiveData<Resource<User>>()
    val uploadAvatarResult: LiveData<Resource<User>> = _uploadAvatarResult

    fun fetchUserDetails() {
        _userDetails.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.getUserDetails()
                if (response.isSuccessful && response.body() != null) {
                    _userDetails.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _userDetails.postValue(Resource.Error(response.message()))
                }
            } catch (e: Exception) {
                _userDetails.postValue(Resource.Error(e.message ?: "Lỗi tải thông tin"))
            }
        }
    }

    // (Thêm các hàm cho update user, upload avatar, change password, address...)

    // --- Address ---
    private val _addresses = MutableLiveData<Resource<List<Address>>>()
    val addresses: LiveData<Resource<List<Address>>> = _addresses

    fun fetchAddresses() {
        _addresses.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.getAddresses()
                if (response.isSuccessful && response.body() != null) {
                    _addresses.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _addresses.postValue(Resource.Error(response.message()))
                }
            } catch (e: Exception) {
                _addresses.postValue(Resource.Error(e.message ?: "Lỗi tải địa chỉ"))
            }
        }
    }

    fun updateUserDetails(request: UpdateUserRequest) {
        _updateUserResult.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.updateUserDetails(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedUser = response.body()!!.data
                    _updateUserResult.postValue(Resource.Success(updatedUser))
                    _userDetails.postValue(Resource.Success(updatedUser)) // Cập nhật UI
                } else {
                    _updateUserResult.postValue(Resource.Error(response.body()?.message ?: "Lỗi cập nhật"))
                }
            } catch (e: Exception) {
                _updateUserResult.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    // --- HÀM UPLOAD AVATAR (SỬA LỖI Unresolved reference 'uploadAvatar') ---
    fun uploadAvatar(filePart: MultipartBody.Part) {
        _uploadAvatarResult.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.uploadAvatar(filePart)
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedUser = response.body()!!.data
                    _uploadAvatarResult.postValue(Resource.Success(updatedUser))
                    _userDetails.postValue(Resource.Success(updatedUser)) // Cập nhật UI
                } else {
                    _uploadAvatarResult.postValue(Resource.Error(response.body()?.message ?: "Lỗi upload"))
                }
            } catch (e: Exception) {
                _uploadAvatarResult.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    private val _changePasswordResult = MutableLiveData<Resource<GenericResponse>>()
    val changePasswordResult: LiveData<Resource<GenericResponse>> = _changePasswordResult

    // --- HÀM CHANGE PASSWORD ---
    fun changePassword(request: ChangePasswordRequest) {
        _changePasswordResult.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.changePassword(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _changePasswordResult.postValue(Resource.Success(response.body()!!))
                } else {
                    _changePasswordResult.postValue(Resource.Error(response.body()?.message ?: "Lỗi đổi mật khẩu"))
                }
            } catch (e: Exception) {
                _changePasswordResult.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }


    private val _deleteAddressResult = MutableLiveData<Resource<GenericResponse>>()
    val deleteAddressResult: LiveData<Resource<GenericResponse>> = _deleteAddressResult

    // Thêm hàm này vào class ProfileViewModel
    fun deleteAddress(id: String) {
        _deleteAddressResult.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.deleteAddress(id)
                if (response.isSuccessful && response.body()?.success == true) {
                    _deleteAddressResult.postValue(Resource.Success(response.body()!!))
                } else {
                    _deleteAddressResult.postValue(Resource.Error(response.body()?.message ?: "Lỗi xóa"))
                }
            } catch (e: Exception) {
                _deleteAddressResult.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    private val _createAddressResult = MutableLiveData<Resource<Address>>()
    val createAddressResult: LiveData<Resource<Address>> = _createAddressResult

    // Dùng cho kết quả cập nhật địa chỉ (UPDATE)
    private val _updateAddressResult = MutableLiveData<Resource<Address>>()
    val updateAddressResult: LiveData<Resource<Address>> = _updateAddressResult

    // ... (LiveData addresses, fetchAddresses) ...

    // --- HÀM TẠO MỚI (SỬA LỖI Unresolved reference 'createAddress') ---
    fun createAddress(request: AddressRequest) {
        _createAddressResult.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.createAddress(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _createAddressResult.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _createAddressResult.postValue(Resource.Error(response.body()?.message ?: "Lỗi tạo địa chỉ"))
                }
            } catch (e: Exception) {
                _createAddressResult.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    // --- HÀM CẬP NHẬT (SỬA LỖI Unresolved reference 'updateAddress') ---
    fun updateAddress(id: String, request: AddressRequest) {
        _updateAddressResult.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.updateAddress(id, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _updateAddressResult.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _updateAddressResult.postValue(Resource.Error(response.body()?.message ?: "Lỗi cập nhật địa chỉ"))
                }
            } catch (e: Exception) {
                _updateAddressResult.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }
}

// TODO: Tạo ProfileViewModelFactory (giống hệt HomeViewModelFactory nhưng dùng ProfileRepository)