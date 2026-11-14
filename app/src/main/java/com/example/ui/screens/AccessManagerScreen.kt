package com.example.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServerAccessEntry
import com.example.data.model.ServerEvent
import com.example.ui.components.MinecraftAvatar
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NetherPurple
import com.example.ui.theme.RedstoneRed
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessManagerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.serverAccessEntries.collectAsState()
    val events by viewModel.serverEvents.collectAsState()

    var selectedSection by remember { mutableIntStateOf(0) } // 0: Permissions & Roles, 1: Blocked Players, 2: World Events
    var showAddEntryDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Navigation Tabs
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = EmeraldPrimary
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text("In-World Roles", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text("Blocked Players", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                text = { Text("World Sessions", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedSection) {
                0 -> {
                    // Pending Requests Banner
                    val pendingRequests = entries.filter { it.isPendingWhitelist && !it.isBanned }
                    if (pendingRequests.isNotEmpty()) {
                        item {
                            Surface(
                                color = GoldAccent.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = GoldAccent)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Pending World Join Requests (${pendingRequests.size})",
                                            fontWeight = FontWeight.Bold,
                                            color = GoldAccent
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    pendingRequests.forEach { req ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                                                .padding(10.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                MinecraftAvatar(username = req.playerIgn, sizeDp = 36)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(req.playerIgn, fontWeight = FontWeight.Bold, color = Color.White)
                                                    Text(req.requestNote, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                                }
                                            }

                                            Row {
                                                Button(
                                                    onClick = { viewModel.approveWhitelist(req.id, req.playerIgn) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.height(32.dp).testTag("approve_whitelist_${req.playerIgn}")
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = "Approve", modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Allow", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Permissions Header & Add Button
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Trusted Friends in World",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Assign in-world permissions (Operator OP, Member, Visitor)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { showAddEntryDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                                modifier = Modifier.height(34.dp).testTag("add_whitelist_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Friend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    val activePlayers = entries.filter { !it.isBanned && !it.isPendingWhitelist }
                    items(activePlayers, key = { it.id }) { entry ->
                        PlayerAccessCard(
                            entry = entry,
                            onRoleChange = { newRole -> viewModel.updatePlayerRole(entry.id, entry.playerIgn, newRole) },
                            onBan = { reason -> viewModel.banPlayer(entry.id, entry.playerIgn, reason) },
                            onDelete = { viewModel.deleteAccessEntry(entry) }
                        )
                    }
                }

                1 -> {
                    // Blocked Players Section
                    val bannedPlayers = entries.filter { it.isBanned }
                    if (bannedPlayers.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No Blocked Players", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("No friends are currently blocked from your hosted worlds.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(bannedPlayers, key = { it.id }) { banned ->
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RedstoneRed.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        MinecraftAvatar(username = banned.playerIgn, sizeDp = 40)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(banned.playerIgn, fontWeight = FontWeight.Bold, color = RedstoneRed)
                                            Text("Reason: ${banned.banReason.ifBlank { "Blocked by Host" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.unbanPlayer(banned.id, banned.playerIgn) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldLight),
                                        modifier = Modifier.height(34.dp).testTag("unban_${banned.playerIgn}")
                                    ) {
                                        Text("Unblock", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // World Events / Scheduled Sessions Section
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Scheduled World Sessions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Plan group survival nights & build contests",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { showAddEventDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                                modifier = Modifier.height(34.dp).testTag("schedule_event_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Session", fontSize = 11.sp)
                            }
                        }
                    }

                    items(events, key = { it.id }) { event ->
                        ServerEventCard(
                            event = event,
                            onDelete = { viewModel.deleteEvent(event) }
                        )
                    }
                }
            }
        }

        // Add Player Entry Dialog
        if (showAddEntryDialog) {
            AddAccessPlayerDialog(
                onDismiss = { showAddEntryDialog = false },
                onAdd = { ign, role, note ->
                    viewModel.addAccessEntry(ign, role, note)
                    showAddEntryDialog = false
                }
            )
        }

        // Schedule Event Dialog
        if (showAddEventDialog) {
            AddEventDialog(
                onDismiss = { showAddEventDialog = false },
                onAdd = { title, desc, date, type ->
                    viewModel.addEvent(title, desc, date, type)
                    showAddEventDialog = false
                }
            )
        }
    }
}

@Composable
fun PlayerAccessCard(
    entry: ServerAccessEntry,
    onRoleChange: (String) -> Unit,
    onBan: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showBanPrompt by remember { mutableStateOf(false) }

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
                MinecraftAvatar(username = entry.playerIgn, sizeDp = 42)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.playerIgn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        RoleBadge(role = entry.role)
                    }
                    val roleDesc = when (entry.role) {
                        "Operator (OP)" -> "Can use /tp & commands"
                        "Member" -> "Can build, mine, open chests"
                        "Visitor" -> "Explore only (grief-proof)"
                        else -> "World Host"
                    }
                    Text(
                        text = "$roleDesc • ${entry.requestNote}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box {
                OutlinedButton(
                    onClick = { showMenu = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp).testTag("role_menu_btn_${entry.playerIgn}")
                ) {
                    Text("Role", fontSize = 11.sp)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    listOf("Operator (OP)", "Member", "Visitor").forEach { r ->
                        DropdownMenuItem(
                            text = { Text("Set as $r") },
                            onClick = {
                                onRoleChange(r)
                                showMenu = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Block from My Worlds", color = RedstoneRed) },
                        onClick = {
                            showMenu = false
                            showBanPrompt = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Remove from List", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }

    if (showBanPrompt) {
        var banReason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBanPrompt = false },
            title = { Text("Block ${entry.playerIgn}?") },
            text = {
                OutlinedTextField(
                    value = banReason,
                    onValueChange = { banReason = it },
                    label = { Text("Reason") },
                    placeholder = { Text("e.g. Griefing base, taking items") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBan(banReason)
                        showBanPrompt = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedstoneRed)
                ) {
                    Text("Block")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBanPrompt = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RoleBadge(role: String) {
    val (color, label) = when (role) {
        "Host (Owner)" -> GoldAccent to "HOST"
        "Operator (OP)" -> RedstoneRed to "OP / COMMANDS"
        "Visitor" -> Color(0xFF64748B) to "VISITOR (SAFE)"
        else -> EmeraldLight to "MEMBER"
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ServerEventCard(
    event: ServerEvent,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = when (event.eventType) {
        "BossRaid" -> NetherPurple
        "BuildParty" -> GoldAccent
        else -> EmeraldLight
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = event.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = event.dateString,
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "${event.registeredCount} Friends Joining",
                    fontSize = 11.sp,
                    color = EmeraldLight,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun AddAccessPlayerDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var ign by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Member") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Grant In-World Permissions", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = ign,
                    onValueChange = { ign = it },
                    label = { Text("Friend's Minecraft IGN") },
                    placeholder = { Text("Crafter_Alex") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Permission Level", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Operator (OP)", "Member", "Visitor").forEach { r ->
                        FilterChip(
                            selected = role == r,
                            onClick = { role = r },
                            label = { Text(r) }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notes (e.g. Minecraft / Roblox friend)") },
                    placeholder = { Text("Squad friend / Builder") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ign.isNotBlank()) onAdd(ign, role, note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
            ) {
                Text("Allow Friend")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("BossRaid") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule World Session", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Session Title") },
                    placeholder = { Text("e.g. Wither Boss Battle in My World") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Session Goals & Rules") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date & Time") },
                    placeholder = { Text("Saturday 7:00 PM EST") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Type", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("BossRaid", "BuildParty", "Session").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onAdd(title, desc, date, type)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
            ) {
                Text("Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
