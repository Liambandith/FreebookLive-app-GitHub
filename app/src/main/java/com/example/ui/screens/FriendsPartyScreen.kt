package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Friend
import com.example.ui.components.EditionBadge
import com.example.ui.components.MinecraftAvatar
import com.example.ui.components.PlatformBadge
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RedstoneRed
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsPartyScreen(
    viewModel: MainViewModel,
    onOpenPartyChat: () -> Unit = {},
    onMessageFriend: (Friend) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val friends by viewModel.allFriends.collectAsState()
    val partyMembers by viewModel.partyMembers.collectAsState()
    var showAddFriendDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Multiplayer Party Squad Section
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("party_squad_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = EmeraldPrimary,
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Groups,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Active Multiplayer Party",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${partyMembers.size} Member${if (partyMembers.size != 1) "s" else ""} in Squad",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = EmeraldGlow
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = onOpenPartyChat,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(34.dp).testTag("open_party_chat_btn")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Squad Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                if (partyMembers.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { viewModel.disbandParty() },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedstoneRed),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Disband", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (partyMembers.isEmpty()) {
                            Text(
                                text = "No active squad. Tap '+ Party' on any friend below to invite them to join Minecraft servers together!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                partyMembers.forEach { member ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            MinecraftAvatar(username = member.username, sizeDp = 34)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = member.username,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color.White
                                                    )
                                                    if (member.isPartyLeader) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(
                                                            color = GoldAccent.copy(alpha = 0.2f),
                                                            shape = RoundedCornerShape(4.dp)
                                                        ) {
                                                            Text(
                                                                text = "LEADER",
                                                                color = GoldAccent,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = if (member.isPartyReady) "Ready to connect" else "Waiting in hub...",
                                                    fontSize = 11.sp,
                                                    color = if (member.isPartyReady) EmeraldLight else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedButton(
                                                onClick = { viewModel.togglePartyReady(member.id) },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = if (member.isPartyReady) EmeraldLight else GoldAccent
                                                ),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text(if (member.isPartyReady) "Ready" else "Set Ready", fontSize = 11.sp)
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))

                                            IconButton(
                                                onClick = { viewModel.togglePartyMember(member.id, true) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Leave party",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Friend List Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Minecraft & Roblox Friends (${friends.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedButton(
                        onClick = { showAddFriendDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp).testTag("add_friend_btn")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Friend", fontSize = 12.sp)
                    }
                }
            }

            // Friend Items
            items(friends, key = { it.id }) { friend ->
                FriendItemCard(
                    friend = friend,
                    onToggleParty = { viewModel.togglePartyMember(friend.id, friend.isInParty) },
                    onJoinServer = {
                        viewModel.lobbyPlatformFilter.value = "All"
                        viewModel.lobbySearchQuery.value = friend.currentServer
                        viewModel.selectTab(com.example.ui.viewmodel.NavigationTab.LOBBIES)
                    },
                    onMessage = { onMessageFriend(friend) },
                    onRemove = { viewModel.removeFriend(friend) }
                )
            }
        }

        // Add Friend Dialog
        if (showAddFriendDialog) {
            AddFriendDialog(
                onDismiss = { showAddFriendDialog = false },
                onAdd = { username, gamePlatform, edition ->
                    viewModel.addFriend(username, gamePlatform, edition)
                    showAddFriendDialog = false
                }
            )
        }
    }
}

@Composable
fun FriendItemCard(
    friend: Friend,
    onToggleParty: () -> Unit,
    onJoinServer: () -> Unit,
    onMessage: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Avatar with status dot
                Box {
                    MinecraftAvatar(
                        username = friend.username,
                        sizeDp = 44,
                        gamePlatform = friend.gamePlatform
                    )
                    // Status dot
                    val isHostingOrPlaying = friend.status.contains("Hosting") || friend.status.contains("Playing") || friend.status.contains("World")
                    val dotColor = when {
                        isHostingOrPlaying -> EmeraldLight
                        friend.status == "In Lobby" -> GoldAccent
                        friend.status == "Online" -> DiamondCyan
                        else -> Color(0xFF64748B)
                    }
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = friend.username,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PlatformBadge(gamePlatform = friend.gamePlatform)
                        EditionBadge(edition = friend.edition)
                    }
                    Spacer(modifier = Modifier.height(2.dp))

                    val isHostingOrPlaying = friend.status.contains("Hosting") || friend.status.contains("Playing") || friend.status.contains("World")
                    if (isHostingOrPlaying && friend.currentServer.isNotBlank()) {
                        Text(
                            text = "${friend.status}: ${friend.currentServer}",
                            fontSize = 11.sp,
                            color = EmeraldLight,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = friend.status,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action Buttons
            val canJoin = (friend.status.contains("Hosting") || friend.status.contains("Playing") || friend.status.contains("World")) && friend.currentServer.isNotBlank()
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canJoin) {
                    Button(
                        onClick = onJoinServer,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.height(32.dp).testTag("join_friend_${friend.username}")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Join", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                IconButton(
                    onClick = onMessage,
                    modifier = Modifier.size(36.dp).testTag("message_friend_${friend.username}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Message ${friend.username}",
                        tint = DiamondCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                OutlinedButton(
                    onClick = onToggleParty,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (friend.isInParty) GoldAccent else DiamondCyan
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(if (friend.isInParty) "In Party" else "+ Party", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var gamePlatform by remember { mutableStateOf("Minecraft") }
    var edition by remember { mutableStateOf("Bedrock") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Gaming Friend", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Choose game platform and enter your friend's username or Gamertag:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text("Game Platform", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Minecraft", "Roblox").forEach { plat ->
                        FilterChip(
                            selected = gamePlatform == plat,
                            onClick = {
                                gamePlatform = plat
                                edition = if (plat == "Roblox") "Roleplay" else "Bedrock"
                            },
                            label = { Text(plat) }
                        )
                    }
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(if (gamePlatform == "Roblox") "Roblox Username" else "Minecraft IGN / Gamertag") },
                    placeholder = { Text(if (gamePlatform == "Roblox") "e.g. BloxBuilder_Kai" else "e.g. Alex_Overlord") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_friend_ign_field")
                )

                Text(if (gamePlatform == "Roblox") "Favorite Genre" else "Edition", style = MaterialTheme.typography.labelMedium)
                val opts = if (gamePlatform == "Roblox") {
                    listOf("Roleplay", "Action RPG", "Obby")
                } else {
                    listOf("Bedrock", "Java", "Crossplay")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    opts.forEach { ed ->
                        FilterChip(
                            selected = edition == ed,
                            onClick = { edition = ed },
                            label = { Text(ed, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (username.isNotBlank()) {
                        onAdd(username, gamePlatform, edition)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                modifier = Modifier.testTag("submit_add_friend_btn")
            ) {
                Text("Add Friend")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
