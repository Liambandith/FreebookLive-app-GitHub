package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.ChatConversation
import com.example.data.model.ChatMessage
import com.example.data.model.SquadMessage
import com.example.data.model.SquadRoom
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MessengerRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        Firebase.auth
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun sanitizeId(raw: String, prefix: String = "id"): String {
        val cleaned = raw.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
        return cleaned.take(100).ifBlank { "${prefix}_${UUID.randomUUID().toString().take(8)}" }
    }

    private fun normalizePlatform(platform: String): String {
        return when {
            platform.equals("Roblox", ignoreCase = true) -> "Roblox"
            platform.equals("Minecraft", ignoreCase = true) -> "Minecraft"
            else -> "General"
        }
    }

    fun observeConversations(userId: String): Flow<List<ChatConversation>> = flow {
        val path = "users/$userId/conversations"
        emitAll(
            db.collection("users")
                .document(userId)
                .collection("conversations")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ChatConversation::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeMessages(userId: String, conversationId: String): Flow<List<ChatMessage>> = flow {
        val path = "users/$userId/conversations/$conversationId/messages"
        emitAll(
            db.collection("users")
                .document(userId)
                .collection("conversations")
                .document(conversationId)
                .collection("messages")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        ChatMessage::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun seedStarterConversationsIfEmpty(): Result<Unit> {
        return try {
            val uid = requireUserId()
            val convCol = db.collection("users").document(uid).collection("conversations")
            val existing = convCol.limit(1).get().await()
            if (!existing.isEmpty) return Result.success(Unit)

            val starters = listOf(
                Triple(
                    "dm_alex_overlord",
                    mapOf(
                        "userId" to uid,
                        "title" to "Alex_Overlord",
                        "peerUsername" to "Alex_Overlord",
                        "gamePlatform" to "Minecraft",
                        "lobbyId" to "fbl_world_alex",
                        "inviteCode" to "FBL-1001",
                        "lastMessage" to "Multiplayer Game & Visible to LAN Players is ON! Tap Join on my world.",
                        "unreadCount" to 1L,
                        "isGroup" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    mapOf(
                        "userId" to uid,
                        "conversationId" to "dm_alex_overlord",
                        "senderName" to "Alex_Overlord",
                        "isFromMe" to false,
                        "text" to "Multiplayer Game & Visible to LAN Players is ON! Tap Join on my world.",
                        "gamePlatform" to "Minecraft",
                        "lobbyInviteCode" to "FBL-1001",
                        "lobbyTitle" to "Alex's Survival Kingdom",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ),
                Triple(
                    "dm_bloxbuilder_kai",
                    mapOf(
                        "userId" to uid,
                        "title" to "BloxBuilder_Kai",
                        "peerUsername" to "BloxBuilder_Kai",
                        "gamePlatform" to "Roblox",
                        "lobbyId" to "rbx_world_brookhaven",
                        "inviteCode" to "RBX-4924",
                        "lastMessage" to "Squad is meeting at Lake House #14 in Brookhaven! Ready to launch?",
                        "unreadCount" to 1L,
                        "isGroup" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    mapOf(
                        "userId" to uid,
                        "conversationId" to "dm_bloxbuilder_kai",
                        "senderName" to "BloxBuilder_Kai",
                        "isFromMe" to false,
                        "text" to "Squad is meeting at Lake House #14 in Brookhaven! Ready to launch?",
                        "gamePlatform" to "Roblox",
                        "lobbyInviteCode" to "RBX-4924",
                        "lobbyTitle" to "Brookhaven Squad Roleplay",
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
            )

            val batch = db.batch()
            starters.forEach { (convId, convData, msgData) ->
                val convRef = convCol.document(convId)
                val msgRef = convRef.collection("messages").document("msg_welcome")
                batch.set(convRef, convData)
                batch.set(msgRef, msgData)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid ?: "unknown"
            handleFirestoreError(e, OperationType.WRITE, "users/$uid/conversations")
            Result.failure(e)
        }
    }

    suspend fun openOrCreateConversation(
        peerUsername: String,
        gamePlatform: String,
        title: String = peerUsername,
        lobbyId: String = "",
        inviteCode: String = "",
        isGroup: Boolean = false
    ): Result<String> {
        val uid = requireUserId()
        val cleanPeer = peerUsername.trim().take(50).ifBlank { "Player" }
        val cleanTitle = title.trim().take(80).ifBlank { cleanPeer }
        val convId = sanitizeId(
            if (isGroup) "squad_${cleanTitle.lowercase()}" else "dm_${cleanPeer.lowercase()}",
            prefix = "conv"
        )
        val convRef = db.collection("users").document(uid)
            .collection("conversations").document(convId)

        return try {
            val snap = convRef.get().await()
            if (!snap.exists()) {
                val platform = normalizePlatform(gamePlatform)
                val initialMsg = if (inviteCode.isNotBlank()) {
                    "World chat opened for $cleanTitle ($inviteCode)"
                } else {
                    "Started chat with $cleanPeer"
                }
                val convPayload = mapOf(
                    "userId" to uid,
                    "title" to cleanTitle,
                    "peerUsername" to cleanPeer,
                    "gamePlatform" to platform,
                    "lobbyId" to lobbyId.take(128),
                    "inviteCode" to inviteCode.take(32),
                    "lastMessage" to initialMsg.take(500),
                    "unreadCount" to 0L,
                    "isGroup" to isGroup,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                convRef.set(convPayload).await()

                val msgRef = convRef.collection("messages").document("msg_intro")
                val msgPayload = mapOf(
                    "userId" to uid,
                    "conversationId" to convId,
                    "senderName" to cleanPeer.take(60),
                    "isFromMe" to false,
                    "text" to initialMsg.take(1000),
                    "gamePlatform" to platform,
                    "lobbyInviteCode" to inviteCode.take(32),
                    "lobbyTitle" to cleanTitle.take(80),
                    "createdAt" to FieldValue.serverTimestamp()
                )
                msgRef.set(msgPayload).await()
            }
            Result.success(convId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, convRef.path)
            Result.failure(e)
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        senderName: String,
        text: String,
        gamePlatform: String,
        lobbyInviteCode: String = "",
        lobbyTitle: String = ""
    ): Result<Unit> {
        val uid = requireUserId()
        val cleanText = text.trim().take(1000)
        if (cleanText.isEmpty()) return Result.success(Unit)

        val convRef = db.collection("users").document(uid)
            .collection("conversations").document(conversationId)
        val msgRef = convRef.collection("messages").document("msg_${UUID.randomUUID().toString().replace("-", "").take(16)}")
        val platform = normalizePlatform(gamePlatform)

        return try {
            val msgPayload = mapOf(
                "userId" to uid,
                "conversationId" to conversationId,
                "senderName" to senderName.trim().take(60).ifBlank { "Me" },
                "isFromMe" to true,
                "text" to cleanText,
                "gamePlatform" to platform,
                "lobbyInviteCode" to lobbyInviteCode.trim().take(32),
                "lobbyTitle" to lobbyTitle.trim().take(80),
                "createdAt" to FieldValue.serverTimestamp()
            )
            val convUpdate = mapOf(
                "lastMessage" to cleanText.take(500),
                "unreadCount" to 0L,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            val batch = db.batch()
            batch.set(msgRef, msgPayload)
            batch.update(convRef, convUpdate)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun markConversationRead(conversationId: String): Result<Unit> {
        val uid = requireUserId()
        val convRef = db.collection("users").document(uid)
            .collection("conversations").document(conversationId)
        return try {
            convRef.update(
                mapOf(
                    "unreadCount" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, convRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteConversation(conversationId: String): Result<Unit> {
        val uid = requireUserId()
        val convRef = db.collection("users").document(uid)
            .collection("conversations").document(conversationId)
        return try {
            convRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, convRef.path)
            Result.failure(e)
        }
    }

    // --- Shared Multi-User Squad Chat Rooms (Cross-Device Real-Time) ---

    fun observeSquadRooms(userId: String): Flow<List<SquadRoom>> = flow {
        val path = "squad_rooms"
        emitAll(
            db.collection(path)
                .whereArrayContains("memberUids", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SquadRoom::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeSquadMessages(userId: String, roomId: String): Flow<List<SquadMessage>> = flow {
        val path = "squad_rooms/$roomId/messages"
        emitAll(
            db.collection("squad_rooms")
                .document(roomId)
                .collection("messages")
                .whereArrayContains("memberUids", userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        SquadMessage::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createSquadRoom(
        ownerName: String,
        title: String,
        gamePlatform: String,
        roomCode: String
    ): Result<String> {
        val uid = requireUserId()
        val cleanCode = sanitizeId(roomCode.uppercase(), prefix = "SQUAD")
            .take(32)
            .let { if (it.length < 3) "ROOM_$it" else it }
        val cleanTitle = title.trim().take(80).ifBlank { "$ownerName's Squad" }
        val cleanOwner = ownerName.trim().take(60).ifBlank { "Host" }
        val platform = normalizePlatform(gamePlatform)
        val roomRef = db.collection("squad_rooms").document(cleanCode)

        return try {
            val payload = mapOf(
                "ownerId" to uid,
                "title" to cleanTitle,
                "gamePlatform" to platform,
                "roomCode" to cleanCode,
                "memberUids" to listOf(uid),
                "lastMessage" to "Squad room $cleanCode opened!",
                "lastSenderName" to cleanOwner,
                "isJoinableByCode" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            roomRef.set(payload).await()

            val msgRef = roomRef.collection("messages").document("msg_created")
            msgRef.set(
                mapOf(
                    "roomId" to cleanCode,
                    "senderUid" to uid,
                    "senderName" to cleanOwner,
                    "text" to "Squad room $cleanCode opened! Share this code with friends to chat live.",
                    "gamePlatform" to platform,
                    "lobbyInviteCode" to "",
                    "memberUids" to listOf(uid),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(cleanCode)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, roomRef.path)
            Result.failure(e)
        }
    }

    suspend fun joinSquadRoomByCode(roomCode: String): Result<String> {
        val uid = requireUserId()
        val cleanCode = sanitizeId(roomCode.uppercase(), prefix = "ROOM")
        val roomRef = db.collection("squad_rooms").document(cleanCode)
        return try {
            roomRef.update(
                mapOf(
                    "memberUids" to FieldValue.arrayUnion(uid),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(cleanCode)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, roomRef.path)
            Result.failure(e)
        }
    }

    suspend fun sendSquadMessage(
        room: SquadRoom,
        senderName: String,
        text: String,
        lobbyInviteCode: String = ""
    ): Result<Unit> {
        val uid = requireUserId()
        val cleanText = text.trim().take(1000)
        if (cleanText.isEmpty()) return Result.success(Unit)
        val cleanSender = senderName.trim().take(60).ifBlank { "Player" }
        val platform = normalizePlatform(room.gamePlatform)
        val members = if (uid in room.memberUids) room.memberUids else (room.memberUids + uid)

        val roomRef = db.collection("squad_rooms").document(room.id)
        val msgRef = roomRef.collection("messages")
            .document("msg_${UUID.randomUUID().toString().replace("-", "").take(16)}")

        return try {
            val msgPayload = mapOf(
                "roomId" to room.id,
                "senderUid" to uid,
                "senderName" to cleanSender,
                "text" to cleanText,
                "gamePlatform" to platform,
                "lobbyInviteCode" to lobbyInviteCode.trim().take(32),
                "memberUids" to members.take(12),
                "createdAt" to FieldValue.serverTimestamp()
            )
            val batch = db.batch()
            batch.set(msgRef, msgPayload)
            batch.update(
                roomRef,
                mapOf(
                    "lastMessage" to cleanText.take(500),
                    "lastSenderName" to cleanSender,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }
}
