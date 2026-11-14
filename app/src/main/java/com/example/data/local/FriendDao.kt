package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Friend
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends ORDER BY isInParty DESC, (status LIKE '%Hosting%' OR status LIKE '%Playing%' OR status LIKE '%World%') DESC, status = 'Online' DESC, username ASC")
    fun getAllFriends(): Flow<List<Friend>>

    @Query("SELECT * FROM friends WHERE isInParty = 1")
    fun getPartyMembers(): Flow<List<Friend>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<Friend>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: Friend)

    @Update
    suspend fun updateFriend(friend: Friend)

    @Delete
    suspend fun deleteFriend(friend: Friend)

    @Query("UPDATE friends SET isInParty = :inParty, isPartyReady = 0 WHERE id = :id")
    suspend fun setPartyMember(id: String, inParty: Boolean)

    @Query("UPDATE friends SET isPartyReady = NOT isPartyReady WHERE id = :id")
    suspend fun togglePartyReady(id: String)

    @Query("UPDATE friends SET isInParty = 0, isPartyReady = 0")
    suspend fun disbandParty()
}
