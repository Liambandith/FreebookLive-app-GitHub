package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PlayerStats
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerStatsDao {
    @Query("SELECT * FROM player_stats LIMIT 1")
    fun getPlayerStats(): Flow<PlayerStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: PlayerStats)

    @Update
    suspend fun update(stats: PlayerStats)
}
