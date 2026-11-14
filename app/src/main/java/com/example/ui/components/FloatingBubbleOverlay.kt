package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Lobby
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RobloxBlue
import com.example.ui.theme.RobloxGlow
import kotlin.math.roundToInt

@Composable
fun FloatingBubbleOverlay(
    visible: Boolean,
    pinnedLobby: Lobby?,
    isSystemOverlayRunning: Boolean,
    onOpenGame: (Lobby) -> Unit,
    onStartSystemOverlay: (Lobby) -> Unit,
    onStopSystemOverlay: () -> Unit,
    onHideBubble: () -> Unit,
    onOpenMessenger: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (!visible || pinnedLobby == null) return

    val context = LocalContext.current
    val density = LocalDensity.current

    var isExpanded by remember { mutableStateOf(false) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Start floating overlay regardless of notification permission; FGS handles gracefully
        if (Settings.canDrawOverlays(context)) {
            onStartSystemOverlay(pinnedLobby)
        } else {
            showOverlayPermissionDialog = true
        }
    }

    fun requestSystemFloat() {
        if (!Settings.canDrawOverlays(context)) {
            showOverlayPermissionDialog = true
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        onStartSystemOverlay(pinnedLobby)
    }

    val isRoblox = pinnedLobby.gamePlatform.equals("Roblox", ignoreCase = true)
    val accentColor = if (isRoblox) RobloxBlue else EmeraldPrimary
    val accentGlow = if (isRoblox) RobloxGlow else EmeraldGlow

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val bubbleSizePx = with(density) { 60.dp.toPx() }
        val edgeMarginPx = with(density) { 12.dp.toPx() }
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }

        var initialized by remember { mutableStateOf(false) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }
        var isDragging by remember { mutableStateOf(false) }

        LaunchedEffect(maxWidthPx, maxHeightPx) {
            if (!initialized && maxWidthPx > 0f) {
                offsetX = (maxWidthPx - bubbleSizePx - edgeMarginPx).coerceAtLeast(edgeMarginPx)
                offsetY = (maxHeightPx * 0.42f).coerceIn(edgeMarginPx, (maxHeightPx - bubbleSizePx - edgeMarginPx).coerceAtLeast(edgeMarginPx))
                initialized = true
            }
        }

        val animatedOffset by animateIntOffsetAsState(
            targetValue = IntOffset(offsetX.roundToInt(), offsetY.roundToInt()),
            animationSpec = spring(stiffness = if (isDragging) Spring.StiffnessHigh else Spring.StiffnessMediumLow),
            label = "floating_bubble_offset"
        )

        // Expanded Floating HUD Panel (centered horizontally near middle-top for clear readability)
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 20.dp)
        ) {
            Surface(
                color = Color(0xF50A0E17),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.85f)),
                shadowElevation = 18.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("floating_hud_expanded_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FLOATING LOBBY HUD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = accentGlow
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isExpanded = false },
                                modifier = Modifier.size(34.dp).testTag("floating_minimize_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Minimize,
                                    contentDescription = "Minimize to floating bubble",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                            IconButton(
                                onClick = {
                                    isExpanded = false
                                    onHideBubble()
                                },
                                modifier = Modifier.size(34.dp).testTag("floating_hide_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hide floating bubble",
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pinned World Summary
                    Surface(
                        color = Color(0xFF111C2D),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                PlatformBadge(gamePlatform = pinnedLobby.gamePlatform)
                                Text(
                                    text = "${pinnedLobby.currentPlayers}/${pinnedLobby.maxPlayers} Players • ${pinnedLobby.inviteCode}",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = pinnedLobby.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isRoblox) {
                                    "Host: @${pinnedLobby.hostUsername} • Place ID: ${pinnedLobby.robloxPlaceId}"
                                } else {
                                    "Host: ${pinnedLobby.hostUsername} • Minecraft > Play > Worlds"
                                },
                                fontSize = 11.sp,
                                color = accentGlow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // System Float Over Games Toggle (Minecraft / Roblox)
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSystemOverlayRunning) DiamondCyan.copy(alpha = 0.7f) else Color(0xFF334155)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Float Over Minecraft / Roblox",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isSystemOverlayRunning) {
                                        "Active! Bubble stays on top over games."
                                    } else {
                                        "Keep a draggable bubble floating while playing."
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Switch(
                                checked = isSystemOverlayRunning,
                                onCheckedChange = { enabled ->
                                    if (enabled) requestSystemFloat() else onStopSystemOverlay()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = accentColor
                                ),
                                modifier = Modifier.testTag("system_overlay_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Launch Game + Minimize Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (!isSystemOverlayRunning && Settings.canDrawOverlays(context)) {
                                    onStartSystemOverlay(pinnedLobby)
                                }
                                onOpenGame(pinnedLobby)
                                isExpanded = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor,
                                contentColor = if (isRoblox) Color.White else Color.Black
                            ),
                            modifier = Modifier.weight(1f).testTag("floating_launch_game_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isRoblox) "Open Roblox" else "Open Minecraft",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                isExpanded = false
                                onOpenMessenger()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("floating_open_messenger_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Draggable Collapsed Floating Bubble
        if (!isExpanded && initialized) {
            Box(
                modifier = Modifier
                    .offset { animatedOffset }
                    .size(60.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFF1E293B), Color(0xFF0A0E17))
                        )
                    )
                    .border(2.5.dp, accentColor, CircleShape)
                    .pointerInput(maxWidthPx, maxHeightPx) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                // Snap horizontally to nearest screen edge
                                offsetX = if (offsetX + bubbleSizePx / 2f < maxWidthPx / 2f) {
                                    edgeMarginPx
                                } else {
                                    (maxWidthPx - bubbleSizePx - edgeMarginPx).coerceAtLeast(edgeMarginPx)
                                }
                            },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX = (offsetX + dragAmount.x).coerceIn(
                                    edgeMarginPx,
                                    (maxWidthPx - bubbleSizePx - edgeMarginPx).coerceAtLeast(edgeMarginPx)
                                )
                                offsetY = (offsetY + dragAmount.y).coerceIn(
                                    edgeMarginPx,
                                    (maxHeightPx - bubbleSizePx - edgeMarginPx).coerceAtLeast(edgeMarginPx)
                                )
                            }
                        )
                    }
                    .clickable { isExpanded = true }
                    .testTag("floating_bubble_btn"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isRoblox) "RBX" else "MC",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = accentGlow,
                        lineHeight = 11.sp
                    )
                    Text(
                        text = "${pinnedLobby.currentPlayers}/${pinnedLobby.maxPlayers}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 13.sp
                    )
                    if (isSystemOverlayRunning) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(DiamondCyan)
                        )
                    }
                }
            }
        }
    }

    // Overlay Permission Explanation Dialog
    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Allow Floating Over Games?", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "To keep your FreebookLive multiplayer bubble floating on top of Minecraft or Roblox, allow 'Display over other apps' in Android Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverlayPermissionDialog = false
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Settings", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayPermissionDialog = false }) {
                    Text("Not Now")
                }
            }
        )
    }
}
