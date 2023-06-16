package com.bangkit.capstone.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bangkit.capstone.data.model.ItemsRemoteKeys
@Dao
interface ItemsRemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<ItemsRemoteKeys>)

    @Query("SELECT * FROM items_remote_keys WHERE item_id = :item_id")
    suspend fun getRemoteKeysId(item_id: String): ItemsRemoteKeys?

    @Query("DELETE FROM items_remote_keys")
    suspend fun deleteRemoteKeys()
}