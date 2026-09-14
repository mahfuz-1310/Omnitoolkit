package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_items")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "NAME", "USERNAME", "PASSWORD"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
