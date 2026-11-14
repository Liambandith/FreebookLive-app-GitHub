package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lobbies")
data class Lobby(
    @PrimaryKey val id: String,
    val name: String, // e.g. "Alex's Survival Kingdom" or "Brookhaven Squad RP"
    val gamePlatform: String = "Minecraft", // "Minecraft" or "Roblox"
    val hostUsername: String, // Host Gamertag / Roblox Username
    val serverAddress: String = "Alex_Overlord", // Host Gamertag / LAN / Roblox Place ID
    val port: Int = 19132,
    val robloxPlaceId: String = "", // Roblox Experience Place ID e.g. "4924922222"
    val robloxPrivateLink: String = "", // Optional Roblox private server link code
    val connectionType: String = "LAN / Worlds Tab", // Minecraft: "LAN / Worlds Tab", "Mobile Data (4G/5G)", "Gamertag Online Join" | Roblox: "Roblox Lobby", "Private World Link"
    val edition: String = "Bedrock", // Minecraft: "Bedrock", "Java", "Crossplay" | Roblox: "Roleplay", "Action RPG", "Obby", "Custom World"
    val version: String = "1.21.50",
    val gameMode: String = "Survival", // "Survival", "Creative", "Hardcore", "Adventure", "Roleplay", "Obby", "Raid"
    val difficulty: String = "Normal", // "Peaceful", "Easy", "Normal", "Hard", "Casual"
    val keepInventory: Boolean = true,
    val allowPvp: Boolean = false,
    val useCellularData: Boolean = true, // Play over cellular/mobile data or LAN
    val maxPlayers: Int = 8,
    val currentPlayers: Int = 3,
    val accessType: String = "Friends Only", // "Friends Only", "Invite Code", "Open Lobby"
    val inviteCode: String = "",
    val motd: String = "Open world! Press Join here, then open Minecraft > Play > Worlds.",
    val isLive: Boolean = true,
    val pingMs: Int = 24,
    val tags: String = "Multiplayer,WorldsTab",
    val isBookmarked: Boolean = false,
    val isMyHostedWorld: Boolean = false,
    val createdAtEpoch: Long = System.currentTimeMillis()
)
