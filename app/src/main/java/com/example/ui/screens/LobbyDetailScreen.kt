package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Lobby
import com.example.ui.components.ConnectionTypeBadge
import com.example.ui.components.EditionBadge
import com.example.ui.components.MinecraftAvatar
import com.example.ui.components.PlatformBadge
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RedstoneRed
import com.example.ui.theme.RobloxBlue
import com.example.ui.theme.RobloxGlow
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyDetailScreen(
    lobby: Lobby,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenWorldChat: (Lobby) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pingState by viewModel.pingTestState.collectAsState()
    val partyMembers by viewModel.partyMembers.collectAsState()
    val joinedLobby by viewModel.activeJoinedLobby.collectAsState()

    val isRoblox = lobby.gamePlatform.equals("Roblox", ignoreCase = true)
    val accentColor = if (isRoblox) RobloxBlue else EmeraldPrimary
    val accentGlow = if (isRoblox) RobloxGlow else EmeraldGlow

    // Test host / experience reachability
    LaunchedEffect(lobby.serverAddress) {
        viewModel.testHostReachability(lobby.serverAddress, lobby.port)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = lobby.name,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleLobbyBookmark(lobby.id) }
                    ) {
                        Icon(
                            imageVector = if (lobby.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (lobby.isBookmarked) GoldAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { viewModel.shareLobby(context, lobby) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share World",
                            tint = DiamondCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Hero Card
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PlatformBadge(gamePlatform = lobby.gamePlatform)
                                EditionBadge(edition = lobby.edition)
                                ConnectionTypeBadge(connectionType = lobby.connectionType)
                            }

                            // Invite Code Chip
                            Surface(
                                color = Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = lobby.inviteCode,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MinecraftAvatar(
                                username = lobby.hostUsername,
                                sizeDp = 48,
                                gamePlatform = lobby.gamePlatform
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = lobby.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isRoblox) "Hosted by @${lobby.hostUsername}" else "World Hosted by ${lobby.hostUsername}",
                                    fontSize = 12.sp,
                                    color = accentGlow,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = lobby.motd.replace("§.", ""),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Primary Join Button
                        Button(
                            onClick = { viewModel.joinLobbyAndShowGuide(context, lobby) },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = if (isRoblox) Color.White else Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("launch_minecraft_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRoblox) "JOIN ROBLOX EXPERIENCE" else "JOIN WORLD",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.pinLobbyToFloating(lobby) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("pin_floating_bubble_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = accentGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Pin Bubble",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = { onOpenWorldChat(lobby) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("open_world_chat_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = null,
                                    tint = DiamondCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "World Chat",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Exact Hosting & Joining Instructions Card
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isRoblox) "How Roblox Lobby Works" else "How Minecraft Hosting & Joining Works",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (isRoblox) {
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("JOINING:", fontSize = 10.sp, fontWeight = FontWeight.Black, color = RobloxGlow)
                                    Text(
                                        text = "Press Join here, then tap Open Roblox to launch straight into Place ID ${lobby.robloxPlaceId} (or join @${lobby.hostUsername} from Friends).",
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("HOSTING:", fontSize = 10.sp, fontWeight = FontWeight.Black, color = GoldAccent)
                                    Text(
                                        text = "Open your world, then turn on Multiplayer Game and Visible to LAN Players.",
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("JOINING:", fontSize = 10.sp, fontWeight = FontWeight.Black, color = EmeraldGlow)
                                    Text(
                                        text = "Press Join here, then open Minecraft > Play > Worlds and tap the world.",
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Host Identity / Place ID Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isRoblox) "ROBLOX PLACE ID" else "HOST GAMERTAG (${lobby.connectionType})",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DiamondCyan
                                )
                                Text(
                                    text = if (isRoblox) lobby.robloxPlaceId.ifBlank { lobby.hostUsername } else lobby.hostUsername,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }

                        // World Settings Grid
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("LOBBY SETTINGS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Mode: ${lobby.gameMode}", fontSize = 12.sp, color = Color.White)
                                    Text("Difficulty: ${lobby.difficulty}", fontSize = 12.sp, color = Color.White)
                                }
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Players: ${lobby.currentPlayers}/${lobby.maxPlayers}", fontSize = 12.sp, color = EmeraldLight)
                                    if (!isRoblox) {
                                        Text(
                                            text = if (lobby.keepInventory) "KeepInv: ON" else "KeepInv: OFF",
                                            fontSize = 12.sp,
                                            color = if (lobby.keepInventory) EmeraldLight else GoldAccent
                                        )
                                    } else {
                                        Text(
                                            text = "Access: ${lobby.accessType}",
                                            fontSize = 12.sp,
                                            color = RobloxGlow
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Online World Reachability Check
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SignalCellularAlt,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Lobby & Connection Status",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            IconButton(
                                onClick = { viewModel.testHostReachability(lobby.serverAddress, lobby.port) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Status",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (pingState.isLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 10.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = accentColor)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Checking lobby status...", fontSize = 13.sp)
                            }
                        } else if (pingState.result != null) {
                            val res = pingState.result!!
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (res.online) EmeraldPrimary else RedstoneRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = res.motd?.clean ?: if (res.online) "Lobby is Active & Joinable!" else "Lobby offline.",
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.online) EmeraldLight else RedstoneRed,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Squad Party Launch Integration
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Groups, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Join with Squad (${partyMembers.size} in squad)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Jump into ${lobby.hostUsername}'s ${lobby.gamePlatform} world with your active squad.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        FilledTonalButton(
                            onClick = { viewModel.joinLobbyAndShowGuide(context, lobby) },
                            modifier = Modifier.fillMaxWidth().testTag("join_with_party_btn")
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Join With Squad")
                        }
                    }
                }
            }

            // Host Control: Delete World Session
            item {
                OutlinedButton(
                    onClick = {
                        viewModel.deleteLobby(lobby)
                        onBack()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedstoneRed),
                    modifier = Modifier.fillMaxWidth().testTag("delete_lobby_btn")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Close World Broadcast")
                }
            }
        }
    }

    // Show Join confirmation dialog if triggered from detail screen
    joinedLobby?.let { active ->
        JoinWorldConfirmationDialog(
            lobby = active,
            onDismiss = { viewModel.dismissJoinedLobbyModal() },
            onOpenGame = { viewModel.launchGameApp(context, active) }
        )
    }
}
