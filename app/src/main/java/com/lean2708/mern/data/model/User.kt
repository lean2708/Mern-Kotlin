package com.lean2708.mern.data.model

data class User(
    val _id: String,
    val name: String,
    val email: String,
    val role: String,
    val dateOfBirth: String?, // "2004-08-27T00:00:00.000Z"
    val gender: String?,
    val profilePic: String?
)