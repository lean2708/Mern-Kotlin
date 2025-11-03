package com.lean2708.mern.ui.home.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.lean2708.mern.R
import com.lean2708.mern.data.local.SessionManager
import com.lean2708.mern.data.network.RetrofitInstance
import com.lean2708.mern.databinding.ActivityMainBinding
import com.lean2708.mern.ui.auth.LoginActivity
import com.lean2708.mern.ui.cart.CartFragment
import com.lean2708.mern.ui.home.fragment.HomeFragment
import com.lean2708.mern.ui.orders.OrdersFragment
import com.lean2708.mern.ui.profile.ProfileFragment
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.lean2708.mern.repository.OrderRepository
import com.lean2708.mern.ui.viewmodel.OrderViewModel
import com.lean2708.mern.ui.viewmodel.OrderViewModelFactory
import com.lean2708.mern.ui.viewmodel.Resource

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val sessionManager by lazy { SessionManager(this) }

    // Khởi tạo OrderViewModel (Đã sửa lỗi và chuẩn hóa)
    private val orderViewModel: OrderViewModel by lazy {
        ViewModelProvider(this, OrderViewModelFactory(OrderRepository(RetrofitInstance.api))).get(OrderViewModel::class.java)
    }

    // Khởi tạo các Fragment
    private val homeFragment = HomeFragment()
    private val cartFragment = CartFragment()
    private val ordersFragment = OrdersFragment()
    private val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Đặt Fragment mặc định là HomeFragment
        loadFragment(homeFragment, "Trang chủ")

        binding.bottomNavigation.setOnItemSelectedListener { item ->

            // --- LOGIC KIỂM TRA BẢO MẬT VÀ ĐIỀU HƯỚNG ---
            if (isAuthRequired(item.itemId) && !isUserLoggedIn()) {

                Toast.makeText(this, "Vui lòng đăng nhập để tiếp tục.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))

                // Giữ lại tab Home
                binding.bottomNavigation.selectedItemId = R.id.nav_home
                return@setOnItemSelectedListener false
            }

            when (item.itemId) {
                R.id.nav_home -> loadFragment(homeFragment, "Trang chủ")
                R.id.nav_cart -> loadFragment(cartFragment, "Giỏ hàng")
                R.id.nav_orders -> loadFragment(ordersFragment, "Đơn hàng")

                // Khi người dùng đã login, load Fragment Cá nhân
                R.id.nav_profile -> loadFragment(profileFragment, "Cá nhân")
                else -> false
            }
            true
        }
    }

    private fun isUserLoggedIn(): Boolean {
        return sessionManager.fetchAuthToken() != null
    }

    private fun isAuthRequired(itemId: Int): Boolean {
        // Các tab cần xác thực
        return when (itemId) {
            R.id.nav_cart, R.id.nav_orders, R.id.nav_profile -> true
            else -> false
        }
    }

    private fun loadFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    /**
     * Hàm nhận kết quả thanh toán từ VnPayWebViewFragment (API 4)
     * Đây là nơi lỗi Unresolved Reference đã được sửa.
     */
    fun handleVnPayReturn(vnpayParams: Map<String, String>) {

        // 1. Gọi ViewModel để xác nhận thanh toán với backend
        orderViewModel.handleVnPayReturn(vnpayParams)

        // 2. Observer kết quả và điều hướng
        orderViewModel.vnpayCallbackResult.observe(this) { resource ->
            when (resource) {
                is Resource.Success -> {
                    Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show()
                }
                is Resource.Error -> {
                    Toast.makeText(this, "Thanh toán thất bại: ${resource.message}", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                    // Logic loading nếu cần
                }
                else -> Unit
            }

            // 3. CHUYỂN VỀ TAB ĐƠN HÀNG SAU KHI XÁC NHẬN
            selectBottomNavItem(R.id.nav_orders)
        }
    }

    /**
     * Hàm tiện ích để chọn item trên Bottom Nav
     */
    fun selectBottomNavItem(itemId: Int) {
        binding.bottomNavigation.selectedItemId = itemId
    }

    fun updateToolbarTitle(title: String) {
        // (Giữ nguyên)
    }
}