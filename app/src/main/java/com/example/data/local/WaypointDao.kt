package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Waypoint
import kotlinx.coroutines.flow.Flow

@Dao
interface WaypointDao {
    @Query("SELECT * FROM waypoints ORDER BY name ASC")
    fun getAllWaypoints(): Flow<List<Waypoint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaypoints(waypoints: List<Waypoint>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaypoint(waypoint: Waypoint)

    @Delete
    suspend fun deleteWaypoint(waypoint: Waypoint)
}
