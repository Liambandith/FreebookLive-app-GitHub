package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_stats")
data class PlayerStats(
    @PrimaryKey val ign: String = "Crafter_Hero",
    val robloxUsername: String = "BloxHero_99",
    val title: String = "World Host & Builder",
    val edition: String = "Minecraft & Roblox",
    val guild: String = "Survival Squad",
    val playtimeHours: Int = 428,
    val worldsHosted: Int = 18,
    val friendSessionsJoined: Int = 64,
    val blocksMined: Long = 349120,
    val mobsSlain: Int = 5420,
    val diamondsMined: Int = 1864,
    val netheriteCrafted: Int = 42,
    val pvpKills: Int = 312,
    val pvpDeaths: Int = 89,
    val achievementsUnlocked: Int = 94,
    val maxAchievements: Int = 110,
    val favoriteServer: String = "Alex's Survival Empire", // Favorite friend world session
    val lastUpdatedEpoch: Long = System.currentTimeMillis()
) {
    val kdRatio: Float
        get() = if (pvpDeaths == 0) pvpKills.toFloat() else (pvpKills.toFloat() / pvpDeaths)

    fun getAvatarUrl(): String = "https://mc-heads.net/avatar/$ign/128"
}

data class LeaderboardEntry(
    val rank: Int,
    val ign: String,
    val metricValue: String,
    val metricLabel: String,
    val edition: String
)
