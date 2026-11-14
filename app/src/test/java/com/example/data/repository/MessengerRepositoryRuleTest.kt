package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class MessengerRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun openConversationAndSendMessage_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = MessengerRepository(firestore, auth)

        val peer = "Alex_${UUID.randomUUID().toString().take(6)}"
        val convResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.openOrCreateConversation(
                peerUsername = peer,
                gamePlatform = "Minecraft",
                inviteCode = "FBL-1001"
            )
        }
        assertTrue(convResult.isSuccess)
        val convId = convResult.getOrThrow()

        val msgText = "Hopping into your Survival Kingdom now!"
        val sendResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.sendMessage(
                conversationId = convId,
                senderName = "Alice",
                text = msgText,
                gamePlatform = "Minecraft",
                lobbyInviteCode = "FBL-1001"
            )
        }
        assertTrue(sendResult.isSuccess)

        val conversations = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeConversations(uid).first { list -> list.any { it.id == convId && it.lastMessage == msgText } }
        }
        assertTrue(conversations.any { it.id == convId })

        val messages = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeMessages(uid, convId).first { list -> list.any { it.text == msgText } }
        }
        assertTrue(messages.any { it.text == msgText && it.isFromMe })
    }

    @Test
    fun observeConversations_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = MessengerRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeConversations("alice_uid").first()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated user")
        } catch (e: Exception) {
            val firestoreEx = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
        }
    }

    @Test
    fun observeConversations_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = MessengerRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.openOrCreateConversation("PixieCrafter", "Minecraft").getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = MessengerRepository(firestore, auth)
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeConversations(aliceUid).first()
            }
            fail("Expected PERMISSION_DENIED when Bob reads Alice's conversations")
        } catch (e: Exception) {
            val firestoreEx = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
        }
    }

    @Test
    fun squadRoom_createJoinByCodeAndChat_isolatesNonMembers() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = MessengerRepository(firestore, auth)
        val roomCode = "SQD-${UUID.randomUUID().toString().take(6).uppercase()}"

        val createRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createSquadRoom(
                ownerName = "Alice",
                title = "Minecraft Raid Squad",
                gamePlatform = "Minecraft",
                roomCode = roomCode
            )
        }
        assertTrue(createRes.isSuccess)

        val bobUid = signInTestUser(BOB_EMAIL)
        val bobRepo = MessengerRepository(firestore, auth)
        val joinRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.joinSquadRoomByCode(roomCode)
        }
        assertTrue(joinRes.isSuccess)

        val roomsForBob = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeSquadRooms(bobUid).first { list -> list.any { it.id == roomCode } }
        }
        val joinedRoom = roomsForBob.first { it.id == roomCode }
        assertTrue(bobUid in joinedRoom.memberUids)
        assertTrue(aliceUid in joinedRoom.memberUids)

        val squadMsg = "Ready in Minecraft > Play > Worlds!"
        val sendRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.sendSquadMessage(joinedRoom, "Bob", squadMsg, "FBL-1001")
        }
        assertTrue(sendRes.isSuccess)

        val emittedMsgs = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeSquadMessages(bobUid, roomCode).first { list -> list.any { it.text == squadMsg } }
        }
        assertTrue(emittedMsgs.any { it.text == squadMsg && it.senderUid == bobUid })

        // Unauthenticated cannot observe squad rooms
        auth.signOut()
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                bobRepo.observeSquadRooms(bobUid).first()
            }
            fail("Expected PERMISSION_DENIED when unauthenticated")
        } catch (e: Exception) {
            val firestoreEx = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_messenger@test.com"
        const val BOB_EMAIL = "bob_messenger@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
