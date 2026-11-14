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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlayerStats
import com.example.ui.components.EditionBadge
import com.example.ui.components.MinecraftAvatar
import com.example.ui.components.StatCard
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NetherPurple
import com.example.ui.theme.RedstoneRed
import com.example.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileStatsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.playerStats.collectAsState()
    val leaderboards = viewModel.leaderboards

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Career Stats, 1: Leaderboards
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLogSessionDialog by remember { mutableStateOf(false) }

    val numFormat = remember { NumberFormat.getNumberInstance(Locale.US) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Player Profile Card
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth().testTag("player_profile_card")
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF1E293B),
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
                                MinecraftAvatar(username = stats.ign, sizeDp = 56)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stats.ign,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                    Text(
                                        text = stats.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = EmeraldLight,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Roblox: @${stats.robloxUsername} • Clan: ${stats.guild}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showEditProfileDialog = true },
                                modifier = Modifier.testTag("edit_profile_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = DiamondCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Achievements Progress
                        val progress = stats.achievementsUnlocked.toFloat() / stats.maxAchievements.toFloat()
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Achievements",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldAccent
                                    )
                                }
                                Text(
                                    text = "${stats.achievementsUnlocked} / ${stats.maxAchievements} Unlocked",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = GoldAccent,
                                trackColor = Color(0xFF334155),
                            )
                        }
                    }
                }
            }

            // Tab Selector: Career Stats vs Leaderboard
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = EmeraldPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Career Stats", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Lobby Leaderboards", fontWeight = FontWeight.Bold) }
                    )
                }
            }

            if (selectedTab == 0) {
                // Stats Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            StatCard(
                                title = "Blocks Mined",
                                value = numFormat.format(stats.blocksMined),
                                icon = Icons.Default.Terrain,
                                accentColor = EmeraldLight,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Diamonds Found",
                                value = numFormat.format(stats.diamondsMined),
                                icon = Icons.Default.Diamond,
                                accentColor = DiamondCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            StatCard(
                                title = "Mobs Slain",
                                value = numFormat.format(stats.mobsSlain),
                                icon = Icons.Default.LocalFireDepartment,
                                accentColor = RedstoneRed,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Hours Played",
                                value = "${stats.playtimeHours}h",
                                icon = Icons.Default.HourglassTop,
                                accentColor = GoldAccent,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            StatCard(
                                title = "Worlds Hosted",
                                value = "${stats.worldsHosted}",
                                icon = Icons.Default.MilitaryTech,
                                accentColor = GoldAccent,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Friend Sessions Joined",
                                value = "${stats.friendSessionsJoined}",
                                icon = Icons.Default.Groups,
                                accentColor = DiamondCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            StatCard(
                                title = "Netherite Upgrades",
                                value = "${stats.netheriteCrafted}",
                                icon = Icons.Default.Shield,
                                accentColor = NetherPurple,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "PvP K/D Ratio",
                                value = String.format(Locale.US, "%.2f", stats.kdRatio),
                                icon = Icons.Default.SportsKabaddi,
                                accentColor = Color(0xFFF97316),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Log Session Action Card
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DiamondCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Log Today's Play Session",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Record blocks, diamonds, or mobs from your session.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { showLogSessionDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DiamondCyan, contentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("log_session_btn")
                            ) {
                                Text("Log Haul", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                // Leaderboards
                items(leaderboards) { entry ->
                    LeaderboardCard(entry = entry)
                }
            }
        }

        // Edit Profile Dialog
        if (showEditProfileDialog) {
            EditProfileDialog(
                currentStats = stats,
                onDismiss = { showEditProfileDialog = false },
                onSave = { newIgn, newRoblox, newTitle, newGuild ->
                    viewModel.updatePlayerProfile(newIgn, newRoblox, newTitle, newGuild)
                    showEditProfileDialog = false
                }
            )
        }

        // Log Session Dialog
        if (showLogSessionDialog) {
            LogSessionDialog(
                onDismiss = { showLogSessionDialog = false },
                onSave = { mined, slain, diamonds, hours, kills, deaths ->
                    viewModel.logPlaySession(mined, slain, diamonds, hours, kills, deaths)
                    showLogSessionDialog = false
                }
            )
        }
    }
}

@Composable
fun LeaderboardCard(entry: com.example.data.model.LeaderboardEntry) {
    val medalColor = when (entry.rank) {
        1 -> GoldAccent
        2 -> Color(0xFFCBD5E1)
        3 -> Color(0xFFD97706)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = medalColor.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${entry.rank}",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = medalColor
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                MinecraftAvatar(username = entry.ign, sizeDp = 36)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = entry.ign, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = entry.metricLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Text(
                    text = entry.metricValue,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = EmeraldLight,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    currentStats: PlayerStats,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var ign by remember { mutableStateOf(currentStats.ign) }
    var robloxUsername by remember { mutableStateOf(currentStats.robloxUsername) }
    var title by remember { mutableStateOf(currentStats.title) }
    var guild by remember { mutableStateOf(currentStats.guild) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Gaming Identity", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = ign,
                    onValueChange = { ign = it },
                    label = { Text("Minecraft IGN / Gamertag") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_ign_input")
                )
                OutlinedTextField(
                    value = robloxUsername,
                    onValueChange = { robloxUsername = it },
                    label = { Text("Roblox Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_roblox_input")
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Custom Title / Rank") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = guild,
                    onValueChange = { guild = it },
                    label = { Text("Squad / Clan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(ign, robloxUsername, title, guild) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                modifier = Modifier.testTag("save_profile_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun LogSessionDialog(
    onDismiss: () -> Unit,
    onSave: (Long, Int, Int, Int, Int, Int) -> Unit
) {
    var minedText by remember { mutableStateOf("") }
    var diamondsText by remember { mutableStateOf("") }
    var slainText by remember { mutableStateOf("") }
    var hoursText by remember { mutableStateOf("1") }
    var killsText by remember { mutableStateOf("0") }
    var deathsText by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Play Session", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = minedText,
                    onValueChange = { minedText = it },
                    label = { Text("Blocks Mined") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("e.g. 1500") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = diamondsText,
                    onValueChange = { diamondsText = it },
                    label = { Text("Diamonds Found") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("e.g. 18") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = slainText,
                    onValueChange = { slainText = it },
                    label = { Text("Mobs Slain") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text("e.g. 45") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { hoursText = it },
                        label = { Text("Hours") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = killsText,
                        onValueChange = { killsText = it },
                        label = { Text("PvP Kills") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mined = minedText.toLongOrNull() ?: 0L
                    val diamonds = diamondsText.toIntOrNull() ?: 0
                    val slain = slainText.toIntOrNull() ?: 0
                    val hours = hoursText.toIntOrNull() ?: 0
                    val kills = killsText.toIntOrNull() ?: 0
                    val deaths = deathsText.toIntOrNull() ?: 0
                    onSave(mined, slain, diamonds, hours, kills, deaths)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                modifier = Modifier.testTag("submit_session_btn")
            ) {
                Text("Log Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
