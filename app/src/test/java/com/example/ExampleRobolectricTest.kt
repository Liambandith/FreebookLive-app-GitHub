package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.FreebookDatabase
import com.example.data.model.Lobby
import com.example.data.repository.FreebookRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FreebookLive", appName)
    }

    @Test
    fun `seed data populates both Minecraft and Roblox lobbies and allows hosting`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, FreebookDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = FreebookRepository(db)
            repo.initializeSeedDataIfNeeded()

            val lobbies = repo.allLobbies.first()
            assertTrue(lobbies.any { it.gamePlatform == "Minecraft" })
            assertTrue(lobbies.any { it.gamePlatform == "Roblox" })

            val friends = repo.allFriends.first()
            assertTrue(friends.any { it.gamePlatform == "Minecraft" })
            assertTrue(friends.any { it.gamePlatform == "Roblox" })

            val customLobby = Lobby(
                id = "test_host_world",
                name = "My Test World",
                gamePlatform = "Minecraft",
                hostUsername = "Crafter_Hero",
                inviteCode = "FBL-9999",
                isMyHostedWorld = true
            )
            repo.createLobby(customLobby)
            val found = repo.findLobbyByCode("FBL-9999")
            assertEquals("My Test World", found?.name)
        } finally {
            db.close()
        }
    }
}
