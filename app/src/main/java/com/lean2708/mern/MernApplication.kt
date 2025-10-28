package com.lean2708.mern

import android.app.Application
import com.lean2708.mern.data.network.RetrofitInstance

class MernApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Khởi tạo RetrofitInstance một lần duy nhất tại đây
        RetrofitInstance.initialize(this)
    }
}