package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "server_access_entries")
data class ServerAccessEntry(
    @PrimaryKey val id: String,
    val serverId: String = "my_hosted_world",
    val playerIgn: String,
    val role: String = "Member", // "Host (Owner)", "Operator (OP)", "Member", "Visitor"
    val isWhitelisted: Boolean = true, // Allowed into your world
    val isPendingWhitelist: Boolean = false, // Requested to join world
    val isBanned: Boolean = false, // Blocked from joining your worlds
    val banReason: String = "",
    val requestNote: String = "School friend / Local Wi-Fi",
    val addedDate: String = "Today"
)

@Entity(tableName = "server_events")
data class ServerEvent(
    @PrimaryKey val id: String,
    val serverId: String = "my_hosted_world",
    val title: String,
    val description: String,
    val dateString: String,
    val eventType: String = "Session", // "BossRaid", "BuildParty", "SurvivalNight", "Session"
    val maxParticipants: Int = 8,
    val registeredCount: Int = 4
)
