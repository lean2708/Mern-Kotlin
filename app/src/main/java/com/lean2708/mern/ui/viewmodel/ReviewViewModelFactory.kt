package com.lean2708.mern.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lean2708.mern.repository.OrderRepository
import com.lean2708.mern.repository.ProfileRepository

class ReviewViewModelFactory(
    private val orderRepository: OrderRepository,
    private val profileRepository: ProfileRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReviewViewModel(orderRepository, profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}