package com.lean2708.mern.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lean2708.mern.R
import com.lean2708.mern.data.model.DetailedCartItem
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.FragmentCartBinding
import com.lean2708.mern.databinding.ItemCartProductBinding
import com.lean2708.mern.repository.CartRepository
import com.lean2708.mern.ui.viewmodel.CartActionResource
import com.lean2708.mern.ui.viewmodel.CartViewModel
import com.lean2708.mern.ui.viewmodel.CartViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var cartAdapter: CartAdapter

    // TODO: Cần tạo CartViewModelFactory
    private val viewModel: CartViewModel by viewModels {
        CartViewModelFactory(CartRepository(RetrofitInstance.api))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onQuantityChange = { cartId, quantity ->
                viewModel.updateCartQuantity(cartId, quantity)
            },
            onDelete = { cartId ->
                viewModel.deleteCartItem(cartId)
            }
        )
        binding.rvCartItems.adapter = cartAdapter
    }

    private fun setupListeners() {
        binding.btnCheckout.setOnClickListener {
            Toast.makeText(requireContext(), "Chuyển tới thanh toán", Toast.LENGTH_SHORT).show()
            // TODO: Logic điều hướng thanh toán
        }
    }

    private fun setupObservers() {
        // 1. Lắng nghe danh sách giỏ hàng
        viewModel.cartItems.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success<*> -> {
                    binding.progressBar.visibility = View.GONE
                    @Suppress("UNCHECKED_CAST")
                    val items = resource.data as? List<DetailedCartItem> ?: emptyList()
                    cartAdapter.submitList(items)
                    updateSummary(items)
                }
                is Resource.Error<*> -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Lỗi tải giỏ hàng: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // 2. Lắng nghe kết quả thao tác (Update/Delete)
        viewModel.cartActionResult.observe(viewLifecycleOwner) { resource ->
            when(resource) {
                is CartActionResource.Loading -> binding.btnCheckout.isEnabled = false
                is CartActionResource.Success -> { // <-- SỬA LỖI ÉP KIỂU
                    binding.btnCheckout.isEnabled = true
                    Toast.makeText(requireContext(), resource.msg, Toast.LENGTH_SHORT).show() // Dùng resource.msg
                    // Rất quan trọng: Tải lại giỏ hàng sau khi thao tác
                    viewModel.viewCartProducts()
                }
                is CartActionResource.Error -> { // <-- SỬA LỖI ÉP KIỂU
                    binding.btnCheckout.isEnabled = true
                    Toast.makeText(requireContext(), resource.msg, Toast.LENGTH_LONG).show() // Dùng resource.msg
                }
                else -> Unit
            }
        }
    }

    private fun updateSummary(items: List<DetailedCartItem>) {
        val count = items.size
        val total = viewModel.calculateTotalPrice(items)
        val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

        binding.tvCartTitle.text = "Giỏ hàng của tôi ($count sản phẩm)"
        binding.tvTotalPrice.text = formatter.format(total)
        binding.bottomCheckoutLayout.visibility = if (count > 0) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- ADAPTER ---
    class CartAdapter(
        private val onQuantityChange: (String, Int) -> Unit,
        private val onDelete: (String) -> Unit
    ) : androidx.recyclerview.widget.ListAdapter<DetailedCartItem, CartAdapter.CartViewHolder>(DiffCallback()) {

        inner class CartViewHolder(private val binding: ItemCartProductBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: DetailedCartItem) {
                val product = item.productId
                val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

                binding.tvProductName.text = product.productName
                binding.tvSellingPrice.text = formatter.format(product.sellingPrice)
                binding.tvQuantity.text = item.quantity.toString()

                Glide.with(binding.imgProduct.context).load(product.productImage.firstOrNull()).into(binding.imgProduct)

                // Listeners
                binding.btnDelete.setOnClickListener { onDelete(item._id) }
                binding.btnIncrease.setOnClickListener { onQuantityChange(item._id, item.quantity + 1) }
                binding.btnDecrease.setOnClickListener {
                    if (item.quantity > 1) {
                        onQuantityChange(item._id, item.quantity - 1)
                    } else {
                        // Có thể hiển thị dialog xác nhận xóa nếu quantity = 1
                        onDelete(item._id)
                    }
                }
            }
        }

        private class DiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<DetailedCartItem>() {
            override fun areItemsTheSame(oldItem: DetailedCartItem, newItem: DetailedCartItem): Boolean = oldItem._id == newItem._id
            override fun areContentsTheSame(oldItem: DetailedCartItem, newItem: DetailedCartItem): Boolean = oldItem == newItem
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
            val binding = ItemCartProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CartViewHolder(binding)
        }

        override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }
}