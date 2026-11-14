package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Lobby
import com.example.ui.components.LobbyCard
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RobloxBlue
import com.example.ui.theme.RobloxGlow
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyHubScreen(
    viewModel: MainViewModel,
    unreadChatCount: Int = 0,
    onOpenMessenger: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lobbies by viewModel.filteredLobbies.collectAsState()
    val searchQuery by viewModel.lobbySearchQuery.collectAsState()
    val platformFilter by viewModel.lobbyPlatformFilter.collectAsState()
    val filterType by viewModel.lobbyConnectionTypeFilter.collectAsState()
    val joinedLobby by viewModel.activeJoinedLobby.collectAsState()
    val isFloatingVisible by viewModel.isFloatingBubbleVisible.collectAsState()
    val isSystemOverlayRunning by viewModel.isSystemOverlayRunning.collectAsState()

    var showHostDialog by remember { mutableStateOf(false) }
    var showJoinCodeDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // Dual-Game Hero Banner Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(195.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_gaming_lobby_banner),
                        contentDescription = "Minecraft and Roblox Multiplayer Lobbies Hub",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x440A0E17),
                                        Color(0xDD0A0E17),
                                        Color(0xFF0A0E17)
                                    )
                                )
                            )
                    )

                    // Hero Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = EmeraldPrimary,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.VideogameAsset,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "FreebookLive",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Minecraft & Roblox Multiplayer Lobby",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldGlow,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Messenger Pill Button
                                Surface(
                                    color = if (unreadChatCount > 0) DiamondCyan.copy(alpha = 0.2f) else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(20.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (unreadChatCount > 0) DiamondCyan else Color(0xFF475569)
                                    ),
                                    modifier = Modifier
                                        .clickable { onOpenMessenger() }
                                        .testTag("open_messenger_header_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Chat,
                                            contentDescription = "Open Messenger",
                                            tint = DiamondCyan,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (unreadChatCount > 0) "Chat ($unreadChatCount)" else "Chat",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Floating Bubble Toggle Pill
                                Surface(
                                    color = if (isFloatingVisible || isSystemOverlayRunning) EmeraldPrimary.copy(alpha = 0.22f) else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(20.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isFloatingVisible || isSystemOverlayRunning) EmeraldPrimary else Color(0xFF475569)
                                    ),
                                    modifier = Modifier
                                        .clickable { viewModel.toggleFloatingBubble() }
                                        .testTag("toggle_floating_bubble_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Layers,
                                            contentDescription = "Toggle Floating Bubble",
                                            tint = if (isFloatingVisible || isSystemOverlayRunning) EmeraldGlow else Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isFloatingVisible) "Floating ON" else "Floating",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isFloatingVisible || isSystemOverlayRunning) EmeraldGlow else Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Invite Code Banner Button
                        Surface(
                            color = Color(0xFF1E293B).copy(alpha = 0.92f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DiamondCyan.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showJoinCodeDialog = true }
                                .testTag("quick_join_code_bar")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Enter Lobby Code (e.g. FBL-1001 or RBX-4924)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = "Join >",
                                    color = DiamondCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Platform Switcher (All / Minecraft / Roblox) + Quick Steps Card
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    // Platform Selector Pills
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val platforms = listOf("All", "Minecraft", "Roblox")
                        platforms.forEach { plat ->
                            val isSelected = platformFilter == plat
                            val pillBg = when {
                                !isSelected -> MaterialTheme.colorScheme.surfaceVariant
                                plat == "Roblox" -> RobloxBlue
                                else -> EmeraldPrimary
                            }
                            val pillContent = when {
                                !isSelected -> MaterialTheme.colorScheme.onSurfaceVariant
                                plat == "Roblox" -> Color.White
                                else -> Color.Black
                            }
                            Surface(
                                color = pillBg,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) pillBg else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clickable { viewModel.lobbyPlatformFilter.value = plat }
                                    .testTag("platform_filter_${plat.lowercase()}")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (plat == "All") "All Games" else plat,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = pillContent
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Compact How-To Quick Card (Host & Join workflow)
                    Surface(
                        color = Color(0xFF111C2D),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "QUICK HOST & JOIN GUIDE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GoldAccent
                                )
                                Text(
                                    text = "Host Your Own World >",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight,
                                    modifier = Modifier
                                        .clickable { showHostDialog = true }
                                        .testTag("inline_host_world_link")
                                )
                            }
                            Text(
                                text = "• Hosting: Open your world, then turn on Multiplayer Game and Visible to LAN Players (or paste your Roblox Place ID).",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 15.sp
                            )
                            Text(
                                text = "• Joining: Press Join here, then open Minecraft > Play > Worlds and tap the world (or jump straight into Roblox).",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.lobbySearchQuery.value = it },
                        placeholder = { Text("Search world, Roblox game, host, code...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = EmeraldPrimary)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.lobbySearchQuery.value = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lobby_search_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Connection Filters
                    val filters = listOf("All", "Worlds / LAN", "Mobile Data", "Hosted by Me", "Bookmarked")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filters) { filter ->
                            val isSelected = filterType == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.lobbyConnectionTypeFilter.value = filter },
                                label = { Text(filter, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldDark,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_chip_$filter")
                            )
                        }
                    }
                }
            }

            // Active Worlds List
            if (lobbies.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 36.dp, start = 20.dp, end = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SignalCellularAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matching multiplayer lobbies",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Host Your World' below to open a Minecraft or Roblox lobby for your friends!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(lobbies, key = { it.id }) { lobby ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        LobbyCard(
                            lobby = lobby,
                            onLobbyClick = { viewModel.selectLobby(lobby) },
                            onJoinClick = {
                                viewModel.joinLobbyAndShowGuide(context, lobby)
                            },
                            onBookmarkToggle = { viewModel.toggleLobbyBookmark(lobby.id) }
                        )
                    }
                }
            }
        }

        // Host Your Own World FAB
        FloatingActionButton(
            onClick = { showHostDialog = true },
            containerColor = EmeraldPrimary,
            contentColor = Color.Black,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 20.dp)
                .testTag("host_lobby_fab")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Host Your Own World")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Host Your World",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Join with Code Dialog
    if (showJoinCodeDialog) {
        var inputCode by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showJoinCodeDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = GoldAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Join Friend's Lobby")
                }
            },
            text = {
                Column {
                    Text(
                        "Enter the invite code from your friend's Minecraft or Roblox lobby:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputCode,
                        onValueChange = { inputCode = it.uppercase() },
                        placeholder = { Text("FBL-1001 or RBX-4924") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (inputCode.isNotBlank()) {
                                    viewModel.joinLobbyByCode(inputCode)
                                    showJoinCodeDialog = false
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("invite_code_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputCode.isNotBlank()) {
                            viewModel.joinLobbyByCode(inputCode)
                            showJoinCodeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                    modifier = Modifier.testTag("submit_invite_code_btn")
                ) {
                    Text("Join")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinCodeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Host Your Own World Dialog (Minecraft & Roblox)
    if (showHostDialog) {
        HostWorldDialog(
            onDismiss = { showHostDialog = false },
            onHost = { name, platform, hostUser, connType, placeId, privateLink, edition, mode, diff, keepInv, pvp, maxP, access, motd ->
                viewModel.hostMyWorld(
                    worldName = name,
                    gamePlatform = platform,
                    hostUsername = hostUser,
                    connectionType = connType,
                    robloxPlaceId = placeId,
                    robloxPrivateLink = privateLink,
                    edition = edition,
                    gameMode = mode,
                    difficulty = diff,
                    keepInventory = keepInv,
                    allowPvp = pvp,
                    maxPlayers = maxP,
                    accessType = access,
                    motd = motd
                )
                showHostDialog = false
            }
        )
    }

    // Step-by-step Join Confirmation Dialog
    joinedLobby?.let { lobby ->
        JoinWorldConfirmationDialog(
            lobby = lobby,
            onDismiss = { viewModel.dismissJoinedLobbyModal() },
            onOpenGame = { viewModel.launchGameApp(context, lobby) }
        )
    }
}

@Composable
fun JoinWorldConfirmationDialog(
    lobby: Lobby,
    onDismiss: () -> Unit,
    onOpenGame: () -> Unit
) {
    val isRoblox = lobby.gamePlatform.equals("Roblox", ignoreCase = true)
    val accent = if (isRoblox) RobloxBlue else EmeraldPrimary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = accent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRoblox) "Joining Roblox Lobby" else "Ready to Join World!",
                    fontWeight = FontWeight.Black
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = lobby.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isRoblox) "Host: @${lobby.hostUsername} • Place ID: ${lobby.robloxPlaceId}" else "Host: ${lobby.hostUsername} • Code: ${lobby.inviteCode}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (isRoblox) RobloxGlow else EmeraldGlow
                        )
                    }
                }

                if (isRoblox) {
                    Text(
                        text = "How to Join in Roblox:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Text(
                        text = "1. Tap 'Open Roblox' below to jump straight into this experience (Place ID: ${lobby.robloxPlaceId}).\n2. Or join @${lobby.hostUsername} from your Roblox Friends tab.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )
                } else {
                    Text(
                        text = "How to Join in Minecraft:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Text(
                        text = "1. You pressed Join here.\n2. Now open Minecraft > Play > Worlds and tap '${lobby.name}'.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onOpenGame()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = if (isRoblox) Color.White else Color.Black
                ),
                modifier = Modifier.testTag("dialog_open_game_btn")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isRoblox) "Open Roblox" else "Open Minecraft", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostWorldDialog(
    onDismiss: () -> Unit,
    onHost: (String, String, String, String, String, String, String, String, String, Boolean, Boolean, Int, String, String) -> Unit
) {
    var gamePlatform by remember { mutableStateOf("Minecraft") }
    var worldName by remember { mutableStateOf("") }
    var hostUsername by remember { mutableStateOf("") }
    var connectionType by remember { mutableStateOf("LAN / Worlds Tab") }
    var robloxPlaceId by remember { mutableStateOf("4924922222") }
    var robloxPrivateLink by remember { mutableStateOf("") }
    var edition by remember { mutableStateOf("Bedrock") }
    var gameMode by remember { mutableStateOf("Survival") }
    var difficulty by remember { mutableStateOf("Normal") }
    var keepInventory by remember { mutableStateOf(true) }
    var allowPvp by remember { mutableStateOf(false) }
    var maxPlayers by remember { mutableIntStateOf(8) }
    var accessType by remember { mutableStateOf("Friends Only") }
    var motd by remember { mutableStateOf("") }

    val isRoblox = gamePlatform == "Roblox"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Host Your Own World", fontWeight = FontWeight.Black) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Platform Toggle: Minecraft vs Roblox
                item {
                    Text("Choose Game", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("Minecraft", "Roblox").forEach { plat ->
                            val selected = gamePlatform == plat
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    gamePlatform = plat
                                    if (plat == "Roblox") {
                                        connectionType = "Roblox Friend Lobby"
                                        edition = "Roleplay"
                                        gameMode = "Roleplay"
                                        difficulty = "Casual"
                                        maxPlayers = 12
                                    } else {
                                        connectionType = "LAN / Worlds Tab"
                                        edition = "Bedrock"
                                        gameMode = "Survival"
                                        difficulty = "Normal"
                                        maxPlayers = 8
                                    }
                                },
                                label = { Text(plat, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (plat == "Roblox") Icons.Default.Public else Icons.Default.VideogameAsset,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f).testTag("host_platform_${plat.lowercase()}")
                            )
                        }
                    }
                }

                // Exact Hosting Instructions Reminder
                item {
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isRoblox) RobloxBlue.copy(alpha = 0.5f) else EmeraldPrimary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isRoblox) "ROBLOX HOSTING STEPS" else "MINECRAFT HOSTING STEPS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldAccent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isRoblox) {
                                    "Open your Roblox world or experience, pick a preset or paste your Place ID, then publish your lobby here for friends to join."
                                } else {
                                    "Open your world in Minecraft, then turn on Multiplayer Game and Visible to LAN Players (enable Use Cellular Data if off Wi-Fi)."
                                },
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = worldName,
                        onValueChange = { worldName = it },
                        label = { Text(if (isRoblox) "Roblox World / Experience Name" else "Minecraft World Name") },
                        placeholder = { Text(if (isRoblox) "e.g. Brookhaven Squad RP" else "e.g. Alex's Survival Kingdom") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("host_world_name_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = hostUsername,
                        onValueChange = { hostUsername = it },
                        label = { Text(if (isRoblox) "Your Roblox Username" else "Your Minecraft Gamertag") },
                        placeholder = { Text(if (isRoblox) "e.g. BloxHero_99" else "e.g. Crafter_Hero") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (isRoblox) {
                    item {
                        Text("Quick Roblox Presets (or Custom Place ID)", style = MaterialTheme.typography.labelMedium)
                        val presets = listOf(
                            "Brookhaven" to "4924922222",
                            "Blox Fruits" to "2753915549",
                            "Tower of Hell" to "1962086868",
                            "Adopt Me" to "920587237"
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(presets) { (title, id) ->
                                FilterChip(
                                    selected = robloxPlaceId == id,
                                    onClick = {
                                        robloxPlaceId = id
                                        if (worldName.isBlank()) worldName = "$title Squad World"
                                    },
                                    label = { Text(title, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = robloxPlaceId,
                            onValueChange = { robloxPlaceId = it },
                            label = { Text("Roblox Place ID") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = robloxPrivateLink,
                            onValueChange = { robloxPrivateLink = it },
                            label = { Text("Private Server Code / Link (Optional)") },
                            placeholder = { Text("Optional private server code") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    item {
                        Text("Connection Method", style = MaterialTheme.typography.labelMedium)
                        val connectionOptions = listOf("LAN / Worlds Tab", "Mobile Data (4G/5G)", "Gamertag Online Join")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(connectionOptions) { opt ->
                                FilterChip(
                                    selected = connectionType == opt,
                                    onClick = { connectionType = opt },
                                    label = { Text(opt, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    item {
                        Text("Game Mode & Difficulty", style = MaterialTheme.typography.labelMedium)
                        val gameModes = listOf("Survival", "Creative", "Hardcore", "Adventure")
                        val difficulties = listOf("Peaceful", "Easy", "Normal", "Hard")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(gameModes) { mode ->
                                FilterChip(
                                    selected = gameMode == mode,
                                    onClick = { gameMode = mode },
                                    label = { Text(mode, fontSize = 11.sp) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(difficulties) { diff ->
                                FilterChip(
                                    selected = difficulty == diff,
                                    onClick = { difficulty = diff },
                                    label = { Text(diff, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    item {
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Keep Inventory On Death", fontSize = 12.sp, color = Color.White)
                                    Switch(
                                        checked = keepInventory,
                                        onCheckedChange = { keepInventory = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Allow Friendly PvP", fontSize = 12.sp, color = Color.White)
                                    Switch(
                                        checked = allowPvp,
                                        onCheckedChange = { allowPvp = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = motd,
                        onValueChange = { motd = it },
                        label = { Text("Lobby Message for Friends") },
                        placeholder = { Text("Join my world! Squad meetup at spawn.") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onHost(
                        worldName,
                        gamePlatform,
                        hostUsername,
                        connectionType,
                        robloxPlaceId,
                        robloxPrivateLink,
                        edition,
                        gameMode,
                        difficulty,
                        keepInventory,
                        allowPvp,
                        maxPlayers,
                        accessType,
                        motd
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRoblox) RobloxBlue else EmeraldPrimary,
                    contentColor = if (isRoblox) Color.White else Color.Black
                ),
                modifier = Modifier.testTag("confirm_create_lobby_btn")
            ) {
                Text("Host World")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
