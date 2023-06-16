package com.bangkit.capstone.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "items")
data class DataItems (
        @PrimaryKey
        @field:SerializedName("item_id")
        val item_id: String,

        @field:SerializedName("image")
        val image: String,

        @field:SerializedName("createdAt")
        val createdAt: String,

        @field:SerializedName("pname")
        val pname: String,

        @field:SerializedName("price")
        val price: Int,

        @field:SerializedName("quantity")
        val quantity: Int
)