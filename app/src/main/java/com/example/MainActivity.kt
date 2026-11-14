package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.repository.MessengerRepository
import com.example.ui.auth.SignInScreen
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOut
import com.example.ui.components.FloatingBubbleOverlay
import com.example.ui.screens.AccessManagerScreen
import com.example.ui.screens.FriendsPartyScreen
import com.example.ui.screens.LobbyDetailScreen
import com.example.ui.screens.LobbyHubScreen
import com.example.ui.screens.MessengerScreen
import com.example.ui.screens.MinecraftToolsScreen
import com.example.ui.screens.PlayerProfileStatsScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FreebookLiveTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MessengerViewModel
import com.example.ui.viewmodel.NavigationTab
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.merge

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle deep links like freebooklive://lobby/{code}
        intent?.data?.let { uri ->
            if (uri.scheme == "freebooklive" && uri.host == "lobby") {
                val code = uri.lastPathSegment
                if (!code.isNullOrBlank()) {
                    mainViewModel.joinLobbyByCode(code)
                }
            }
        }

        setContent {
            FreebookLiveTheme {
                AppNavigation(mainViewModel = mainViewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(mainViewModel: MainViewModel) {
    val auth = remember { Firebase.auth }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        SignInScreen(
            onAuthSuccess = { /* authStateFlow updates automatically */ }
        )
    } else {
        val uid = user.uid
        val displayName = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "Player"

        key(uid) {
            val messengerViewModel: MessengerViewModel = viewModel(
                key = uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY]) {
                            "APPLICATION_KEY missing from CreationExtras"
                        }
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        MessengerViewModel(
                            repository = MessengerRepository(db, Firebase.auth),
                            currentUserId = uid,
                            currentUserName = displayName
                        )
                    }
                }
            )
            MainAppScreen(
                viewModel = mainViewModel,
                messengerViewModel = messengerViewModel
            )
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    messengerViewModel: MessengerViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedLobby by viewModel.selectedLobby.collectAsState()
    val isFloatingBubbleVisible by viewModel.isFloatingBubbleVisible.collectAsState()
    val pinnedFloatingLobby by viewModel.pinnedFloatingLobby.collectAsState()
    val isSystemOverlayRunning by viewModel.isSystemOverlayRunning.collectAsState()
    val unreadChatCount by messengerViewModel.unreadTotalCount.collectAsState()
    val selectedConvId by messengerViewModel.selectedConversationId.collectAsState()
    val selectedSquadRoomId by messengerViewModel.selectedSquadRoomId.collectAsState()

    var isMessengerOpen by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        merge(viewModel.uiEvents, messengerViewModel.messengerEvents).collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Back handling: step out of chat thread -> Messenger inbox -> selected lobby -> Lobbies tab
    BackHandler(enabled = isMessengerOpen || selectedLobby != null || selectedTab != NavigationTab.LOBBIES) {
        when {
            isMessengerOpen && (selectedConvId != null || selectedSquadRoomId != null) -> {
                messengerViewModel.selectConversation(null)
                messengerViewModel.selectSquadRoom(null)
            }
            isMessengerOpen -> {
                isMessengerOpen = false
            }
            selectedLobby != null -> {
                viewModel.selectLobby(null)
            }
            selectedTab != NavigationTab.LOBBIES -> {
                viewModel.selectTab(NavigationTab.LOBBIES)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!isMessengerOpen && selectedLobby == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    val tabs = listOf(
                        NavigationTab.LOBBIES to Icons.Default.VideogameAsset,
                        NavigationTab.FRIENDS to Icons.Default.Groups,
                        NavigationTab.ACCESS to Icons.Default.Security,
                        NavigationTab.STATS to Icons.Default.MilitaryTech,
                        NavigationTab.TOOLS to Icons.Default.SignalCellularAlt
                    )

                    tabs.forEach { (tab, icon) ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = EmeraldPrimary,
                                indicatorColor = EmeraldPrimary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isMessengerOpen -> {
                    MessengerScreen(
                        messengerViewModel = messengerViewModel,
                        mainViewModel = viewModel,
                        onBack = { isMessengerOpen = false },
                        onSignOut = {
                            signOut(
                                credentialManager = credentialManager,
                                scope = coroutineScope
                            )
                        }
                    )
                }

                selectedLobby != null -> {
                    LobbyDetailScreen(
                        lobby = selectedLobby!!,
                        viewModel = viewModel,
                        onBack = { viewModel.selectLobby(null) },
                        onOpenWorldChat = { lobby ->
                            messengerViewModel.openOrCreateFriendChat(
                                peerUsername = lobby.hostUsername,
                                gamePlatform = lobby.gamePlatform,
                                title = lobby.name,
                                lobbyId = lobby.id,
                                inviteCode = lobby.inviteCode,
                                isGroup = true
                            )
                            isMessengerOpen = true
                        }
                    )
                }

                else -> {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tab_transition"
                    ) { targetTab ->
                        when (targetTab) {
                            NavigationTab.LOBBIES -> LobbyHubScreen(
                                viewModel = viewModel,
                                unreadChatCount = unreadChatCount,
                                onOpenMessenger = { isMessengerOpen = true }
                            )
                            NavigationTab.FRIENDS -> FriendsPartyScreen(
                                viewModel = viewModel,
                                onOpenPartyChat = {
                                    messengerViewModel.openOrCreateFriendChat(
                                        peerUsername = "Party Squad",
                                        gamePlatform = "Minecraft",
                                        title = "Multiplayer Squad Chat",
                                        isGroup = true
                                    )
                                    isMessengerOpen = true
                                },
                                onMessageFriend = { friend ->
                                    messengerViewModel.openOrCreateFriendChat(
                                        peerUsername = friend.username,
                                        gamePlatform = friend.gamePlatform,
                                        title = friend.username,
                                        lobbyId = friend.currentLobbyId
                                    )
                                    isMessengerOpen = true
                                }
                            )
                            NavigationTab.ACCESS -> AccessManagerScreen(viewModel = viewModel)
                            NavigationTab.STATS -> PlayerProfileStatsScreen(viewModel = viewModel)
                            NavigationTab.TOOLS -> MinecraftToolsScreen(viewModel = viewModel)
                        }
                    }
                }
            }

            if (!isMessengerOpen) {
                FloatingBubbleOverlay(
                    visible = isFloatingBubbleVisible,
                    pinnedLobby = pinnedFloatingLobby,
                    isSystemOverlayRunning = isSystemOverlayRunning,
                    onOpenGame = { lobby -> viewModel.launchGameApp(context, lobby) },
                    onStartSystemOverlay = { lobby -> viewModel.startSystemFloatingOverlay(context, lobby) },
                    onStopSystemOverlay = { viewModel.stopSystemFloatingOverlay(context) },
                    onHideBubble = { viewModel.setFloatingBubbleVisible(false) },
                    onOpenMessenger = { isMessengerOpen = true }
                )
            }
        }
    }
}
