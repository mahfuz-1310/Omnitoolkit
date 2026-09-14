package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_history")
data class AppHistoryEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val actionType: String, // "Installed", "Uninstalled", "Reinstalled"
    val actionTimestamp: String, // DD/MM/YYYY - HH:mm:ss
    val installTimeMillis: Long,
    val uninstallTimeMillis: Long = 0L,
    val reinstallCount: Int = 0
)
