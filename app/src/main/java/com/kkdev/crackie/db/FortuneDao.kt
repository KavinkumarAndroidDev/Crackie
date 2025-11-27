package com.kkdev.crackie.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FortuneDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fortunes: List<Fortune>)

    @Query("SELECT * FROM fortunes WHERE wasSeen = 0 ORDER BY RANDOM() LIMIT 1")
    suspend fun getUnseenFortune(): Fortune?

    @Update
    suspend fun updateFortune(fortune: Fortune)

    @Query("SELECT COUNT(id) FROM fortunes")
    suspend fun getFortuneCount(): Int

    @Query("SELECT * FROM fortunes WHERE isFavorite = 1 ORDER BY dateAdded ASC")
    fun getFavoriteFortunesByDateAsc(): Flow<List<Fortune>>

    @Query("SELECT * FROM fortunes WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteFortunesByDateDesc(): Flow<List<Fortune>>

    @Query("SELECT * FROM fortunes WHERE isFavorite = 1 ORDER BY rarity ASC")
    fun getFavoriteFortunesByRarityAsc(): Flow<List<Fortune>>

    @Query("SELECT * FROM fortunes WHERE isFavorite = 1 ORDER BY rarity DESC")
    fun getFavoriteFortunesByRarityDesc(): Flow<List<Fortune>>

    @Query("UPDATE fortunes SET wasSeen = 0")
    suspend fun resetSeenFortunes()

    @Query("SELECT COUNT(id) FROM fortunes WHERE wasSeen = 1")
    suspend fun getSeenFortunesCount(): Int
}
