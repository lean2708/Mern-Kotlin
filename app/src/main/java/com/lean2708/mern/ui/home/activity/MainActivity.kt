package com.lean2708.mern.ui.home.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.lean2708.mern.R
import com.lean2708.mern.databinding.ActivityMainBinding
import com.lean2708.mern.ui.cart.CartFragment
import com.lean2708.mern.ui.home.fragment.HomeFragment
import com.lean2708.mern.ui.orders.OrdersFragment
import com.lean2708.mern.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Khởi tạo các Fragment
    private val homeFragment = HomeFragment()
    private val cartFragment = CartFragment()
    private val ordersFragment = OrdersFragment() // Bạn tự tạo fragment này nhé
    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Đặt Fragment mặc định là HomeFragment
        loadFragment(homeFragment, "Trang chủ")

        // Xử lý sự kiện click bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(homeFragment, "Trang chủ")
                    true
                }
                R.id.nav_cart -> {
                    loadFragment(cartFragment, "Giỏ hàng")
                    true
                }
                R.id.nav_orders -> {
                    loadFragment(ordersFragment, "Đơn hàng")
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(profileFragment, "Cá nhân")
                    true
                }
                else -> false
            }
        }
    }

    // Hàm helper để thay đổi Fragment
    private fun loadFragment(fragment: Fragment, title: String) {

        // Thay đổi Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}