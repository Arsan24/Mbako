package com.bangkit.capstone.data.model

import com.google.gson.annotations.SerializedName

data class DataLogin (
    @field:SerializedName("username")
    val username: String,

    @field:SerializedName("contact")
    val contact: String,

    @field:SerializedName("email")
    val email: String,

    @field:SerializedName("token")
    val token: String
)