package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Lobby
import kotlinx.coroutines.flow.Flow

@Dao
interface LobbyDao {
    @Query("SELECT * FROM lobbies ORDER BY isLive DESC, currentPlayers DESC")
    fun getAllLobbies(): Flow<List<Lobby>>

    @Query("SELECT * FROM lobbies WHERE id = :id")
    suspend fun getLobbyById(id: String): Lobby?

    @Query("SELECT * FROM lobbies WHERE inviteCode = :code OR id = :code LIMIT 1")
    suspend fun findByCode(code: String): Lobby?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLobbies(lobbies: List<Lobby>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLobby(lobby: Lobby)

    @Update
    suspend fun updateLobby(lobby: Lobby)

    @Delete
    suspend fun deleteLobby(lobby: Lobby)

    @Query("UPDATE lobbies SET isBookmarked = NOT isBookmarked WHERE id = :id")
    suspend fun toggleBookmark(id: String)
}
