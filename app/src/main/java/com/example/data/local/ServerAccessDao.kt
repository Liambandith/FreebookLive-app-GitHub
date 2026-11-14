package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ServerAccessEntry
import com.example.data.model.ServerEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerAccessDao {
    @Query("SELECT * FROM server_access_entries ORDER BY isPendingWhitelist DESC, role = 'Owner' DESC, role = 'Admin (OP)' DESC, playerIgn ASC")
    fun getAllEntries(): Flow<List<ServerAccessEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<ServerAccessEntry>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: ServerAccessEntry)

    @Update
    suspend fun updateEntry(entry: ServerAccessEntry)

    @Delete
    suspend fun deleteEntry(entry: ServerAccessEntry)

    @Query("UPDATE server_access_entries SET isWhitelisted = 1, isPendingWhitelist = 0 WHERE id = :id")
    suspend fun approveWhitelist(id: String)

    @Query("UPDATE server_access_entries SET isBanned = 1, isWhitelisted = 0, banReason = :reason WHERE id = :id")
    suspend fun banPlayer(id: String, reason: String)

    @Query("UPDATE server_access_entries SET isBanned = 0, banReason = '' WHERE id = :id")
    suspend fun unbanPlayer(id: String)

    @Query("UPDATE server_access_entries SET role = :newRole WHERE id = :id")
    suspend fun updateRole(id: String, newRole: String)

    // Events
    @Query("SELECT * FROM server_events ORDER BY id ASC")
    fun getAllEvents(): Flow<List<ServerEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<ServerEvent>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: ServerEvent)

    @Delete
    suspend fun deleteEvent(event: ServerEvent)
}
