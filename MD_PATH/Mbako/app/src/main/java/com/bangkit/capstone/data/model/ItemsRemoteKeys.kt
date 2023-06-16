package com.bangkit.capstone.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items_remote_keys")
data class ItemsRemoteKeys (
    @PrimaryKey val item_id: String,
    val prevKey: Int?,
    val nextKey: Int?,
)