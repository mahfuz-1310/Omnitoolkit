package com.example.data.repository

import com.example.data.db.FavoriteDao
import com.example.data.db.FavoriteItem
import kotlinx.coroutines.flow.Flow

class FavoriteRepository(private val favoriteDao: FavoriteDao) {
    val allFavorites: Flow<List<FavoriteItem>> = favoriteDao.getAllFavorites()

    suspend fun insert(type: String, content: String) {
        favoriteDao.insertFavoriteItem(FavoriteItem(type = type, content = content))
    }

    suspend fun remove(type: String, content: String) = favoriteDao.removeFavoriteItemByContent(content, type)

    suspend fun deleteById(id: Int) = favoriteDao.deleteFavoriteItemById(id)
    
    suspend fun clearAll() = favoriteDao.clearFavorites()
    
    fun isFavorite(type: String, content: String): Flow<Boolean> = favoriteDao.isFavorite(content, type)
}
