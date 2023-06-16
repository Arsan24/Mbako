package com.bangkit.capstone.data.api.remotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.bangkit.capstone.data.api.ApiService
import com.bangkit.capstone.data.database.ItemsDatabase
import com.bangkit.capstone.data.model.DataItems
import com.bangkit.capstone.data.model.ItemsRemoteKeys

@OptIn(ExperimentalPagingApi::class)
class ItemsRemoteMediator(
    private val database: ItemsDatabase,
    private val apiService: ApiService,
    private val token: String
): RemoteMediator<Int, DataItems>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, DataItems>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }
        return try {
            val responseData =
                apiService.getItemList(token, page, state.config.pageSize).listItems
            val endOfPaginationReached = responseData.isEmpty()
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.itemsRemoteKeysDao().deleteRemoteKeys()
                    database.itemsDao().deleteAll()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = responseData.map {
                    ItemsRemoteKeys(item_id = it.item_id, prevKey = prevKey, nextKey = nextKey)
                }
                database.itemsRemoteKeysDao().insertAll(keys)
                database.itemsDao().insertItems(responseData)
            }
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, DataItems>): ItemsRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            database.itemsRemoteKeysDao().getRemoteKeysId(data.item_id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, DataItems>): ItemsRemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            database.itemsRemoteKeysDao().getRemoteKeysId(data.item_id)
        }
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}