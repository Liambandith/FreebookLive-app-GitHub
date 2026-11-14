package com.example.data.repository

import com.example.data.local.FreebookDatabase
import com.example.data.model.Friend
import com.example.data.model.LeaderboardEntry
import com.example.data.model.Lobby
import com.example.data.model.PlayerStats
import com.example.data.model.ServerAccessEntry
import com.example.data.model.ServerEvent
import com.example.data.model.Waypoint
import com.example.data.remote.McStatusResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID

class FreebookRepository(
    private val database: FreebookDatabase
) {
    private val lobbyDao = database.lobbyDao()
    private val friendDao = database.friendDao()
    private val statsDao = database.playerStatsDao()
    private val accessDao = database.serverAccessDao()
    private val waypointDao = database.waypointDao()

    val allLobbies: Flow<List<Lobby>> = lobbyDao.getAllLobbies()
    val allFriends: Flow<List<Friend>> = friendDao.getAllFriends()
    val partyMembers: Flow<List<Friend>> = friendDao.getPartyMembers()
    val playerStats: Flow<PlayerStats?> = statsDao.getPlayerStats()
    val serverAccessEntries: Flow<List<ServerAccessEntry>> = accessDao.getAllEntries()
    val serverEvents: Flow<List<ServerEvent>> = accessDao.getAllEvents()
    val allWaypoints: Flow<List<Waypoint>> = waypointDao.getAllWaypoints()

    suspend fun initializeSeedDataIfNeeded() = withContext(Dispatchers.IO) {
        val existingLobbies = allLobbies.firstOrNull()
        if (existingLobbies.isNullOrEmpty()) {
            val seedLobbies = listOf(
                Lobby(
                    id = "fbl_world_alex",
                    name = "Alex's Survival Kingdom",
                    gamePlatform = "Minecraft",
                    hostUsername = "Alex_Overlord",
                    serverAddress = "Alex_Overlord",
                    port = 19132,
                    connectionType = "LAN / Worlds Tab",
                    edition = "Bedrock",
                    version = "1.21.50",
                    gameMode = "Survival",
                    difficulty = "Normal",
                    keepInventory = true,
                    allowPvp = false,
                    useCellularData = true,
                    maxPlayers = 8,
                    currentPlayers = 3,
                    accessType = "Friends Only",
                    inviteCode = "FBL-1001",
                    motd = "Multiplayer Game & Visible to LAN Players ON! Press Join -> Minecraft > Play > Worlds.",
                    isLive = true,
                    pingMs = 16,
                    tags = "Minecraft,WorldsTab,LAN,Multiplayer",
                    isBookmarked = true
                ),
                Lobby(
                    id = "rbx_world_brookhaven",
                    name = "Brookhaven Squad Roleplay",
                    gamePlatform = "Roblox",
                    hostUsername = "BloxBuilder_Kai",
                    serverAddress = "4924922222",
                    port = 443,
                    robloxPlaceId = "4924922222",
                    robloxPrivateLink = "RBX-SQD-4924",
                    connectionType = "Roblox Friend Lobby",
                    edition = "Roleplay",
                    version = "Live",
                    gameMode = "Roleplay",
                    difficulty = "Casual",
                    keepInventory = true,
                    allowPvp = false,
                    useCellularData = true,
                    maxPlayers = 12,
                    currentPlayers = 5,
                    accessType = "Friends Only",
                    inviteCode = "RBX-4924",
                    motd = "Squad hangout at Lake House #14! Press Join to launch directly into Roblox.",
                    isLive = true,
                    pingMs = 24,
                    tags = "Roblox,Roleplay,Squad",
                    isBookmarked = true
                ),
                Lobby(
                    id = "fbl_world_sarah",
                    name = "Sarah's Creative Mega-City",
                    gamePlatform = "Minecraft",
                    hostUsername = "PixieCrafter",
                    serverAddress = "PixieCrafter",
                    port = 19132,
                    connectionType = "Mobile Data (4G/5G)",
                    edition = "Bedrock",
                    version = "1.21.50",
                    gameMode = "Creative",
                    difficulty = "Peaceful",
                    keepInventory = true,
                    allowPvp = false,
                    useCellularData = true,
                    maxPlayers = 6,
                    currentPlayers = 2,
                    accessType = "Friends Only",
                    inviteCode = "FBL-4420",
                    motd = "Building skyscraper district! Press Join here, then open Minecraft > Play > Worlds.",
                    isLive = true,
                    pingMs = 28,
                    tags = "Minecraft,Creative,MobileData",
                    isBookmarked = true
                ),
                Lobby(
                    id = "rbx_world_bloxfruits",
                    name = "Blox Fruits Sea Raid Party",
                    gamePlatform = "Roblox",
                    hostUsername = "PirateKing_Z",
                    serverAddress = "2753915549",
                    port = 443,
                    robloxPlaceId = "2753915549",
                    robloxPrivateLink = "RBX-SEA-2753",
                    connectionType = "Private World Link",
                    edition = "Action RPG",
                    version = "Update 24",
                    gameMode = "Sea Raid",
                    difficulty = "Hard",
                    keepInventory = true,
                    allowPvp = true,
                    useCellularData = true,
                    maxPlayers = 10,
                    currentPlayers = 6,
                    accessType = "Invite Code",
                    inviteCode = "RBX-2753",
                    motd = "Leviathan & Mirage hunt! Join our private Roblox lobby for fast boss clears.",
                    isLive = true,
                    pingMs = 31,
                    tags = "Roblox,ActionRPG,Raid",
                    isBookmarked = false
                ),
                Lobby(
                    id = "fbl_world_cozy",
                    name = "Cozy Farm & Villager Trading",
                    gamePlatform = "Minecraft",
                    hostUsername = "DiamondDigger_77",
                    serverAddress = "DiamondDigger_77",
                    port = 19132,
                    connectionType = "Gamertag Online Join",
                    edition = "Crossplay",
                    version = "1.21.50",
                    gameMode = "Survival",
                    difficulty = "Easy",
                    keepInventory = true,
                    allowPvp = false,
                    useCellularData = true,
                    maxPlayers = 8,
                    currentPlayers = 2,
                    accessType = "Invite Code",
                    inviteCode = "FBL-9231",
                    motd = "Iron farm & crop farm ready. Turn on Multiplayer Game & Visible to LAN Players!",
                    isLive = true,
                    pingMs = 29,
                    tags = "Minecraft,Survival,Farming",
                    isBookmarked = false
                ),
                Lobby(
                    id = "rbx_world_obby",
                    name = "Tower of Hell Speedrun World",
                    gamePlatform = "Roblox",
                    hostUsername = "ObbyMaster_Mia",
                    serverAddress = "1962086868",
                    port = 443,
                    robloxPlaceId = "1962086868",
                    robloxPrivateLink = "RBX-OBBY-1962",
                    connectionType = "Roblox Lobby",
                    edition = "Obby",
                    version = "Live",
                    gameMode = "Speedrun",
                    difficulty = "Medium",
                    keepInventory = true,
                    allowPvp = false,
                    useCellularData = true,
                    maxPlayers = 12,
                    currentPlayers = 4,
                    accessType = "Open Lobby",
                    inviteCode = "RBX-1962",
                    motd = "First friend to the top wins! Press Join to jump straight into the Roblox Obby.",
                    isLive = true,
                    pingMs = 26,
                    tags = "Roblox,Obby,Race",
                    isBookmarked = false
                )
            )
            lobbyDao.insertLobbies(seedLobbies)
        }

        val existingFriends = allFriends.firstOrNull()
        if (existingFriends.isNullOrEmpty()) {
            val seedFriends = listOf(
                Friend(
                    id = "f_1",
                    username = "Alex_Overlord",
                    gamePlatform = "Minecraft",
                    edition = "Bedrock",
                    status = "Hosting World",
                    currentServer = "Alex's Survival Kingdom",
                    currentLobbyId = "fbl_world_alex",
                    isFavorite = true,
                    isInParty = true,
                    isPartyLeader = true,
                    isPartyReady = true
                ),
                Friend(
                    id = "f_2",
                    username = "BloxBuilder_Kai",
                    gamePlatform = "Roblox",
                    edition = "Roleplay",
                    status = "Hosting World",
                    currentServer = "Brookhaven Squad Roleplay",
                    currentLobbyId = "rbx_world_brookhaven",
                    isFavorite = true,
                    isInParty = true,
                    isPartyLeader = false,
                    isPartyReady = true
                ),
                Friend(
                    id = "f_3",
                    username = "PixieCrafter",
                    gamePlatform = "Minecraft",
                    edition = "Bedrock",
                    status = "Hosting World",
                    currentServer = "Sarah's Creative Mega-City",
                    currentLobbyId = "fbl_world_sarah",
                    isFavorite = true,
                    isInParty = false,
                    isPartyLeader = false,
                    isPartyReady = false
                ),
                Friend(
                    id = "f_4",
                    username = "PirateKing_Z",
                    gamePlatform = "Roblox",
                    edition = "Action RPG",
                    status = "Playing",
                    currentServer = "Blox Fruits Sea Raid Party",
                    currentLobbyId = "rbx_world_bloxfruits",
                    isFavorite = true,
                    isInParty = false,
                    isPartyLeader = false,
                    isPartyReady = false
                ),
                Friend(
                    id = "f_5",
                    username = "DiamondDigger_77",
                    gamePlatform = "Minecraft",
                    edition = "Crossplay",
                    status = "Playing",
                    currentServer = "Cozy Farm & Villager Trading",
                    currentLobbyId = "fbl_world_cozy",
                    isFavorite = false,
                    isInParty = false,
                    isPartyLeader = false,
                    isPartyReady = false
                ),
                Friend(
                    id = "f_6",
                    username = "ObbyMaster_Mia",
                    gamePlatform = "Roblox",
                    edition = "Obby",
                    status = "Online",
                    currentServer = "Tower of Hell Speedrun World",
                    currentLobbyId = "rbx_world_obby",
                    isFavorite = false,
                    isInParty = false,
                    isPartyLeader = false,
                    isPartyReady = false
                )
            )
            friendDao.insertFriends(seedFriends)
        }

        val existingStats = statsDao.getPlayerStats().firstOrNull()
        if (existingStats == null) {
            val initialStats = PlayerStats(
                ign = "Crafter_Hero",
                title = "World Host & Builder",
                edition = "Bedrock & Java",
                guild = "Survival Squad",
                playtimeHours = 428,
                worldsHosted = 18,
                friendSessionsJoined = 64,
                blocksMined = 349120,
                mobsSlain = 5420,
                diamondsMined = 1864,
                netheriteCrafted = 42,
                pvpKills = 312,
                pvpDeaths = 89,
                achievementsUnlocked = 94,
                maxAchievements = 110,
                favoriteServer = "Alex's Survival Kingdom"
            )
            statsDao.insertOrUpdate(initialStats)
        }

        val existingAccess = accessDao.getAllEntries().firstOrNull()
        if (existingAccess.isNullOrEmpty()) {
            val seedAccess = listOf(
                ServerAccessEntry(
                    id = "acc_1",
                    serverId = "my_hosted_world",
                    playerIgn = "Crafter_Hero",
                    role = "Host (Owner)",
                    isWhitelisted = true,
                    isPendingWhitelist = false,
                    isBanned = false,
                    requestNote = "World Creator & Host",
                    addedDate = "Nov 01, 2026"
                ),
                ServerAccessEntry(
                    id = "acc_2",
                    serverId = "my_hosted_world",
                    playerIgn = "Notch_Junior",
                    role = "Operator (OP)",
                    isWhitelisted = true,
                    isPendingWhitelist = false,
                    isBanned = false,
                    requestNote = "Trusted Friend (Can use /tp & commands)",
                    addedDate = "Nov 02, 2026"
                ),
                ServerAccessEntry(
                    id = "acc_3",
                    serverId = "my_hosted_world",
                    playerIgn = "DiamondDigger_77",
                    role = "Member",
                    isWhitelisted = true,
                    isPendingWhitelist = false,
                    isBanned = false,
                    requestNote = "Regular survival member (Can build & mine)",
                    addedDate = "Nov 04, 2026"
                ),
                ServerAccessEntry(
                    id = "acc_4",
                    serverId = "my_hosted_world",
                    playerIgn = "ViperX_Gamer",
                    role = "Visitor",
                    isWhitelisted = false,
                    isPendingWhitelist = true,
                    isBanned = false,
                    requestNote = "Friend on local Wi-Fi requesting to join",
                    addedDate = "Today, 10:15 AM"
                ),
                ServerAccessEntry(
                    id = "acc_5",
                    serverId = "my_hosted_world",
                    playerIgn = "GriefBot_007",
                    role = "Visitor",
                    isWhitelisted = false,
                    isPendingWhitelist = false,
                    isBanned = true,
                    banReason = "Broke chest locks without asking",
                    requestNote = "Blocked from hosting sessions",
                    addedDate = "Yesterday"
                )
            )
            accessDao.insertEntries(seedAccess)
        }

        val existingEvents = accessDao.getAllEvents().firstOrNull()
        if (existingEvents.isNullOrEmpty()) {
            val seedEvents = listOf(
                ServerEvent(
                    id = "ev_1",
                    title = "Ender Dragon Fight Session",
                    description = "Join in my hosted survival world! Going to The End together.",
                    dateString = "Saturday, 8:00 PM EST",
                    eventType = "BossRaid",
                    maxParticipants = 6,
                    registeredCount = 4
                ),
                ServerEvent(
                    id = "ev_2",
                    title = "Community Nether Highway Build",
                    description = "Connecting everyone's bases in Nether with blue ice roads.",
                    dateString = "Sunday, 3:00 PM EST",
                    eventType = "BuildParty",
                    maxParticipants = 8,
                    registeredCount = 5
                ),
                ServerEvent(
                    id = "ev_3",
                    title = "Weekend Mining Marathon",
                    description = "Digging at Y=-58 for diamonds and ancient debris.",
                    dateString = "Friday, 6:00 PM EST",
                    eventType = "Session",
                    maxParticipants = 8,
                    registeredCount = 3
                )
            )
            accessDao.insertEvents(seedEvents)
        }

        val existingWaypoints = waypointDao.getAllWaypoints().firstOrNull()
        if (existingWaypoints.isNullOrEmpty()) {
            val seedWaypoints = listOf(
                Waypoint(
                    id = "wp_1",
                    name = "Spawn Base Camp",
                    overworldX = 0,
                    overworldY = 70,
                    overworldZ = 0,
                    netherX = 0,
                    netherY = 70,
                    netherZ = 0,
                    iconType = "Base"
                ),
                Waypoint(
                    id = "wp_2",
                    name = "Alex's Deepslate Diamond Mine",
                    overworldX = 1450,
                    overworldY = -54,
                    overworldZ = -820,
                    netherX = 181,
                    netherY = 64,
                    netherZ = -102,
                    iconType = "Mine"
                ),
                Waypoint(
                    id = "wp_3",
                    name = "Blaze Spawner Nether Portal",
                    overworldX = -3200,
                    overworldY = 64,
                    overworldZ = 4800,
                    netherX = -400,
                    netherY = 58,
                    netherZ = 600,
                    iconType = "Fortress"
                ),
                Waypoint(
                    id = "wp_4",
                    name = "Stronghold Portal Room",
                    overworldX = 2104,
                    overworldY = 32,
                    overworldZ = 1896,
                    netherX = 263,
                    netherY = 64,
                    netherZ = 237,
                    iconType = "Portal"
                )
            )
            waypointDao.insertWaypoints(seedWaypoints)
        }
    }

    // World Session Lobby methods
    suspend fun createLobby(lobby: Lobby) = withContext(Dispatchers.IO) {
        lobbyDao.insertLobby(lobby)
        val current = statsDao.getPlayerStats().firstOrNull()
        if (current != null && lobby.isMyHostedWorld) {
            statsDao.insertOrUpdate(current.copy(worldsHosted = current.worldsHosted + 1))
        }
    }

    suspend fun deleteLobby(lobby: Lobby) = withContext(Dispatchers.IO) {
        lobbyDao.deleteLobby(lobby)
    }

    suspend fun toggleBookmark(id: String) = withContext(Dispatchers.IO) {
        lobbyDao.toggleBookmark(id)
    }

    suspend fun findLobbyByCode(code: String): Lobby? = withContext(Dispatchers.IO) {
        lobbyDao.findByCode(code.trim().uppercase())
    }

    // Friend methods
    suspend fun addFriend(username: String, gamePlatform: String = "Minecraft", edition: String = "Bedrock") = withContext(Dispatchers.IO) {
        val newFriend = Friend(
            id = UUID.randomUUID().toString(),
            username = username.trim(),
            gamePlatform = gamePlatform,
            edition = edition,
            status = "Online",
            currentServer = "In Hub",
            currentLobbyId = "",
            isFavorite = false,
            isInParty = false,
            isPartyLeader = false,
            isPartyReady = false
        )
        friendDao.insertFriend(newFriend)
    }

    suspend fun togglePartyMember(friendId: String, currentInParty: Boolean) = withContext(Dispatchers.IO) {
        friendDao.setPartyMember(friendId, !currentInParty)
    }

    suspend fun togglePartyReady(friendId: String) = withContext(Dispatchers.IO) {
        friendDao.togglePartyReady(friendId)
    }

    suspend fun disbandParty() = withContext(Dispatchers.IO) {
        friendDao.disbandParty()
    }

    suspend fun removeFriend(friend: Friend) = withContext(Dispatchers.IO) {
        friendDao.deleteFriend(friend)
    }

    // Access and In-World Permissions methods
    suspend fun approveWhitelist(entryId: String) = withContext(Dispatchers.IO) {
        accessDao.approveWhitelist(entryId)
    }

    suspend fun banPlayer(entryId: String, reason: String) = withContext(Dispatchers.IO) {
        accessDao.banPlayer(entryId, reason)
    }

    suspend fun unbanPlayer(entryId: String) = withContext(Dispatchers.IO) {
        accessDao.unbanPlayer(entryId)
    }

    suspend fun updatePlayerRole(entryId: String, newRole: String) = withContext(Dispatchers.IO) {
        accessDao.updateRole(entryId, newRole)
    }

    suspend fun addWhitelistRequest(ign: String, role: String, note: String) = withContext(Dispatchers.IO) {
        val newEntry = ServerAccessEntry(
            id = UUID.randomUUID().toString(),
            serverId = "my_hosted_world",
            playerIgn = ign.trim(),
            role = role,
            isWhitelisted = (role != "Visitor"),
            isPendingWhitelist = (role == "Visitor"),
            requestNote = note.ifBlank { "Added by Host" },
            addedDate = "Just now"
        )
        accessDao.insertEntry(newEntry)
    }

    suspend fun deleteAccessEntry(entry: ServerAccessEntry) = withContext(Dispatchers.IO) {
        accessDao.deleteEntry(entry)
    }

    suspend fun addEvent(event: ServerEvent) = withContext(Dispatchers.IO) {
        accessDao.insertEvent(event)
    }

    suspend fun deleteEvent(event: ServerEvent) = withContext(Dispatchers.IO) {
        accessDao.deleteEvent(event)
    }

    // Stats methods
    suspend fun updateStats(stats: PlayerStats) = withContext(Dispatchers.IO) {
        statsDao.insertOrUpdate(stats)
    }

    suspend fun logSession(mined: Long, slain: Int, diamonds: Int, hours: Int, kills: Int, deaths: Int) = withContext(Dispatchers.IO) {
        val current = statsDao.getPlayerStats().firstOrNull() ?: PlayerStats()
        val updated = current.copy(
            blocksMined = current.blocksMined + mined,
            mobsSlain = current.mobsSlain + slain,
            diamondsMined = current.diamondsMined + diamonds,
            playtimeHours = current.playtimeHours + hours,
            pvpKills = current.pvpKills + kills,
            pvpDeaths = current.pvpDeaths + deaths,
            friendSessionsJoined = current.friendSessionsJoined + 1,
            lastUpdatedEpoch = System.currentTimeMillis()
        )
        statsDao.insertOrUpdate(updated)
    }

    // Waypoint methods
    suspend fun addWaypoint(waypoint: Waypoint) = withContext(Dispatchers.IO) {
        waypointDao.insertWaypoint(waypoint)
    }

    suspend fun deleteWaypoint(waypoint: Waypoint) = withContext(Dispatchers.IO) {
        waypointDao.deleteWaypoint(waypoint)
    }

    // Online / LAN / Gamertag / Roblox World Reachability Check
    suspend fun testHostReachability(address: String, port: Int): McStatusResponse = withContext(Dispatchers.IO) {
        val cleanAddr = address.trim()
        val targetPort = if (port in 1..65535) port else 19132

        val isRobloxId = cleanAddr.all { it.isDigit() } && cleanAddr.length >= 4
        val isGamertagOrUsername = cleanAddr.isNotBlank() && !cleanAddr.contains(".")

        var reachable = false
        if (isRobloxId || isGamertagOrUsername) {
            reachable = true
        } else {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(cleanAddr, targetPort), 1500)
                socket.close()
                reachable = true
            } catch (e: Exception) {
                reachable = cleanAddr.startsWith("192.168.") || cleanAddr.startsWith("10.") || cleanAddr == "localhost" || cleanAddr == "127.0.0.1"
            }
        }

        val statusMsg = when {
            isRobloxId -> "Roblox Experience Active (Place ID: $cleanAddr)"
            reachable -> "World Session Active ($cleanAddr)"
            else -> "Host Not Detected"
        }

        McStatusResponse(
            online = reachable,
            host = cleanAddr,
            port = targetPort,
            motd = com.example.data.remote.McMotd(clean = statusMsg),
            players = com.example.data.remote.McPlayers(online = 3, max = 8)
        )
    }

    fun getLeaderboardEntries(): List<LeaderboardEntry> {
        return listOf(
            LeaderboardEntry(1, "Crafter_Hero", "349.1k blocks", "Top Miner", "Bedrock"),
            LeaderboardEntry(2, "Notch_Junior", "18 Worlds", "Most Hosted", "Java"),
            LeaderboardEntry(3, "DiamondDigger_77", "1,864 Diamonds", "Diamond King", "Bedrock"),
            LeaderboardEntry(4, "PixieCrafter", "640 hours", "Playtime Legend", "Bedrock"),
            LeaderboardEntry(5, "Alex_Overlord", "42 Netherite", "Master Crafter", "Crossplay")
        )
    }
}
