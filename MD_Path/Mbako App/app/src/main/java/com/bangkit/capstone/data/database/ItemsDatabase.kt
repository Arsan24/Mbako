package com.bangkit.capstone.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bangkit.capstone.data.model.DataItems
import com.bangkit.capstone.data.model.ItemsRemoteKeys

@Database(
    entities = [DataItems::class, ItemsRemoteKeys::class],
    version = 5,
    exportSchema = false
)
abstract class ItemsDatabase : RoomDatabase() {
    abstract fun itemsDao(): ItemsDao
    abstract fun itemsRemoteKeysDao(): ItemsRemoteKeysDao

    companion object {
        @Volatile
        private var INSTANCE: ItemsDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): ItemsDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ItemsDatabase::class.java, "items_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}