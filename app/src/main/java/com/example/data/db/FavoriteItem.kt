package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_items")
data class FavoriteItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "NAME", "USERNAME"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
