package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.Lobby
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

@Composable
fun MinecraftAvatar(
    username: String,
    modifier: Modifier = Modifier,
    sizeDp: Int = 44,
    gamePlatform: String = "Minecraft"
) {
    val isRoblox = gamePlatform.equals("Roblox", ignoreCase = true)
    val context = LocalContext.current
    val borderColor = if (isRoblox) RobloxBlue.copy(alpha = 0.7f) else EmeraldPrimary.copy(alpha = 0.6f)
    val bgColor = if (isRoblox) Color(0xFF0F2942) else Color(0xFF1E293B)
    val initial = username.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "P"

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isRoblox) {
            Text(
                text = initial,
                fontWeight = FontWeight.Black,
                fontSize = (sizeDp * 0.42f).sp,
                color = RobloxGlow
            )
        } else {
            val avatarUrl = "https://mc-heads.net/avatar/$username/128"
            Text(
                text = initial,
                fontWeight = FontWeight.Bold,
                fontSize = (sizeDp * 0.4f).sp,
                color = EmeraldGlow
            )
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Minecraft skin of $username",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(sizeDp.dp)
            )
        }
    }
}

@Composable
fun PlatformBadge(gamePlatform: String) {
    val isRoblox = gamePlatform.equals("Roblox", ignoreCase = true)
    val bg = if (isRoblox) RobloxBlue.copy(alpha = 0.2f) else EmeraldDark.copy(alpha = 0.35f)
    val border = if (isRoblox) RobloxBlue else EmeraldPrimary
    val text = if (isRoblox) RobloxGlow else EmeraldGlow

    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, border),
        modifier = Modifier.padding(end = 6.dp)
    ) {
        Text(
            text = gamePlatform.uppercase(),
            color = text,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun EditionBadge(edition: String) {
    val (badgeBg, badgeBorder, badgeText) = when (edition.lowercase()) {
        "java" -> Triple(Color(0xFF831843).copy(alpha = 0.4f), Color(0xFFF43F5E), Color(0xFFFDA4AF))
        "bedrock" -> Triple(Color(0xFF064E3B).copy(alpha = 0.4f), EmeraldLight, EmeraldGlow)
        "roleplay", "obby", "action rpg", "custom world" -> Triple(Color(0xFF1E3A8A).copy(alpha = 0.4f), RobloxBlue, RobloxGlow)
        else -> Triple(Color(0xFF0E7490).copy(alpha = 0.4f), DiamondCyan, Color(0xFFBAE6FD))
    }

    Surface(
        color = badgeBg,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeBorder),
        modifier = Modifier.padding(end = 6.dp)
    ) {
        Text(
            text = edition.uppercase(),
            color = badgeText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ConnectionTypeBadge(connectionType: String) {
    val (color, label) = when {
        connectionType.contains("LAN") || connectionType.contains("Worlds") -> EmeraldLight to "LAN / WORLDS"
        connectionType.contains("Roblox") -> RobloxGlow to "ROBLOX LOBBY"
        connectionType.contains("Private") -> GoldAccent to "PRIVATE LINK"
        connectionType.contains("Mobile Data") || connectionType.contains("4G") || connectionType.contains("5G") -> EmeraldLight to "MOBILE DATA"
        connectionType.contains("Gamertag") -> GoldAccent to "GAMERTAG JOIN"
        else -> DiamondCyan to connectionType.uppercase().take(16)
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
fun PingBadge(pingMs: Int) {
    val pingColor = when {
        pingMs < 40 -> EmeraldLight
        pingMs < 100 -> GoldAccent
        else -> RedstoneRed
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.SignalCellularAlt,
            contentDescription = "Ping",
            tint = pingColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = "${pingMs}ms",
            color = pingColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun LobbyCard(
    lobby: Lobby,
    onLobbyClick: () -> Unit,
    onJoinClick: () -> Unit,
    onBookmarkToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isRoblox = lobby.gamePlatform.equals("Roblox", ignoreCase = true)
    val accentColor = if (isRoblox) RobloxBlue else EmeraldPrimary
    val accentGlow = if (isRoblox) RobloxGlow else EmeraldGlow

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (lobby.isBookmarked) accentColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onLobbyClick() }
            .testTag("lobby_card_${lobby.id}")
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(14.dp)
        ) {
            // Top badges row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlatformBadge(gamePlatform = lobby.gamePlatform)
                    EditionBadge(edition = lobby.edition)
                    if (lobby.isMyHostedWorld) {
                        Surface(
                            color = GoldAccent.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "MY WORLD",
                                color = GoldAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.size(32.dp).testTag("bookmark_btn_${lobby.id}")
                ) {
                    Icon(
                        imageVector = if (lobby.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark World",
                        tint = if (lobby.isBookmarked) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // World / Experience Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                MinecraftAvatar(
                    username = lobby.hostUsername,
                    sizeDp = 36,
                    gamePlatform = lobby.gamePlatform
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lobby.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isRoblox) "Host: @${lobby.hostUsername}" else "Host: ${lobby.hostUsername}",
                            fontSize = 11.sp,
                            color = accentGlow,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        ConnectionTypeBadge(connectionType = lobby.connectionType)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Announcement / MOTD
            Text(
                text = lobby.motd.replace("§.", ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Connection & Mode Strip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isRoblox) Icons.Default.Public else Icons.Default.VideogameAsset,
                        contentDescription = null,
                        tint = accentGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRoblox) "Place ID: ${lobby.robloxPlaceId.ifBlank { lobby.inviteCode }}" else "Play > Worlds (${lobby.inviteCode})",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0)
                    )
                }

                Text(
                    text = "${lobby.gameMode} • ${lobby.difficulty}",
                    fontSize = 11.sp,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Row: Players, Ping, Join Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Players",
                        tint = accentGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${lobby.currentPlayers}/${lobby.maxPlayers}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                    PingBadge(pingMs = lobby.pingMs)
                }

                ElevatedButton(
                    onClick = onJoinClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = accentColor,
                        contentColor = if (isRoblox) Color.White else Color.Black
                    ),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("join_lobby_${lobby.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Join",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
