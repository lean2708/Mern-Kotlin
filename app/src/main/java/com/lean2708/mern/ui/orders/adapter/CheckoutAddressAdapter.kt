package com.lean2708.mern.ui.orders.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lean2708.mern.R
import com.lean2708.mern.data.model.Address
import com.lean2708.mern.databinding.ItemCheckoutAddressBinding

class CheckoutAddressAdapter(
    private val onAddressSelected: (Address) -> Unit
) : ListAdapter<Address, CheckoutAddressAdapter.AddressViewHolder>(DiffCallback()) {

    /** ID địa chỉ hiện đang được chọn (do CheckoutFragment truyền vào) */
    var selectedAddressId: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class AddressViewHolder(private val binding: ItemCheckoutAddressBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(address: Address) {
            val context = binding.root.context
            val isSelected = address._id == selectedAddressId

            // Gán dữ liệu text
            binding.tvAddressName.text = "Địa chỉ"
            binding.tvAddressDetail.text = address.addressDetail
            binding.tvPhone.text = address.phone
            binding.tvIsDefault.visibility = if (address.isDefault) View.VISIBLE else View.GONE

            // --- XỬ LÝ HIGHLIGHT ---
            val bgRes = if (isSelected) R.drawable.bg_address_selected else R.drawable.bg_address_unselected
            binding.layoutAddressRoot.background = ContextCompat.getDrawable(context, bgRes)

            val colorPrimary = ContextCompat.getColor(context, R.color.colorTextPrimary)
            val colorSecondary = ContextCompat.getColor(context, R.color.colorTextSecondary)
            val colorWhite = ContextCompat.getColor(context, R.color.white)

            binding.tvAddressName.setTextColor(if (isSelected) colorWhite else colorPrimary)
            binding.tvAddressDetail.setTextColor(if (isSelected) colorWhite else colorPrimary)
            binding.tvPhone.setTextColor(if (isSelected) colorWhite else colorSecondary)

            // --- XỬ LÝ CLICK ---
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onAddressSelected(address)
                    selectedAddressId = address._id
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<Address>() {
        override fun areItemsTheSame(oldItem: Address, newItem: Address): Boolean =
            oldItem._id == newItem._id

        override fun areContentsTheSame(oldItem: Address, newItem: Address): Boolean =
            oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val binding =
            ItemCheckoutAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
