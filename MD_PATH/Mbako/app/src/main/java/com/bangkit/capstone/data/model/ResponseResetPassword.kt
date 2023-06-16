package com.bangkit.capstone.data.model

import com.google.gson.annotations.SerializedName

data class ResponseResetPassword (
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,
)