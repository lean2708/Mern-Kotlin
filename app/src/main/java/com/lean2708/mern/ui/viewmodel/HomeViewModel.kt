package com.lean2708.mern.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lean2708.mern.repository.HomeRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {

    private val _homeItems = MutableLiveData<Resource<List<HomeDisplayItem>>>()
    val homeItems: LiveData<Resource<List<HomeDisplayItem>>> = _homeItems

    init {
        loadHomePageData()
    }

    fun loadHomePageData() {
        viewModelScope.launch {
            _homeItems.postValue(Resource.Loading()) // Bắt đầu tải

            try {
                // 1. Gọi API lấy danh sách category (sản phẩm đại diện)
                val categoryResponse = repository.getCategoryProducts()

                if (categoryResponse.isSuccessful && categoryResponse.body() != null) {
                    val categoryProducts = categoryResponse.body()!!.data

                    // Danh sách cuối cùng để hiển thị lên UI
                    val finalDisplayList = mutableListOf<HomeDisplayItem>()

                    // Thêm mục đầu tiên: Danh sách Category
                    finalDisplayList.add(HomeDisplayItem.Categories(categoryProducts))

                    // 2. Lặp qua từng category, gọi API lấy danh sách sản phẩm của nó
                    // Dùng async/awaitAll để các lệnh gọi này chạy song song
                    val productSections = categoryProducts.map { categoryProduct ->
                        async {
                            val productListResponse = repository.getProductsForCategory(categoryProduct.category)
                            if (productListResponse.isSuccessful && productListResponse.body() != null) {
                                HomeDisplayItem.ProductSection(
                                    categoryName = categoryProduct.category,
                                    products = productListResponse.body()!!.data
                                )
                            } else {
                                null // Bỏ qua nếu lỗi
                            }
                        }
                    }.awaitAll().filterNotNull() // Đợi tất cả hoàn thành và lọc ra các mục bị null

                    // Thêm các mục sản phẩm vào danh sách cuối cùng
                    finalDisplayList.addAll(productSections)

                    // 3. Gửi danh sách hoàn chỉnh lên UI
                    _homeItems.postValue(Resource.Success(finalDisplayList))

                } else {
                    _homeItems.postValue(Resource.Error("Lỗi tải danh sách category"))
                }
            } catch (e: Exception) {
                _homeItems.postValue(Resource.Error(e.message ?: "Lỗi không xác định"))
            }
        }
    }
}