package com.bangkit.capstone.data.model

import com.google.gson.annotations.SerializedName

data class DataItemList (
    @field:SerializedName("listItems")
    val listItems: List<DataItems>,

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)