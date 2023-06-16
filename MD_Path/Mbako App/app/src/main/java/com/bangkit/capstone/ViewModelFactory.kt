package com.bangkit.capstone

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bangkit.capstone.data.api.ApiService
import com.bangkit.capstone.data.database.ItemsDatabase
import com.bangkit.capstone.data.model.ItemsPagerViewModel
import com.bangkit.capstone.data.room.ItemsRepository

class ViewModelFactory(val context: Context, private val apiService: ApiService, val token:String) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemsPagerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val database = ItemsDatabase.getDatabase(context)
            return ItemsPagerViewModel(
                ItemsRepository(
                    database,
                    apiService, token
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}