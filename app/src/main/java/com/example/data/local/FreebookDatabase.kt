package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Friend
import com.example.data.model.Lobby
import com.example.data.model.PlayerStats
import com.example.data.model.ServerAccessEntry
import com.example.data.model.ServerEvent
import com.example.data.model.Waypoint

@Database(
    entities = [
        Lobby::class,
        Friend::class,
        PlayerStats::class,
        ServerAccessEntry::class,
        ServerEvent::class,
        Waypoint::class
    ],
    version = 4,
    exportSchema = false
)
abstract class FreebookDatabase : RoomDatabase() {
    abstract fun lobbyDao(): LobbyDao
    abstract fun friendDao(): FriendDao
    abstract fun playerStatsDao(): PlayerStatsDao
    abstract fun serverAccessDao(): ServerAccessDao
    abstract fun waypointDao(): WaypointDao

    companion object {
        @Volatile
        private var INSTANCE: FreebookDatabase? = null

        fun getDatabase(context: Context): FreebookDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FreebookDatabase::class.java,
                    "freebooklive_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
