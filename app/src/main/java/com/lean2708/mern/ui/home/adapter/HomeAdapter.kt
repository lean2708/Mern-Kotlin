package com.lean2708.mern.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.lean2708.mern.databinding.ItemHomeCategoriesBinding
import com.lean2708.mern.databinding.ItemHomeProductSectionBinding
import com.lean2708.mern.ui.viewmodel.HomeDisplayItem

class HomeAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Định nghĩa 2 loại ViewType
    companion object {
        private const val VIEW_TYPE_CATEGORIES = 0
        private const val VIEW_TYPE_PRODUCT_SECTION = 1
    }

    // ViewHolder cho mục Category
    inner class CategorySectionViewHolder(val binding: ItemHomeCategoriesBinding) : RecyclerView.ViewHolder(binding.root) {
        private val categoryAdapter = CategoryAdapter()
        init {
            binding.rvCategories.adapter = categoryAdapter
        }
        fun bind(item: HomeDisplayItem.Categories) {
            categoryAdapter.differ.submitList(item.representativeProducts)
        }
    }

    // ViewHolder cho mục Product
    inner class ProductSectionViewHolder(val binding: ItemHomeProductSectionBinding) : RecyclerView.ViewHolder(binding.root) {
        private val productAdapter = ProductAdapter()
        init {
            binding.rvProducts.adapter = productAdapter
        }
        fun bind(item: HomeDisplayItem.ProductSection) {
            binding.tvSectionTitle.text = "Sản phẩm ${item.categoryName}"
            productAdapter.differ.submitList(item.products)
        }
    }

    // DiffCallback cho RecyclerView chính
    private val diffCallback = object : DiffUtil.ItemCallback<HomeDisplayItem>() {
        override fun areItemsTheSame(oldItem: HomeDisplayItem, newItem: HomeDisplayItem): Boolean {
            return when {
                oldItem is HomeDisplayItem.Categories && newItem is HomeDisplayItem.Categories -> true
                oldItem is HomeDisplayItem.ProductSection && newItem is HomeDisplayItem.ProductSection -> oldItem.categoryName == newItem.categoryName
                else -> false
            }
        }
        override fun areContentsTheSame(oldItem: HomeDisplayItem, newItem: HomeDisplayItem): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    // Quyết định xem item ở vị trí `position` là loại nào
    override fun getItemViewType(position: Int): Int {
        return when (differ.currentList[position]) {
            is HomeDisplayItem.Categories -> VIEW_TYPE_CATEGORIES
            is HomeDisplayItem.ProductSection -> VIEW_TYPE_PRODUCT_SECTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // Tạo ViewHolder tương ứng với ViewType
        return when (viewType) {
            VIEW_TYPE_CATEGORIES -> CategorySectionViewHolder(
                ItemHomeCategoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            VIEW_TYPE_PRODUCT_SECTION -> ProductSectionViewHolder(
                ItemHomeProductSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Bind data vào ViewHolder tương ứng
        when (val item = differ.currentList[position]) {
            is HomeDisplayItem.Categories -> (holder as CategorySectionViewHolder).bind(item)
            is HomeDisplayItem.ProductSection -> (holder as ProductSectionViewHolder).bind(item)
        }
    }

    override fun getItemCount() = differ.currentList.size
}