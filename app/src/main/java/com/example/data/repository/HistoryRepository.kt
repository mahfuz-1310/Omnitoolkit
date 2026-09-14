package com.example.data.repository

import com.example.data.db.HistoryDao
import com.example.data.db.HistoryItem
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val historyDao: HistoryDao) {
    val allHistory: Flow<List<HistoryItem>> = historyDao.getAllHistory()

    suspend fun insert(type: String, content: String) {
        historyDao.insertHistoryItem(HistoryItem(type = type, content = content))
    }

    suspend fun deleteById(id: Int) = historyDao.deleteHistoryItemById(id)
    
    suspend fun clearAll() = historyDao.clearHistory()
}
