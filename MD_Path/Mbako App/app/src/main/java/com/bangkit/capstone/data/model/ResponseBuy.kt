package com.bangkit.capstone.data.model

import com.google.gson.annotations.SerializedName

data class ResponseBuy(
    @SerializedName("error")
    val error: Boolean,

    @SerializedName("message")
    val message: String,
)
