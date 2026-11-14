package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FreebookDatabase
import com.example.data.model.Friend
import com.example.data.model.LeaderboardEntry
import com.example.data.model.Lobby
import com.example.data.model.PlayerStats
import com.example.data.model.ServerAccessEntry
import com.example.data.model.ServerEvent
import com.example.data.model.Waypoint
import com.example.data.remote.McStatusResponse
import com.example.data.repository.FreebookRepository
import com.example.overlay.FloatingLobbyService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class NavigationTab(val title: String) {
    LOBBIES("Lobbies"),
    FRIENDS("Friends"),
    ACCESS("Access"),
    STATS("Stats"),
    TOOLS("Guide")
}

data class PingTestState(
    val isLoading: Boolean = false,
    val result: McStatusResponse? = null,
    val error: String? = null,
    val testedAddress: String = ""
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FreebookRepository

    init {
        val database = FreebookDatabase.getDatabase(application)
        repository = FreebookRepository(database)
        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded()
        }
    }

    // Navigation & Selected Lobby
    private val _selectedTab = MutableStateFlow(NavigationTab.LOBBIES)
    val selectedTab: StateFlow<NavigationTab> = _selectedTab.asStateFlow()

    private val _selectedLobby = MutableStateFlow<Lobby?>(null)
    val selectedLobby: StateFlow<Lobby?> = _selectedLobby.asStateFlow()

    // Active Joined Lobby (displays step-by-step join modal/banner after pressing Join)
    private val _activeJoinedLobby = MutableStateFlow<Lobby?>(null)
    val activeJoinedLobby: StateFlow<Lobby?> = _activeJoinedLobby.asStateFlow()

    // Floating Bubble & System Overlay State
    private val _isFloatingBubbleVisible = MutableStateFlow(true)
    val isFloatingBubbleVisible: StateFlow<Boolean> = _isFloatingBubbleVisible.asStateFlow()

    private val _customPinnedLobby = MutableStateFlow<Lobby?>(null)
    val pinnedFloatingLobby: StateFlow<Lobby?> = combine(
        _customPinnedLobby,
        repository.allLobbies
    ) { custom, all ->
        custom ?: all.firstOrNull { it.isMyHostedWorld } ?: all.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isSystemOverlayRunning: StateFlow<Boolean> = FloatingLobbyService.isRunning

    // Filters & Search
    val lobbySearchQuery = MutableStateFlow("")
    val lobbyPlatformFilter = MutableStateFlow("All") // "All", "Minecraft", "Roblox"
    val lobbyConnectionTypeFilter = MutableStateFlow("All") // "All", "Worlds / LAN", "Mobile Data", "Hosted by Me", "Bookmarked"

    // Snackbar / Toast event stream
    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    // Filtered Lobbies (Minecraft & Roblox Worlds)
    val filteredLobbies: StateFlow<List<Lobby>> = combine(
        repository.allLobbies,
        lobbySearchQuery,
        lobbyPlatformFilter,
        lobbyConnectionTypeFilter
    ) { lobbies, query, platform, filter ->
        lobbies.filter { lobby ->
            val matchesPlatform = platform == "All" || lobby.gamePlatform.equals(platform, ignoreCase = true)

            val matchesQuery = query.isBlank() ||
                    lobby.name.contains(query, ignoreCase = true) ||
                    lobby.hostUsername.contains(query, ignoreCase = true) ||
                    lobby.gamePlatform.contains(query, ignoreCase = true) ||
                    lobby.robloxPlaceId.contains(query, ignoreCase = true) ||
                    lobby.gameMode.contains(query, ignoreCase = true) ||
                    lobby.inviteCode.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "Bookmarked" -> lobby.isBookmarked
                "Hosted by Me" -> lobby.isMyHostedWorld
                "All" -> true
                "Worlds / LAN" -> lobby.connectionType.contains("LAN") || lobby.connectionType.contains("Worlds")
                "Mobile Data" -> lobby.connectionType.contains("Mobile Data") || lobby.connectionType.contains("4G") || lobby.useCellularData
                "Gamertags" -> lobby.connectionType.contains("Gamertag") || lobby.connectionType.contains("Friend")
                else -> true
            }
            matchesPlatform && matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Friends & Party
    val allFriends: StateFlow<List<Friend>> = repository.allFriends
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val partyMembers: StateFlow<List<Friend>> = repository.partyMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Player Stats
    val playerStats: StateFlow<PlayerStats> = repository.playerStats
        .combine(MutableStateFlow(Unit)) { stats, _ ->
            stats ?: PlayerStats()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerStats())

    val leaderboards: List<LeaderboardEntry> = repository.getLeaderboardEntries()

    // Server Access & Events
    val serverAccessEntries: StateFlow<List<ServerAccessEntry>> = repository.serverAccessEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val serverEvents: StateFlow<List<ServerEvent>> = repository.serverEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Waypoints
    val allWaypoints: StateFlow<List<Waypoint>> = repository.allWaypoints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Ping / Host Reachability Testing State
    private val _pingTestState = MutableStateFlow(PingTestState())
    val pingTestState: StateFlow<PingTestState> = _pingTestState.asStateFlow()

    fun selectTab(tab: NavigationTab) {
        _selectedTab.value = tab
    }

    fun selectLobby(lobby: Lobby?) {
        _selectedLobby.value = lobby
    }

    fun toggleLobbyBookmark(lobbyId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(lobbyId)
        }
    }

    fun dismissJoinedLobbyModal() {
        _activeJoinedLobby.value = null
    }

    fun toggleFloatingBubble() {
        _isFloatingBubbleVisible.value = !_isFloatingBubbleVisible.value
    }

    fun setFloatingBubbleVisible(visible: Boolean) {
        _isFloatingBubbleVisible.value = visible
    }

    fun pinLobbyToFloating(lobby: Lobby) {
        _customPinnedLobby.value = lobby
        _isFloatingBubbleVisible.value = true
        viewModelScope.launch {
            _uiEvents.emit("Pinned '${lobby.name}' to Floating Bubble!")
        }
    }

    fun startSystemFloatingOverlay(context: Context, lobby: Lobby) {
        _customPinnedLobby.value = lobby
        _isFloatingBubbleVisible.value = true
        FloatingLobbyService.start(context, lobby)
        viewModelScope.launch {
            _uiEvents.emit("Floating bubble enabled over ${lobby.gamePlatform}!")
        }
    }

    fun stopSystemFloatingOverlay(context: Context) {
        FloatingLobbyService.stop(context)
        viewModelScope.launch {
            _uiEvents.emit("Floating game overlay stopped.")
        }
    }

    fun hostMyWorld(
        worldName: String,
        gamePlatform: String,
        hostUsername: String,
        connectionType: String,
        robloxPlaceId: String,
        robloxPrivateLink: String,
        edition: String,
        gameMode: String,
        difficulty: String,
        keepInventory: Boolean,
        allowPvp: Boolean,
        maxPlayers: Int,
        accessType: String,
        motd: String
    ) {
        viewModelScope.launch {
            val codeNum = (1000..9999).random()
            val isRoblox = gamePlatform.equals("Roblox", ignoreCase = true)
            val prefix = if (isRoblox) "RBX" else "FBL"
            val defaultHost = if (isRoblox) playerStats.value.robloxUsername else playerStats.value.ign
            val resolvedHost = hostUsername.trim().ifBlank { defaultHost }
            val resolvedPlaceId = robloxPlaceId.trim().ifBlank { if (isRoblox) "4924922222" else "" }
            val defaultMotd = if (isRoblox) {
                "Hosted Roblox lobby! Press Join to jump straight into my world."
            } else {
                "Multiplayer Game & Visible to LAN Players ON! Press Join -> Minecraft > Play > Worlds."
            }

            val newLobby = Lobby(
                id = "world_" + UUID.randomUUID().toString().take(8),
                name = worldName.trim().ifBlank { if (isRoblox) "$resolvedHost's Roblox World" else "$resolvedHost's Survival World" },
                gamePlatform = if (isRoblox) "Roblox" else "Minecraft",
                hostUsername = resolvedHost,
                serverAddress = if (isRoblox) resolvedPlaceId else resolvedHost,
                port = if (isRoblox) 443 else 19132,
                robloxPlaceId = resolvedPlaceId,
                robloxPrivateLink = robloxPrivateLink.trim(),
                connectionType = connectionType,
                edition = edition,
                version = if (isRoblox) "Live" else "1.21.50",
                gameMode = gameMode,
                difficulty = difficulty,
                keepInventory = keepInventory,
                allowPvp = allowPvp,
                useCellularData = true,
                maxPlayers = maxPlayers,
                currentPlayers = 1,
                accessType = accessType,
                inviteCode = "$prefix-$codeNum",
                motd = motd.trim().ifBlank { defaultMotd },
                isLive = true,
                pingMs = (12..26).random(),
                tags = "$gamePlatform,$edition,$gameMode",
                isBookmarked = true,
                isMyHostedWorld = true
            )
            repository.createLobby(newLobby)
            _customPinnedLobby.value = newLobby
            _isFloatingBubbleVisible.value = true
            _selectedLobby.value = newLobby
            _uiEvents.emit("${newLobby.gamePlatform} world '${newLobby.name}' is live! Code: ${newLobby.inviteCode}")
        }
    }

    fun joinLobbyByCode(code: String) {
        viewModelScope.launch {
            val lobby = repository.findLobbyByCode(code)
            if (lobby != null) {
                _selectedLobby.value = lobby
                _uiEvents.emit("Found ${lobby.gamePlatform} World: ${lobby.name} (Host: ${lobby.hostUsername})!")
            } else {
                _uiEvents.emit("No lobby found with code: $code")
            }
        }
    }

    fun deleteLobby(lobby: Lobby) {
        viewModelScope.launch {
            if (_selectedLobby.value?.id == lobby.id) {
                _selectedLobby.value = null
            }
            if (_activeJoinedLobby.value?.id == lobby.id) {
                _activeJoinedLobby.value = null
            }
            repository.deleteLobby(lobby)
            _uiEvents.emit("World session '${lobby.name}' closed.")
        }
    }

    // Friends & Party
    fun addFriend(username: String, gamePlatform: String, edition: String) {
        viewModelScope.launch {
            repository.addFriend(username, gamePlatform, edition)
            _uiEvents.emit("Added $gamePlatform friend $username ($edition)!")
        }
    }

    fun togglePartyMember(friendId: String, currentlyInParty: Boolean) {
        viewModelScope.launch {
            repository.togglePartyMember(friendId, currentlyInParty)
        }
    }

    fun togglePartyReady(friendId: String) {
        viewModelScope.launch {
            repository.togglePartyReady(friendId)
        }
    }

    fun disbandParty() {
        viewModelScope.launch {
            repository.disbandParty()
            _uiEvents.emit("Squad disbanded.")
        }
    }

    fun removeFriend(friend: Friend) {
        viewModelScope.launch {
            repository.removeFriend(friend)
            _uiEvents.emit("Removed ${friend.username} from friends.")
        }
    }

    // In-World Permissions & Whitelist
    fun approveWhitelist(entryId: String, playerIgn: String) {
        viewModelScope.launch {
            repository.approveWhitelist(entryId)
            _uiEvents.emit("Allowed $playerIgn into your hosted world!")
        }
    }

    fun banPlayer(entryId: String, playerIgn: String, reason: String) {
        viewModelScope.launch {
            repository.banPlayer(entryId, reason.ifBlank { "Violation of world rules" })
            _uiEvents.emit("Blocked $playerIgn from your worlds.")
        }
    }

    fun unbanPlayer(entryId: String, playerIgn: String) {
        viewModelScope.launch {
            repository.unbanPlayer(entryId)
            _uiEvents.emit("Unblocked $playerIgn.")
        }
    }

    fun updatePlayerRole(entryId: String, playerIgn: String, newRole: String) {
        viewModelScope.launch {
            repository.updatePlayerRole(entryId, newRole)
            _uiEvents.emit("Updated $playerIgn role to $newRole.")
        }
    }

    fun addAccessEntry(ign: String, role: String, note: String) {
        viewModelScope.launch {
            repository.addWhitelistRequest(ign, role, note)
            _uiEvents.emit("Added $ign ($role) to world access list.")
        }
    }

    fun deleteAccessEntry(entry: ServerAccessEntry) {
        viewModelScope.launch {
            repository.deleteAccessEntry(entry)
            _uiEvents.emit("Removed ${entry.playerIgn} from permissions.")
        }
    }

    fun addEvent(title: String, desc: String, date: String, type: String) {
        viewModelScope.launch {
            val event = ServerEvent(
                id = "ev_" + UUID.randomUUID().toString().take(6),
                title = title.ifBlank { "Multiplayer Survival Night" },
                description = desc.ifBlank { "Join my hosted world session for fun adventures!" },
                dateString = date.ifBlank { "This Weekend" },
                eventType = type,
                maxParticipants = 8,
                registeredCount = 1
            )
            repository.addEvent(event)
            _uiEvents.emit("Session event '${event.title}' scheduled!")
        }
    }

    fun deleteEvent(event: ServerEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            _uiEvents.emit("Session event '${event.title}' removed.")
        }
    }

    // Stats & Session
    fun updatePlayerProfile(newIgn: String, newRobloxUsername: String, newTitle: String, newGuild: String) {
        viewModelScope.launch {
            val current = playerStats.value
            val updated = current.copy(
                ign = newIgn.trim().ifBlank { current.ign },
                robloxUsername = newRobloxUsername.trim().ifBlank { current.robloxUsername },
                title = newTitle.trim().ifBlank { current.title },
                guild = newGuild.trim().ifBlank { current.guild }
            )
            repository.updateStats(updated)
            _uiEvents.emit("Profile updated! MC: ${updated.ign} • Roblox: @${updated.robloxUsername}")
        }
    }

    fun logPlaySession(mined: Long, slain: Int, diamonds: Int, hours: Int, kills: Int, deaths: Int) {
        viewModelScope.launch {
            repository.logSession(mined, slain, diamonds, hours, kills, deaths)
            _uiEvents.emit("Session logged! +$diamonds diamonds, +$mined blocks with friends!")
        }
    }

    // Launch helpers for Minecraft & Roblox Worlds
    fun joinLobbyAndShowGuide(context: Context, lobby: Lobby) {
        _customPinnedLobby.value = lobby
        _isFloatingBubbleVisible.value = true
        _activeJoinedLobby.value = lobby
    }

    fun launchGameApp(context: Context, lobby: Lobby) {
        if (lobby.gamePlatform.equals("Roblox", ignoreCase = true)) {
            launchRoblox(context, lobby)
        } else {
            launchMinecraft(context, lobby)
        }
    }

    fun launchMinecraft(context: Context, lobby: Lobby) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.mojang.minecraftpe")
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            } else {
                val uri = Uri.parse("minecraft://")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "Open Minecraft > Play > Worlds and tap '${lobby.name}'!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun launchRoblox(context: Context, lobby: Lobby) {
        val placeId = lobby.robloxPlaceId.ifBlank { "4924922222" }
        try {
            val uri = Uri.parse("roblox://experiences/start?placeId=$placeId")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.roblox.client")
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                } else {
                    val webUri = Uri.parse("https://www.roblox.com/games/$placeId")
                    val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(webIntent)
                }
            } catch (ex: Exception) {
                Toast.makeText(
                    context,
                    "Open Roblox and join @${lobby.hostUsername} (Place ID: $placeId).",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun shareLobby(context: Context, lobby: Lobby) {
        val extraInfo = if (lobby.gamePlatform == "Roblox") {
            "Place ID: ${lobby.robloxPlaceId}\nLink: https://www.roblox.com/games/${lobby.robloxPlaceId}"
        } else {
            "How to Join: Press Join in FreebookLive, then open Minecraft > Play > Worlds and tap '${lobby.name}'"
        }
        val shareText = "Join my ${lobby.gamePlatform} world on FreebookLive!\n" +
                "World: ${lobby.name}\n" +
                "Host: ${lobby.hostUsername}\n" +
                "Invite Code: ${lobby.inviteCode}\n" +
                "$extraInfo\n" +
                "Deep Link: freebooklive://lobby/${lobby.inviteCode}"
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share ${lobby.gamePlatform} World")
        shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(shareIntent)
    }

    // Waypoints
    fun addWaypoint(name: String, overworldX: Int, overworldY: Int, overworldZ: Int, iconType: String) {
        viewModelScope.launch {
            val wp = Waypoint(
                id = "wp_" + UUID.randomUUID().toString().take(6),
                name = name.ifBlank { "Waypoint" },
                overworldX = overworldX,
                overworldY = overworldY,
                overworldZ = overworldZ,
                netherX = overworldX / 8,
                netherY = overworldY,
                netherZ = overworldZ / 8,
                iconType = iconType
            )
            repository.addWaypoint(wp)
            _uiEvents.emit("Saved waypoint '${wp.name}'!")
        }
    }

    fun deleteWaypoint(waypoint: Waypoint) {
        viewModelScope.launch {
            repository.deleteWaypoint(waypoint)
            _uiEvents.emit("Waypoint '${waypoint.name}' deleted.")
        }
    }

    // LAN / Host Reachability Testing
    fun testHostReachability(address: String, port: Int = 19132) {
        viewModelScope.launch {
            _pingTestState.value = PingTestState(isLoading = true, testedAddress = address)
            try {
                val res = repository.testHostReachability(address, port)
                _pingTestState.value = PingTestState(
                    isLoading = false,
                    result = res,
                    testedAddress = address
                )
            } catch (e: Exception) {
                _pingTestState.value = PingTestState(
                    isLoading = false,
                    error = e.message ?: "Host unreachable",
                    testedAddress = address
                )
            }
        }
    }
}
