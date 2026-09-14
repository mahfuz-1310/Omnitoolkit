package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_items ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteItem(item: FavoriteItem)

    @Query("DELETE FROM favorite_items WHERE id = :id")
    suspend fun deleteFavoriteItemById(id: Int)
    
    @Query("DELETE FROM favorite_items WHERE content = :content AND type = :type")
    suspend fun removeFavoriteItemByContent(content: String, type: String)
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_items WHERE content = :content AND type = :type)")
    fun isFavorite(content: String, type: String): Flow<Boolean>

    @Query("DELETE FROM favorite_items")
    suspend fun clearFavorites()
}
