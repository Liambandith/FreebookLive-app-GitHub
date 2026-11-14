package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey val id: String,
    val username: String,
    val gamePlatform: String = "Minecraft", // "Minecraft" or "Roblox"
    val edition: String = "Bedrock", // "Bedrock", "Java", "Crossplay", or Roblox genre/tag
    val status: String = "Online", // "Hosting World", "Playing", "Online", "Offline"
    val currentServer: String = "Alex's Survival Kingdom", // Current world or Roblox experience name
    val currentLobbyId: String = "",
    val isFavorite: Boolean = false,
    val isInParty: Boolean = false,
    val isPartyLeader: Boolean = false,
    val isPartyReady: Boolean = false,
    val lastSeenEpoch: Long = System.currentTimeMillis()
) {
    fun getAvatarUrl(): String {
        return "https://mc-heads.net/avatar/$username/128"
    }
}
