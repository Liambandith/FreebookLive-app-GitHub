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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Waypoint
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NetherPurple
import com.example.ui.theme.RedstoneRed
import com.example.ui.theme.RobloxBlue
import com.example.ui.theme.RobloxGlow
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinecraftToolsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTool by remember { mutableIntStateOf(0) } // 0: Mobile Data & Gamertags, 1: Nether Calculator
    val pingState by viewModel.pingTestState.collectAsState()
    val waypoints by viewModel.allWaypoints.collectAsState()

    var showAddWaypointDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTool,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = EmeraldPrimary
        ) {
            Tab(
                selected = selectedTool == 0,
                onClick = { selectedTool = 0 },
                text = { Text("Host & Join Guide", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTool == 1,
                onClick = { selectedTool = 1 },
                text = { Text("Nether Portal Coords", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTool == 0) {
                // Minecraft & Roblox Host / Join Guide Card
                item {
                    MobileDataGuideCard()
                }

                // Gamertag / Roblox Place ID Online Check Card
                item {
                    GamertagOnlineCheckCard(
                        pingState = pingState,
                        onTestGamertag = { gamertag -> viewModel.testHostReachability(gamertag, 19132) }
                    )
                }
            } else {
                // Nether Portal Calculator & Waypoints Tool
                item {
                    NetherPortalCalculatorCard(
                        onSaveWaypoint = { name, ox, oy, oz, type ->
                            viewModel.addWaypoint(name, ox, oy, oz, type)
                        }
                    )
                }

                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Saved Coordinate Waypoints (${waypoints.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = { showAddWaypointDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp).testTag("add_waypoint_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Pin", fontSize = 11.sp)
                        }
                    }
                }

                items(waypoints, key = { it.id }) { wp ->
                    WaypointCard(
                        waypoint = wp,
                        onDelete = { viewModel.deleteWaypoint(wp) }
                    )
                }
            }
        }

        if (showAddWaypointDialog) {
            AddWaypointDialog(
                onDismiss = { showAddWaypointDialog = false },
                onAdd = { name, ox, oy, oz, type ->
                    viewModel.addWaypoint(name, ox, oy, oz, type)
                    showAddWaypointDialog = false
                }
            )
        }
    }
}

@Composable
fun MobileDataGuideCard() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Minecraft Card
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldPrimary.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = EmeraldPrimary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.SignalCellularAlt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Minecraft: Hosting & Joining",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Hosting
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("HOSTING YOUR OWN WORLD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        Text(
                            "Open your world, then turn on Multiplayer Game and Visible to LAN Players. (For remote 4G/5G without Wi-Fi, turn ON Settings > General > Use Cellular Data.)",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                // Joining
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("JOINING A FRIEND'S WORLD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldLight)
                        Text(
                            "Press Join here in FreebookLive, then open Minecraft > Play > Worlds and tap the world.",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Roblox Card
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, RobloxBlue.copy(alpha = 0.65f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = RobloxBlue,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Roblox: Hosting & Joining",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("HOSTING & JOINING ROBLOX WORLDS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RobloxGlow)
                        Text(
                            "Tap 'Host Your World' -> select Roblox -> choose an experience preset or paste your Place ID / Private Server Code. Friends press Join to open Roblox directly into your lobby!",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GamertagOnlineCheckCard(
    pingState: com.example.ui.viewmodel.PingTestState,
    onTestGamertag: (String) -> Unit
) {
    var gamertagInput by remember { mutableStateOf("Alex_Overlord") }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DiamondCyan.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = DiamondCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Username / Place ID Status Check",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Check a Minecraft Gamertag or Roblox Place ID (e.g. 4924922222) before joining.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = gamertagInput,
                onValueChange = { gamertagInput = it },
                label = { Text("Gamertag / Roblox Username / Place ID") },
                placeholder = { Text("e.g. Alex_Overlord or 4924922222") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("ping_address_field")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onTestGamertag(gamertagInput) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("execute_ping_btn")
            ) {
                Text("Verify Online Session", fontWeight = FontWeight.Bold)
            }

            // Results Display
            if (pingState.isLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Checking online status of $gamertagInput...", fontSize = 12.sp)
                }
            } else if (pingState.result != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "SESSION VERIFIED",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = EmeraldLight
                            )
                            Text(
                                text = pingState.result.motd?.clean ?: "Ready to join!",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NetherPortalCalculatorCard(
    onSaveWaypoint: (String, Int, Int, Int, String) -> Unit
) {
    var overworldXText by remember { mutableStateOf("1000") }
    var overworldYText by remember { mutableStateOf("64") }
    var overworldZText by remember { mutableStateOf("-800") }

    val ox = overworldXText.toIntOrNull() ?: 0
    val oy = overworldYText.toIntOrNull() ?: 64
    val oz = overworldZText.toIntOrNull() ?: 0

    val nx = ox / 8
    val ny = oy
    val nz = oz / 8

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, NetherPurple.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = NetherPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nether Portal 8:1 Math Calculator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "1 Block in the Nether equals 8 Blocks in the Overworld. Link portals flawlessly in your survival world!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text("Overworld Coordinates (X, Y, Z)", style = MaterialTheme.typography.labelMedium, color = EmeraldLight)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = overworldXText,
                    onValueChange = { overworldXText = it },
                    label = { Text("X") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = overworldYText,
                    onValueChange = { overworldYText = it },
                    label = { Text("Y") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = overworldZText,
                    onValueChange = { overworldZText = it },
                    label = { Text("Z") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calculated Nether Box
            Surface(
                color = Color(0xFF1E1026),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, NetherPurple),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "BUILD NETHER PORTAL AT:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NetherPurple
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "X: $nx   Y: $ny   Z: $nz",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onSaveWaypoint("Portal at ($ox, $oz)", ox, oy, oz, "Portal")
                },
                colors = ButtonDefaults.buttonColors(containerColor = NetherPurple, contentColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("save_portal_calc_btn")
            ) {
                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Coordinates as Waypoint Pin")
            }
        }
    }
}

@Composable
fun WaypointCard(
    waypoint: Waypoint,
    onDelete: () -> Unit,
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
            modifier = Modifier.padding(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = EmeraldLight,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = waypoint.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Overworld: ${waypoint.overworldX}, ${waypoint.overworldY}, ${waypoint.overworldZ}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = EmeraldGlow
                )
                Text(
                    text = "Nether: ${waypoint.netherX}, ${waypoint.netherY}, ${waypoint.netherZ}",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NetherPurple
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun AddWaypointDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Int, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var xText by remember { mutableStateOf("0") }
    var yText by remember { mutableStateOf("64") }
    var zText by remember { mutableStateOf("0") }
    var type by remember { mutableStateOf("Base") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Coordinate Waypoint", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Waypoint Name") },
                    placeholder = { Text("e.g. End Portal / Mob Farm") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = xText,
                        onValueChange = { xText = it },
                        label = { Text("X") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = yText,
                        onValueChange = { yText = it },
                        label = { Text("Y") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = zText,
                        onValueChange = { zText = it },
                        label = { Text("Z") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val x = xText.toIntOrNull() ?: 0
                    val y = yText.toIntOrNull() ?: 64
                    val z = zText.toIntOrNull() ?: 0
                    onAdd(name.ifBlank { "Waypoint" }, x, y, z, type)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
            ) {
                Text("Save Pin")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
