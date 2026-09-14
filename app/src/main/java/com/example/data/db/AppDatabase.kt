package com.example.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [HistoryItem::class, FavoriteItem::class, AppHistoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun appHistoryDao(): AppHistoryDao
}
