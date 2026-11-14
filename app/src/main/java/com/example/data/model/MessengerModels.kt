package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ChatConversation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val peerUsername: String = "",
    val gamePlatform: String = "Minecraft",
    val lobbyId: String = "",
    val inviteCode: String = "",
    val lastMessage: String = "",
    val unreadCount: Long = 0L,
    val isGroup: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val conversationId: String = "",
    val senderName: String = "",
    val isFromMe: Boolean = true,
    val text: String = "",
    val gamePlatform: String = "Minecraft",
    val lobbyInviteCode: String = "",
    val lobbyTitle: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SquadRoom(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val title: String = "",
    val gamePlatform: String = "Minecraft",
    val roomCode: String = "",
    val memberUids: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastSenderName: String = "",
    val isJoinableByCode: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SquadMessage(
    @DocumentId val id: String = "",
    val roomId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val text: String = "",
    val gamePlatform: String = "Minecraft",
    val lobbyInviteCode: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
)
