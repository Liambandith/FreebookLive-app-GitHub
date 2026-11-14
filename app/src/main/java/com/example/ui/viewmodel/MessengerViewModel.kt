package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatConversation
import com.example.data.model.ChatMessage
import com.example.data.model.SquadMessage
import com.example.data.model.SquadRoom
import com.example.data.repository.MessengerRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@OptIn(ExperimentalCoroutinesApi::class)
class MessengerViewModel(
    private val repository: MessengerRepository,
    val currentUserId: String,
    val currentUserName: String
) : ViewModel() {

    private val _selectedConversationId = MutableStateFlow<String?>(null)
    val selectedConversationId: StateFlow<String?> = _selectedConversationId.asStateFlow()

    private val _selectedSquadRoomId = MutableStateFlow<String?>(null)
    val selectedSquadRoomId: StateFlow<String?> = _selectedSquadRoomId.asStateFlow()

    private val _messengerEvents = MutableSharedFlow<String>()
    val messengerEvents: SharedFlow<String> = _messengerEvents.asSharedFlow()

    val conversationsState: StateFlow<UiState<List<ChatConversation>>> =
        repository.observeConversations(currentUserId)
            .map<List<ChatConversation>, UiState<List<ChatConversation>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing conversations", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load conversations"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val squadRoomsState: StateFlow<UiState<List<SquadRoom>>> =
        repository.observeSquadRooms(currentUserId)
            .map<List<SquadRoom>, UiState<List<SquadRoom>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing squad rooms", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to load squad rooms"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    val unreadTotalCount: StateFlow<Int> = conversationsState
        .map { state ->
            when (state) {
                is UiState.Success -> state.data.sumOf { it.unreadCount.toInt().coerceAtLeast(0) }
                else -> 0
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = 0
        )

    val activeMessages: StateFlow<List<ChatMessage>> = _selectedConversationId
        .flatMapLatest { convId ->
            if (convId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeMessages(currentUserId, convId)
                    .catch { error ->
                        val firestoreError = generateSequence(error) { it.cause }
                            .filterIsInstance<FirebaseFirestoreException>()
                            .firstOrNull()
                        if (firestoreError == null && error is CancellationException) throw error
                        Log.w(TAG, "Error observing messages", firestoreError ?: error)
                        emit(emptyList())
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    val activeSquadMessages: StateFlow<List<SquadMessage>> = _selectedSquadRoomId
        .flatMapLatest { roomId ->
            if (roomId.isNullOrBlank()) {
                flowOf(emptyList())
            } else {
                repository.observeSquadMessages(currentUserId, roomId)
                    .catch { error ->
                        val firestoreError = generateSequence(error) { it.cause }
                            .filterIsInstance<FirebaseFirestoreException>()
                            .firstOrNull()
                        if (firestoreError == null && error is CancellationException) throw error
                        Log.w(TAG, "Error observing squad messages", firestoreError ?: error)
                        emit(emptyList())
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.seedStarterConversationsIfEmpty()
        }
    }

    fun selectConversation(conversationId: String?) {
        _selectedSquadRoomId.value = null
        _selectedConversationId.value = conversationId
        if (!conversationId.isNullOrBlank()) {
            viewModelScope.launch {
                repository.markConversationRead(conversationId)
            }
        }
    }

    fun selectSquadRoom(roomId: String?) {
        _selectedConversationId.value = null
        _selectedSquadRoomId.value = roomId
    }

    fun openOrCreateFriendChat(
        peerUsername: String,
        gamePlatform: String,
        title: String = peerUsername,
        lobbyId: String = "",
        inviteCode: String = "",
        isGroup: Boolean = false
    ) {
        viewModelScope.launch {
            repository.openOrCreateConversation(
                peerUsername = peerUsername,
                gamePlatform = gamePlatform,
                title = title,
                lobbyId = lobbyId,
                inviteCode = inviteCode,
                isGroup = isGroup
            ).onSuccess { convId ->
                selectConversation(convId)
            }.onFailure { err ->
                _messengerEvents.emit(err.localizedMessage ?: "Could not open chat")
            }
        }
    }

    fun sendMessage(
        text: String,
        gamePlatform: String,
        lobbyInviteCode: String = "",
        lobbyTitle: String = ""
    ) {
        val convId = _selectedConversationId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                senderName = currentUserName,
                text = text,
                gamePlatform = gamePlatform,
                lobbyInviteCode = lobbyInviteCode,
                lobbyTitle = lobbyTitle
            ).onFailure { err ->
                _messengerEvents.emit(err.localizedMessage ?: "Failed to send message")
            }
        }
    }

    fun createSquadRoom(title: String, gamePlatform: String, roomCode: String) {
        viewModelScope.launch {
            repository.createSquadRoom(
                ownerName = currentUserName,
                title = title,
                gamePlatform = gamePlatform,
                roomCode = roomCode
            ).onSuccess { createdId ->
                selectSquadRoom(createdId)
                _messengerEvents.emit("Squad room $createdId created! Share code with friends.")
            }.onFailure { err ->
                _messengerEvents.emit(err.localizedMessage ?: "Failed to create squad room")
            }
        }
    }

    fun joinSquadRoomByCode(roomCode: String) {
        val trimmed = roomCode.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.joinSquadRoomByCode(trimmed)
                .onSuccess { joinedId ->
                    selectSquadRoom(joinedId)
                    _messengerEvents.emit("Joined squad room $joinedId!")
                }
                .onFailure { err ->
                    _messengerEvents.emit(err.localizedMessage ?: "Room code not found or inaccessible")
                }
        }
    }

    fun sendSquadMessage(room: SquadRoom, text: String, lobbyInviteCode: String = "") {
        viewModelScope.launch {
            repository.sendSquadMessage(
                room = room,
                senderName = currentUserName,
                text = text,
                lobbyInviteCode = lobbyInviteCode
            ).onFailure { err ->
                _messengerEvents.emit(err.localizedMessage ?: "Failed to send squad message")
            }
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            if (_selectedConversationId.value == conversationId) {
                _selectedConversationId.value = null
            }
            repository.deleteConversation(conversationId)
        }
    }

    private companion object {
        const val TAG = "MessengerVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
