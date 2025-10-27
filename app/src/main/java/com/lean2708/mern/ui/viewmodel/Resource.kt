package com.lean2708.mern.ui.viewmodel

// Lớp Wrapper để quản lý trạng thái Tải, Thành công, Thất bại
sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Loading<T> : Resource<T>()
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}