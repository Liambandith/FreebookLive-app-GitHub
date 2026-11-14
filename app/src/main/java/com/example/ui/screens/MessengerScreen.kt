package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatConversation
import com.example.data.model.ChatMessage
import com.example.data.model.Lobby
import com.example.data.model.SquadMessage
import com.example.data.model.SquadRoom
import com.example.ui.components.MinecraftAvatar
import com.example.ui.components.PlatformBadge
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.RedstoneRed
import com.example.ui.theme.RobloxBlue
import com.example.ui.theme.RobloxGlow
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MessengerViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessengerScreen(
    messengerViewModel: MessengerViewModel,
    mainViewModel: MainViewModel,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val conversationsState by messengerViewModel.conversationsState.collectAsState()
    val squadRoomsState by messengerViewModel.squadRoomsState.collectAsState()
    val selectedConversationId by messengerViewModel.selectedConversationId.collectAsState()
    val selectedSquadRoomId by messengerViewModel.selectedSquadRoomId.collectAsState()
    val activeMessages by messengerViewModel.activeMessages.collectAsState()
    val activeSquadMessages by messengerViewModel.activeSquadMessages.collectAsState()
    val allLobbies by mainViewModel.filteredLobbies.collectAsState()
    val allFriends by mainViewModel.allFriends.collectAsState()

    var showNewChatDialog by remember { mutableStateOf(false) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var showJoinRoomDialog by remember { mutableStateOf(false) }

    val activeConversation = (conversationsState as? UiState.Success)?.data
        ?.firstOrNull { it.id == selectedConversationId }
    val activeSquadRoom = (squadRoomsState as? UiState.Success)?.data
        ?.firstOrNull { it.id == selectedSquadRoomId }

    when {
        activeConversation != null -> {
            DirectChatThreadView(
                conversation = activeConversation,
                messages = activeMessages,
                availableLobbies = allLobbies,
                onBack = { messengerViewModel.selectConversation(null) },
                onSendMessage = { text, inviteCode, lobbyTitle ->
                    messengerViewModel.sendMessage(
                        text = text,
                        gamePlatform = activeConversation.gamePlatform,
                        lobbyInviteCode = inviteCode,
                        lobbyTitle = lobbyTitle
                    )
                },
                onJoinWorldCode = { code ->
                    mainViewModel.joinLobbyByCode(code)
                    onBack()
                },
                modifier = modifier
            )
        }

        activeSquadRoom != null -> {
            SquadRoomThreadView(
                room = activeSquadRoom,
                currentUserId = messengerViewModel.currentUserId,
                messages = activeSquadMessages,
                availableLobbies = allLobbies,
                onBack = { messengerViewModel.selectSquadRoom(null) },
                onSendMessage = { text, inviteCode ->
                    messengerViewModel.sendSquadMessage(
                        room = activeSquadRoom,
                        text = text,
                        lobbyInviteCode = inviteCode
                    )
                },
                onJoinWorldCode = { code ->
                    mainViewModel.joinLobbyByCode(code)
                    onBack()
                },
                modifier = modifier
            )
        }

        else -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "FreebookLive Messenger",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Signed in as ${messengerViewModel.currentUserName}",
                                    fontSize = 11.sp,
                                    color = EmeraldGlow
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("messenger_back_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Lobbies"
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { showNewChatDialog = true },
                                modifier = Modifier.testTag("messenger_new_chat_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddComment,
                                    contentDescription = "New Chat",
                                    tint = EmeraldPrimary
                                )
                            }
                            IconButton(
                                onClick = onSignOut,
                                modifier = Modifier.testTag("messenger_sign_out_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign Out",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quick Action Buttons for Direct Chat & Live Multi-User Squad Rooms
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { showNewChatDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("start_friend_chat_btn")
                            ) {
                                Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Chat", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { showCreateRoomDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("create_squad_room_btn")
                            ) {
                                Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Host Room", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { showJoinRoomDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("join_squad_room_btn")
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Join Code", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // Live Multi-User Squad Rooms Section
                    item {
                        Text(
                            text = "LIVE SQUAD CHAT ROOMS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = DiamondCyan
                        )
                    }

                    when (val rooms = squadRoomsState) {
                        is UiState.Loading -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = DiamondCyan)
                                }
                            }
                        }
                        is UiState.Error -> {
                            item {
                                Text(
                                    text = rooms.message,
                                    fontSize = 12.sp,
                                    color = RedstoneRed
                                )
                            }
                        }
                        is UiState.Success -> {
                            if (rooms.data.isEmpty()) {
                                item {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Tap 'Host Room' to create a live cross-device squad chat, or 'Join Code' to enter a friend's room code.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                }
                            } else {
                                items(rooms.data, key = { "room_${it.id}" }) { room ->
                                    SquadRoomInboxCard(
                                        room = room,
                                        onClick = { messengerViewModel.selectSquadRoom(room.id) }
                                    )
                                }
                            }
                        }
                    }

                    // Direct Friend & World Conversations Section
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "FRIEND & WORLD MESSAGES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldGlow
                        )
                    }

                    when (val convs = conversationsState) {
                        is UiState.Loading -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = EmeraldPrimary)
                                }
                            }
                        }
                        is UiState.Error -> {
                            item {
                                Text(
                                    text = convs.message,
                                    fontSize = 12.sp,
                                    color = RedstoneRed
                                )
                            }
                        }
                        is UiState.Success -> {
                            if (convs.data.isEmpty()) {
                                item {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "No messages yet. Tap 'New Chat' to message a Minecraft or Roblox friend!",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(14.dp)
                                        )
                                    }
                                }
                            } else {
                                items(convs.data, key = { "conv_${it.id}" }) { conv ->
                                    ConversationInboxCard(
                                        conversation = conv,
                                        onClick = { messengerViewModel.selectConversation(conv.id) },
                                        onDelete = { messengerViewModel.deleteConversation(conv.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewChatDialog) {
        NewChatDialog(
            friendsUsernames = allFriends.map { it.username to it.gamePlatform },
            onDismiss = { showNewChatDialog = false },
            onStartChat = { peerName, platform ->
                showNewChatDialog = false
                messengerViewModel.openOrCreateFriendChat(
                    peerUsername = peerName,
                    gamePlatform = platform
                )
            }
        )
    }

    if (showCreateRoomDialog) {
        CreateSquadRoomDialog(
            onDismiss = { showCreateRoomDialog = false },
            onCreate = { title, platform, code ->
                showCreateRoomDialog = false
                messengerViewModel.createSquadRoom(title, platform, code)
            }
        )
    }

    if (showJoinRoomDialog) {
        JoinSquadRoomDialog(
            onDismiss = { showJoinRoomDialog = false },
            onJoin = { code ->
                showJoinRoomDialog = false
                messengerViewModel.joinSquadRoomByCode(code)
            }
        )
    }
}

@Composable
private fun ConversationInboxCard(
    conversation: ChatConversation,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isRoblox = conversation.gamePlatform.equals("Roblox", ignoreCase = true)
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (conversation.unreadCount > 0) EmeraldPrimary.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("conversation_card_${conversation.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            MinecraftAvatar(
                username = conversation.peerUsername,
                sizeDp = 46,
                gamePlatform = conversation.gamePlatform
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    PlatformBadge(gamePlatform = conversation.gamePlatform)
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = conversation.lastMessage.ifBlank { "Tap to start chatting..." },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatTimestamp(conversation.updatedAt ?: conversation.createdAt),
                    fontSize = 10.sp,
                    color = if (isRoblox) RobloxGlow else EmeraldGlow
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (conversation.unreadCount > 0) {
                        Surface(
                            color = EmeraldPrimary,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${conversation.unreadCount}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete conversation",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SquadRoomInboxCard(
    room: SquadRoom,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF111C2D),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, DiamondCyan.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("squad_room_card_${room.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Surface(
                color = DiamondCyan.copy(alpha = 0.16f),
                shape = CircleShape,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = DiamondCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = room.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = GoldAccent.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = room.roomCode,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${room.lastSenderName}: ${room.lastMessage}",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${room.memberUids.size}/12",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DiamondCyan
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DirectChatThreadView(
    conversation: ChatConversation,
    messages: List<ChatMessage>,
    availableLobbies: List<Lobby>,
    onBack: () -> Unit,
    onSendMessage: (String, String, String) -> Unit,
    onJoinWorldCode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var draftText by remember { mutableStateOf("") }
    var attachedLobby by remember { mutableStateOf<Lobby?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MinecraftAvatar(
                            username = conversation.peerUsername,
                            sizeDp = 34,
                            gamePlatform = conversation.gamePlatform
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = conversation.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${conversation.gamePlatform} • Cloud Synced",
                                fontSize = 11.sp,
                                color = EmeraldGlow
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("chat_thread_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Inbox"
                        )
                    }
                },
                actions = {
                    if (conversation.inviteCode.isNotBlank()) {
                        Button(
                            onClick = { onJoinWorldCode(conversation.inviteCode) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 8.dp).height(34.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Join", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageBubble(
                        senderName = msg.senderName,
                        text = msg.text,
                        isFromMe = msg.isFromMe,
                        gamePlatform = msg.gamePlatform,
                        inviteCode = msg.lobbyInviteCode,
                        lobbyTitle = msg.lobbyTitle,
                        timestamp = msg.createdAt,
                        onJoinWorldCode = onJoinWorldCode
                    )
                }
            }

            // Quick World Invite Attachment Row
            if (availableLobbies.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(availableLobbies.take(4), key = { it.id }) { lobby ->
                        val isSelected = attachedLobby?.id == lobby.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { attachedLobby = if (isSelected) null else lobby },
                            leadingIcon = {
                                Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            label = {
                                Text("Attach ${lobby.inviteCode}", fontSize = 11.sp)
                            }
                        )
                    }
                }
            }

            MessageComposerBar(
                draftText = draftText,
                onDraftChange = { draftText = it },
                attachedCode = attachedLobby?.inviteCode.orEmpty(),
                onSend = {
                    val code = attachedLobby?.inviteCode.orEmpty()
                    val lTitle = attachedLobby?.name.orEmpty()
                    val textToSend = draftText.trim().ifBlank {
                        if (code.isNotBlank()) "Join my $lTitle world! Code: $code" else ""
                    }
                    if (textToSend.isNotBlank()) {
                        onSendMessage(textToSend, code, lTitle)
                        draftText = ""
                        attachedLobby = null
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SquadRoomThreadView(
    room: SquadRoom,
    currentUserId: String,
    messages: List<SquadMessage>,
    availableLobbies: List<Lobby>,
    onBack: () -> Unit,
    onSendMessage: (String, String) -> Unit,
    onJoinWorldCode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var draftText by remember { mutableStateOf("") }
    var attachedLobby by remember { mutableStateOf<Lobby?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = room.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Room Code: ${room.roomCode} • ${room.memberUids.size}/12 online",
                            fontSize = 11.sp,
                            color = DiamondCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("squad_thread_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Inbox"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatMessageBubble(
                        senderName = msg.senderName,
                        text = msg.text,
                        isFromMe = msg.senderUid == currentUserId,
                        gamePlatform = msg.gamePlatform,
                        inviteCode = msg.lobbyInviteCode,
                        lobbyTitle = "",
                        timestamp = msg.createdAt,
                        onJoinWorldCode = onJoinWorldCode
                    )
                }
            }

            if (availableLobbies.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(availableLobbies.take(4), key = { it.id }) { lobby ->
                        val isSelected = attachedLobby?.id == lobby.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { attachedLobby = if (isSelected) null else lobby },
                            leadingIcon = {
                                Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            label = {
                                Text("Invite ${lobby.inviteCode}", fontSize = 11.sp)
                            }
                        )
                    }
                }
            }

            MessageComposerBar(
                draftText = draftText,
                onDraftChange = { draftText = it },
                attachedCode = attachedLobby?.inviteCode.orEmpty(),
                onSend = {
                    val code = attachedLobby?.inviteCode.orEmpty()
                    val textToSend = draftText.trim().ifBlank {
                        if (code.isNotBlank()) "Join my squad world! Code: $code" else ""
                    }
                    if (textToSend.isNotBlank()) {
                        onSendMessage(textToSend, code)
                        draftText = ""
                        attachedLobby = null
                    }
                }
            )
        }
    }
}

@Composable
private fun ChatMessageBubble(
    senderName: String,
    text: String,
    isFromMe: Boolean,
    gamePlatform: String,
    inviteCode: String,
    lobbyTitle: String,
    timestamp: Timestamp?,
    onJoinWorldCode: (String) -> Unit
) {
    val isRoblox = gamePlatform.equals("Roblox", ignoreCase = true)
    val bubbleColor = when {
        isFromMe && isRoblox -> RobloxBlue.copy(alpha = 0.25f)
        isFromMe -> EmeraldPrimary.copy(alpha = 0.2f)
        else -> Color(0xFF1E293B)
    }
    val borderColor = when {
        isFromMe && isRoblox -> RobloxBlue.copy(alpha = 0.7f)
        isFromMe -> EmeraldPrimary.copy(alpha = 0.7f)
        else -> Color(0xFF334155)
    }

    Column(
        horizontalAlignment = if (isFromMe) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = if (isFromMe) "You • ${formatTimestamp(timestamp)}" else "$senderName • ${formatTimestamp(timestamp)}",
            fontSize = 10.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromMe) 16.dp else 4.dp,
                bottomEnd = if (isFromMe) 4.dp else 16.dp
            ),
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    color = Color.White
                )
                if (inviteCode.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF0A0E17),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = lobbyTitle.ifBlank { "World Lobby Invite" },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = inviteCode,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = GoldAccent
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onJoinWorldCode(inviteCode) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary,
                                    contentColor = Color.Black
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Join", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageComposerBar(
    draftText: String,
    onDraftChange: (String) -> Unit,
    attachedCode: String,
    onSend: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = draftText,
                onValueChange = onDraftChange,
                placeholder = {
                    Text(
                        text = if (attachedCode.isNotBlank()) {
                            "Add note for $attachedCode..."
                        } else {
                            "Message your squad..."
                        },
                        fontSize = 13.sp
                    )
                },
                maxLines = 3,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("messenger_input_field")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = EmeraldPrimary,
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                IconButton(
                    onClick = onSend,
                    modifier = Modifier.testTag("messenger_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun NewChatDialog(
    friendsUsernames: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onStartChat: (String, String) -> Unit
) {
    var peerName by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("Minecraft") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Messenger Chat", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (friendsUsernames.isNotEmpty()) {
                    Text("Quick Pick Friend:", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(friendsUsernames) { (name, plat) ->
                            FilterChip(
                                selected = peerName == name,
                                onClick = {
                                    peerName = name
                                    platform = plat
                                },
                                label = { Text(name, fontSize = 11.sp) }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = peerName,
                    onValueChange = { peerName = it },
                    label = { Text("Friend Username / Gamertag") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("new_chat_peer_field")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Minecraft", "Roblox").forEach { plat ->
                        FilterChip(
                            selected = platform == plat,
                            onClick = { platform = plat },
                            label = { Text(plat) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (peerName.isNotBlank()) {
                        onStartChat(peerName, platform)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                modifier = Modifier.testTag("confirm_new_chat_btn")
            ) {
                Text("Open Chat", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CreateSquadRoomDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var platform by remember { mutableStateOf("Minecraft") }
    var code by remember { mutableStateOf("SQD-${(1000..9999).random()}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Host Live Squad Room", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Share this Room Code with friends signed into FreebookLive so everyone can chat live together.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Minecraft", "Roblox").forEach { plat ->
                        FilterChip(
                            selected = platform == plat,
                            onClick = { platform = plat },
                            label = { Text(plat) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Room Name") },
                    placeholder = { Text("e.g. Ender Dragon Raid") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("Shareable Room Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank()) {
                        onCreate(title.ifBlank { "Live Gaming Squad" }, platform, code)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DiamondCyan, contentColor = Color.Black)
            ) {
                Text("Create Room", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun JoinSquadRoomDialog(
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Join Squad Room by Code", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.uppercase() },
                label = { Text("Room Code (e.g. SQD-1024)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (code.isNotBlank()) {
                        onJoin(code)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black)
            ) {
                Text("Join Chat", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun formatTimestamp(ts: Timestamp?): String {
    if (ts == null) return "Now"
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(ts.toDate())
}
