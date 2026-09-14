package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppHistoryDao {
    @Query("SELECT * FROM app_history ORDER BY uninstallTimeMillis DESC, installTimeMillis DESC")
    fun getAllHistory(): Flow<List<AppHistoryEntity>>

    @Query("SELECT * FROM app_history WHERE packageName = :pkg")
    suspend fun getByPackage(pkg: String): AppHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: AppHistoryEntity)

    @Query("DELETE FROM app_history WHERE packageName = :pkg")
    suspend fun deleteByPackage(pkg: String)
}
