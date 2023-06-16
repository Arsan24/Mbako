package com.bangkit.capstone.data.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bangkit.capstone.data.room.ItemsRepository

class ItemsPagerViewModel(itemsRepository: ItemsRepository): ViewModel() {
    val items: LiveData<PagingData<DataItems>> =
        itemsRepository.getItems().cachedIn(viewModelScope)
}