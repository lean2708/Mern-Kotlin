package com.lean2708.mern.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lean2708.mern.data.model.CartItem
import com.lean2708.mern.data.model.Product
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



    private val _productDetails = MutableLiveData<Resource<Product>>()
    val productDetails: LiveData<Resource<Product>> = _productDetails

    // MỚI: LiveData cho sản phẩm đề xuất
    private val _suggestedProducts = MutableLiveData<Resource<List<Product>>>()
    val suggestedProducts: LiveData<Resource<List<Product>>> = _suggestedProducts

    // HÀM MỚI: Lấy chi tiết sản phẩm và sản phẩm đề xuất
    fun fetchProductDetails(id: String) {
        _productDetails.postValue(Resource.Loading())
        _suggestedProducts.postValue(Resource.Loading())

        viewModelScope.launch {
            try {
                // SỬ DỤNG HÀM getProductDetails (API 1)
                val detailResponse = repository.getProductDetails(id)

                if (detailResponse.isSuccessful && detailResponse.body()?.success == true) {
                    val product = detailResponse.body()!!.data
                    _productDetails.postValue(Resource.Success(product))

                    // GỌI HÀM LẤY SẢN PHẨM TƯƠNG TỰ (API 2)
                    fetchSuggestedProducts(product.category, productIdToExclude = id)
                } else {
                    _productDetails.postValue(Resource.Error("Không tải được chi tiết: ${detailResponse.message()}"))
                }
            } catch (e: Exception) {
                _productDetails.postValue(Resource.Error(e.message ?: "Lỗi mạng"))
            }
        }
    }

    // HÀM MỚI: Lấy sản phẩm đề xuất theo Category
    fun fetchSuggestedProducts(category: String, productIdToExclude: String) {
        viewModelScope.launch {
            try {
                val suggestedResponse = repository.getProductsForCategory(category)

                if (suggestedResponse.isSuccessful && suggestedResponse.body()?.success == true) {
                    // Lọc sản phẩm hiện tại ra khỏi danh sách đề xuất
                    val filteredList = suggestedResponse.body()!!.data.filter { it._id != productIdToExclude }
                    _suggestedProducts.postValue(Resource.Success(filteredList))
                } else {
                    _suggestedProducts.postValue(Resource.Error("Không tải được sản phẩm đề xuất."))
                }
            } catch (e: Exception) {
                _suggestedProducts.postValue(Resource.Error(e.message ?: "Lỗi mạng đề xuất"))
            }
        }
    }



    private val _addToCartResult = MutableLiveData<Resource<CartItem>>()
    val addToCartResult: LiveData<Resource<CartItem>> = _addToCartResult

    // --- HÀM ADD TO CART ---
    fun addToCart(productId: String) {
        _addToCartResult.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.addToCart(productId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _addToCartResult.postValue(Resource.Success(response.body()!!.data))
                } else {
                    // Dùng GenericResponse message nếu response body thất bại
                    _addToCartResult.postValue(Resource.Error(response.body()?.message ?: "Thêm vào giỏ thất bại"))
                }
            } catch (e: Exception) {
                _addToCartResult.postValue(Resource.Error(e.message ?: "Lỗi mạng khi thêm giỏ hàng"))
            }
        }
    }


    private val _categoryProductList = MutableLiveData<Resource<List<Product>>>()
    val categoryProductList: LiveData<Resource<List<Product>>> = _categoryProductList

    fun fetchProductsByCategoryOnly(category: String) {
        _categoryProductList.postValue(Resource.Loading())
        viewModelScope.launch {
            try {
                val response = repository.getProductsForCategory(category)
                if (response.isSuccessful && response.body()?.success == true) {
                    _categoryProductList.postValue(Resource.Success(response.body()!!.data))
                } else {
                    _categoryProductList.postValue(Resource.Error(response.body()?.message ?: "Không tải được sản phẩm."))
                }
            } catch (e: Exception) {
                _categoryProductList.postValue(Resource.Error(e.message ?: "Lỗi mạng."))
            }
        }
    }
}