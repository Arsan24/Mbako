package com.bangkit.capstone.data.model

import com.google.gson.annotations.SerializedName

data class ResponseForgotPass (
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,
)