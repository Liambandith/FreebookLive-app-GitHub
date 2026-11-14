package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "waypoints")
data class Waypoint(
    @PrimaryKey val id: String,
    val name: String,
    val overworldX: Int,
    val overworldY: Int = 64,
    val overworldZ: Int,
    val netherX: Int = overworldX / 8,
    val netherY: Int = 64,
    val netherZ: Int = overworldZ / 8,
    val iconType: String = "Base" // "Base", "Portal", "Fortress", "Bastion", "Stronghold", "Mine"
)
