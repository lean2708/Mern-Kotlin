package com.lean2708.mern.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    val _id: String,
    val user: String,
    val phone: String,
    val addressDetail: String,
    val isDefault: Boolean
) : Parcelable