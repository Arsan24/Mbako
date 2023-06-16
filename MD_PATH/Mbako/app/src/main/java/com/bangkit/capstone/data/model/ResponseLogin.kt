package com.bangkit.capstone.data.model

import com.google.gson.annotations.SerializedName

data class ResponseLogin (

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("access_token")
    val token: String,

    @field:SerializedName("token_type")
    val token_type: String,

    @field:SerializedName("loginResult")
    val loginResult: DataLogin
)