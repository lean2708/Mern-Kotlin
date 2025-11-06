package com.lean2708.mern.ui.orders

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lean2708.mern.data.model.CreateReviewRequest
import com.lean2708.mern.data.model.UpdateReviewRequest
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentReviewFormBinding
import com.lean2708.mern.repository.OrderRepository
import com.lean2708.mern.repository.ProfileRepository
import com.lean2708.mern.ui.orders.adapter.ReviewFormImageAdapter
import com.lean2708.mern.ui.viewmodel.Resource
import com.lean2708.mern.ui.viewmodel.ReviewViewModel
import com.lean2708.mern.ui.viewmodel.ReviewViewModelFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ReviewFormFragment : Fragment() {

    private var _binding: FragmentReviewFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReviewViewModel by viewModels {
        ReviewViewModelFactory(
            OrderRepository(RetrofitInstance.api),
            ProfileRepository(RetrofitInstance.api)
        )
    }

    private lateinit var imageAdapter: ReviewFormImageAdapter
    private var productId: String? = null
    private var reviewId: String? = null // Null nếu là Tạo mới

    // Danh sách ảnh (Kết hợp ảnh cũ (String) và ảnh mới (Uri))
    private var imageList = mutableListOf<Any>()

    // Activity Result Launcher để chọn ảnh
    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        // Thêm các ảnh mới (Uri) vào danh sách
        imageList.addAll(uris)
        updateImageAdapter()
    }

    companion object {
        private const val ARG_PRODUCT_ID = "product_id"
        private const val ARG_REVIEW_ID = "review_id"

        fun newInstance(productId: String, reviewId: String?): ReviewFormFragment {
            val fragment = ReviewFormFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_PRODUCT_ID, productId)
                reviewId?.let { putString(ARG_REVIEW_ID, it) }
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = arguments?.getString(ARG_PRODUCT_ID)
        reviewId = arguments?.getString(ARG_REVIEW_ID)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReviewFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }

        setupRecyclerView()
        setupListeners()
        setupObservers()

        if (reviewId != null) {
            // Chế độ Sửa: Tải chi tiết review cũ (API 3)
            binding.toolbar.title = "Sửa đánh giá"
            viewModel.getReviewDetail(reviewId!!)
        } else {
            // Chế độ Tạo mới
            binding.toolbar.title = "Viết đánh giá"
        }
    }

    private fun setupRecyclerView() {
        imageAdapter = ReviewFormImageAdapter(
            onAddClick = {
                // Logic này hiện không dùng, vì chúng ta dùng nút riêng
            },
            onDeleteClick = { image ->
                imageList.remove(image)
                updateImageAdapter()
            }
        )
        binding.rvReviewImages.adapter = imageAdapter
        binding.rvReviewImages.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
    }

    private fun updateImageAdapter() {
        imageAdapter.submitList(imageList.toList()) // Cập nhật adapter với danh sách mới
    }

    private fun setupObservers() {
        // Lắng nghe chi tiết review (khi sửa)
        viewModel.reviewDetail.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                resource.data?.let { review ->
                    binding.ratingBar.rating = review.rating.toFloat()
                    binding.etComment.setText(review.comment)
                    imageList.addAll(review.reviewImages ?: emptyList())
                    updateImageAdapter()
                }
            }
        }

        // Lắng nghe kết quả Upload ảnh (API 5)
        viewModel.imageUploadResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    // Upload thành công, lấy URL và gửi Review
                    val newImageUrls = resource.data?.map { it.imageUrl } ?: emptyList()
                    // Lấy các ảnh cũ (String) và gộp với ảnh mới (String)
                    val oldImageUrls = imageList.filterIsInstance<String>()
                    submitReview(oldImageUrls + newImageUrls)
                }
                is Resource.Error -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Lỗi tải ảnh: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> setLoading(true)
            }
        }

        // Lắng nghe kết quả Gửi/Cập nhật (API 2 & 4)
        viewModel.submitResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Gửi đánh giá thành công!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack() // Quay về Chi tiết Đơn hàng
                }
                is Resource.Error -> {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Lỗi: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> setLoading(true)
            }
        }
    }

    private fun setupListeners() {
        binding.btnSubmitReview.setOnClickListener {
            // Bước 1: Upload ảnh (nếu có ảnh mới)
            uploadImagesIfNeeded()
        }

        // SỬA: KÍCH HOẠT IMAGE PICKER
        binding.btnAddImage.setOnClickListener {
            imagePicker.launch("image/*") // Mở trình chọn ảnh
        }
    }

    private fun uploadImagesIfNeeded() {
        val newImages = imageList.filterIsInstance<Uri>() // Chỉ lấy ảnh mới (Uri)

        if (newImages.isNotEmpty()) {
            // Có ảnh mới -> Gọi API 5
            setLoading(true) // Bắt đầu loading
            val imageParts = newImages.mapNotNull { uri ->
                try {
                    context?.contentResolver?.openInputStream(uri)?.let { inputStream ->
                        val requestBody = inputStream.readBytes().toRequestBody("image/*".toMediaTypeOrNull())
                        // API 5 yêu cầu key là "files"
                        MultipartBody.Part.createFormData("files", "image.jpg", requestBody)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            if (imageParts.isNotEmpty()) {
                viewModel.uploadImages(imageParts)
            } else {
                setLoading(false)
                Toast.makeText(requireContext(), "Không thể đọc file ảnh.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Không có ảnh mới -> Gửi review luôn
            submitReview(imageList.filterIsInstance<String>()) // Chỉ gửi ảnh cũ (String URL)
        }
    }

    private fun submitReview(finalImageUrls: List<String>) {
        val rating = binding.ratingBar.rating.toInt()
        val comment = binding.etComment.text.toString()

        if (rating == 0) {
            Toast.makeText(requireContext(), "Vui lòng chọn số sao", Toast.LENGTH_SHORT).show()
            setLoading(false) // Tắt loading nếu validation thất bại
            return
        }

        if (reviewId != null) {
            // API 4: Cập nhật
            val request = UpdateReviewRequest(rating, comment, finalImageUrls)
            viewModel.updateReview(reviewId!!, request)
        } else {
            // API 2: Tạo mới
            val request = CreateReviewRequest(productId!!, rating, comment, finalImageUrls)
            viewModel.createReview(request)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmitReview.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}