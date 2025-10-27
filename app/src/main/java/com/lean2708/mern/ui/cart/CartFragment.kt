package com.lean2708.mern.ui.cart
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lean2708.mern.R

class CartFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Chỉ cần inflate layout
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }
}