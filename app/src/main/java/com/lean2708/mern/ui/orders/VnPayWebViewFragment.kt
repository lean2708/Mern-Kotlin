package com.lean2708.mern.ui.orders

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.lean2708.mern.R
import com.lean2708.mern.ui.home.activity.MainActivity
import java.util.stream.Collectors

class VnPayWebViewFragment : Fragment() {
    private var vnpayUrl: String? = null

    companion object {
        const val ARG_VNPAY_URL = "vnpay_url"
        fun newInstance(url: String): VnPayWebViewFragment {
            val fragment = VnPayWebViewFragment()
            fragment.arguments = Bundle().apply { putString(ARG_VNPAY_URL, url) }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vnpayUrl = arguments?.getString(ARG_VNPAY_URL)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Cần Context để khởi tạo WebView
        val webView = WebView(requireContext())
        webView.settings.javaScriptEnabled = true

        // Ghi đè WebViewClient để bắt URL trả về
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {

                // --- LOGIC BẮT CALLBACK TỪ LOCALHOST:3000 ---
                if (url.contains("http://localhost:3000")) {
                    handleVnPayCallback(url)
                    return true // Ngăn WebView load tiếp
                }
                // ------------------------------------------

                return false // Load bình thường
            }
        }

        vnpayUrl?.let { webView.loadUrl(it) }
        return webView
    }

    private fun handleVnPayCallback(url: String) {
        val uri = Uri.parse(url)

        // Trích xuất tất cả query parameters
        // Sử dụng associateWith để tạo Map (cần cho API 4)
        val params: Map<String, String> = uri.queryParameterNames.associateWith { uri.getQueryParameter(it) ?: "" }

        // Gọi hàm xử lý trong MainActivity để xác nhận thanh toán (API 4)
        (activity as? MainActivity)?.handleVnPayReturn(params)

        // Đóng WebView và quay lại Fragment trước đó
        parentFragmentManager.popBackStack()
    }
}