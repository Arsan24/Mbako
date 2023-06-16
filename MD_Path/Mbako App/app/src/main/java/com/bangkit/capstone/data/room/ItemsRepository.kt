package com.bangkit.capstone.data.room

import androidx.lifecycle.LiveData
import androidx.paging.*
import com.bangkit.capstone.data.api.ApiService
import com.bangkit.capstone.data.api.remotemediator.ItemsRemoteMediator
import com.bangkit.capstone.data.database.ItemsDatabase
import com.bangkit.capstone.data.model.DataItems

class ItemsRepository (
    private val itemsDatabase: ItemsDatabase,
    private val apiService: ApiService,
    private val token:String
) {
    fun getItems(): LiveData<PagingData<DataItems>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(pageSize = 5),
            remoteMediator = ItemsRemoteMediator(itemsDatabase, apiService, token),
            pagingSourceFactory = { itemsDatabase.itemsDao().getAllItems() }
        ).liveData
    }
}
