package com.lean2708.mern.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lean2708.mern.data.model.*
import com.lean2708.mern.repository.OrderRepository
import com.lean2708.mern.repository.ProfileRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import com.google.gson.Gson // Cần import Gson
import retrofit2.Response // Cần import Response

class ReviewViewModel(
    private val orderRepository: OrderRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // SỬA: Dùng DetailedProductReview cho API 3
    private val _reviewDetail = MutableLiveData<Resource<DetailedProductReview>>()
    val reviewDetail: LiveData<Resource<DetailedProductReview>> = _reviewDetail

    // (Giữ nguyên)
    private val _imageUploadResult = MutableLiveData<Resource<List<ImageUploadResult>>>()
    val imageUploadResult: LiveData<Resource<List<ImageUploadResult>>> = _imageUploadResult

    // SỬA: Dùng SimpleProductReview cho API 2 & 4
    private val _submitResult = MutableLiveData<Resource<SimpleProductReview>>()
    val submitResult: LiveData<Resource<SimpleProductReview>> = _submitResult

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

    // --- API 3: Lấy chi tiết Review cũ (Dùng DetailedProductReview) ---
    fun getReviewDetail(reviewId: String) {
        _reviewDetail.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = orderRepository.getReviewDetail(reviewId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _reviewDetail.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _reviewDetail.postValue(Resource.Error(parseError(response)))
                }
            } catch (e: Exception) {
                _reviewDetail.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    // --- API 5: Tải ảnh lên ---
    fun uploadImages(images: List<MultipartBody.Part>) {
        _imageUploadResult.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = profileRepository.uploadImages(images)
                if (response.isSuccessful && response.body()?.success == true) {
                    _imageUploadResult.postValue(Resource.Success(response.body()!!.result))
                } else {
                    _imageUploadResult.postValue(Resource.Error(parseError(response)))
                }
            } catch (e: Exception) {
                _imageUploadResult.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    // --- API 2: Gửi Review MỚI (Dùng SimpleProductReview) ---
    fun createReview(request: CreateReviewRequest) {
        _submitResult.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = orderRepository.addReview(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _submitResult.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _submitResult.postValue(Resource.Error(parseError(response)))
                }
            } catch (e: Exception) {
                _submitResult.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    // --- API 4: Cập nhật Review CŨ (Dùng SimpleProductReview) ---
    fun updateReview(reviewId: String, request: UpdateReviewRequest) {
        _submitResult.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = orderRepository.updateReview(reviewId, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _submitResult.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _submitResult.postValue(Resource.Error(parseError(response)))
                }
            } catch (e: Exception) {
                _submitResult.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }
}