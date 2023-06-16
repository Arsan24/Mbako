package com.bangkit.capstone.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bangkit.capstone.data.model.DataItems

@Dao
interface ItemsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<DataItems>)

    @Query("SELECT * FROM items order by createdAt DESC")
    fun getAllItems(): PagingSource<Int, DataItems>

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}