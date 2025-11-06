package com.lean2708.mern.ui.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // Dùng ViewModel chung
import com.lean2708.mern.data.model.ProductReview
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentAllReviewsBinding
import com.lean2708.mern.repository.HomeRepository
import com.lean2708.mern.ui.home.adapter.ReviewAdapter
import com.lean2708.mern.ui.viewmodel.HomeViewModel
import com.lean2708.mern.ui.viewmodel.HomeViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource

class AllReviewsFragment : Fragment() {

    private var _binding: FragmentAllReviewsBinding? = null
    private val binding get() = _binding!!
    private lateinit var reviewAdapter: ReviewAdapter

    // Sử dụng chung HomeViewModel
    private val viewModel: HomeViewModel by activityViewModels {
        HomeViewModelFactory(HomeRepository(RetrofitInstance.api))
    }

    private var productId: String? = null

    companion object {
        const val ARG_PRODUCT_ID = "product_id"
        fun newInstance(productId: String): AllReviewsFragment {
            val fragment = AllReviewsFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_PRODUCT_ID, productId)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = arguments?.getString(ARG_PRODUCT_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        setupRecyclerView()
        setupObservers()

        // 1. Kiểm tra xem ViewModel đã có dữ liệu chưa (nếu vừa xem chi tiết)
        val cachedReviews = (viewModel.productReviews.value as? Resource.Success)?.data
        if (cachedReviews != null && cachedReviews.firstOrNull()?.product == productId) {
            reviewAdapter.submitList(cachedReviews)
        } else {
            // 2. Nếu không, gọi API để tải lại
            productId?.let { viewModel.fetchProductReviews(it) }
        }
    }

    private fun setupRecyclerView() {
        reviewAdapter = ReviewAdapter()
        binding.rvAllReviews.adapter = reviewAdapter
    }

    private fun setupObservers() {
        // Chỉ lắng nghe nếu cần tải lại (trường hợp cache không có)
        viewModel.productReviews.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success<*> -> {
                    binding.progressBar.visibility = View.GONE
                    @Suppress("UNCHECKED_CAST")
                    val reviews = resource.data as? List<ProductReview> ?: emptyList()
                    // Lọc lại đúng sản phẩm (vì HomeViewModel có thể đang giữ review của sản phẩm khác)
                    val currentProductReviews = reviews.filter { it.product == productId }
                    reviewAdapter.submitList(currentProductReviews)
                }
                is Resource.Error<*> -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Lỗi tải đánh giá: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}