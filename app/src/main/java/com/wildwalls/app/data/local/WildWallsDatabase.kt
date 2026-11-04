package com.wildwalls.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        DownloadEntity::class,
        RecentViewEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WildWallsDatabase : RoomDatabase() {

    abstract fun dao(): WildWallsDao

    companion object {
        @Volatile
        private var instance: WildWallsDatabase? = null

        fun get(context: Context): WildWallsDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                WildWallsDatabase::class.java,
                "wildwalls.db"
            ).fallbackToDestructiveMigration().build().also { instance = it }
        }
    }
}
