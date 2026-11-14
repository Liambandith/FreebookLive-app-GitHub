const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate() {
  return new Date(Date.now() - 2000);
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or list conversations or squad rooms", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("conversations").get()
  );
  await assertFails(
    unauthDb.collection("squad_rooms").where("memberUids", "array-contains", ALICE_UID).get()
  );
});

test("Authenticated user can create and read own conversation and send message", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const convRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("conversations")
    .doc("conv_alex");
  const ts = pastDate();

  await assertSucceeds(
    convRef.set({
      userId: ALICE_UID,
      title: "Alex_Overlord",
      peerUsername: "Alex_Overlord",
      gamePlatform: "Minecraft",
      lobbyId: "fbl_world_alex",
      inviteCode: "FBL-1001",
      lastMessage: "World is open!",
      unreadCount: 0,
      isGroup: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("conversations").get()
  );

  await assertSucceeds(
    convRef.collection("messages").doc("msg_1").set({
      userId: ALICE_UID,
      conversationId: "conv_alex",
      senderName: "Alice",
      isFromMe: true,
      text: "Joining your world now!",
      gamePlatform: "Minecraft",
      lobbyInviteCode: "FBL-1001",
      lobbyTitle: "Alex's Survival Kingdom",
      createdAt: pastDate(),
    })
  );
});

test("Authenticated user cannot read or write another user's private conversation", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const ts = pastDate();
    await ctx
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("conversations")
      .doc("bob_conv")
      .set({
        userId: BOB_UID,
        title: "Secret Squad",
        peerUsername: "PirateKing_Z",
        gamePlatform: "Roblox",
        lastMessage: "Meet at spawn",
        unreadCount: 1,
        isGroup: false,
        createdAt: ts,
        updatedAt: ts,
      });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("conversations").doc("bob_conv").get()
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("conversations").get()
  );
});

test("Shadow update with ghost field is rejected on conversation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = pastDate();
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("conversations")
      .doc("conv_ghost")
      .set({
        userId: ALICE_UID,
        title: "Alex_Overlord",
        peerUsername: "Alex_Overlord",
        gamePlatform: "Minecraft",
        lastMessage: "Hi",
        unreadCount: 0,
        isGroup: false,
        isAdmin: true,
        createdAt: ts,
        updatedAt: ts,
      })
  );
});

test("Shared squad room: owner creates, second user joins by code and chats, third non-member cannot read", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const ts = pastDate();

  const roomRefAlice = aliceDb.collection("squad_rooms").doc("FBL-SQUAD-1");
  await assertSucceeds(
    roomRefAlice.set({
      ownerId: ALICE_UID,
      title: "Survival Squad",
      gamePlatform: "Minecraft",
      roomCode: "FBL-SQUAD-1",
      memberUids: [ALICE_UID],
      lastMessage: "Room created",
      lastSenderName: "Alice",
      isJoinableByCode: true,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Non-member cannot read or list without filter
  await assertFails(bobDb.collection("squad_rooms").doc("FBL-SQUAD-1").get());
  await assertFails(bobDb.collection("squad_rooms").get());

  // Bob joins room by adding only himself to memberUids
  await assertSucceeds(
    bobDb.collection("squad_rooms").doc("FBL-SQUAD-1").update({
      memberUids: [ALICE_UID, BOB_UID],
      updatedAt: pastDate(),
    })
  );

  // Now Bob can query rooms he is a member of and send messages
  await assertSucceeds(
    bobDb.collection("squad_rooms").where("memberUids", "array-contains", BOB_UID).get()
  );
  await assertSucceeds(
    bobDb
      .collection("squad_rooms")
      .doc("FBL-SQUAD-1")
      .collection("messages")
      .doc("m1")
      .set({
        roomId: "FBL-SQUAD-1",
        senderUid: BOB_UID,
        senderName: "Bob",
        text: "On my way to Minecraft Worlds tab!",
        gamePlatform: "Minecraft",
        lobbyInviteCode: "FBL-1001",
        memberUids: [ALICE_UID, BOB_UID],
        createdAt: pastDate(),
      })
  );

  // Charlie (non-member) is still blocked from reading room, reading message doc, or querying Alice's messages
  await assertFails(charlieDb.collection("squad_rooms").doc("FBL-SQUAD-1").get());
  await assertFails(
    charlieDb
      .collection("squad_rooms")
      .doc("FBL-SQUAD-1")
      .collection("messages")
      .doc("m1")
      .get()
  );
  await assertFails(
    charlieDb
      .collection("squad_rooms")
      .doc("FBL-SQUAD-1")
      .collection("messages")
      .get()
  );
  await assertFails(
    charlieDb
      .collection("squad_rooms")
      .doc("FBL-SQUAD-1")
      .collection("messages")
      .where("memberUids", "array-contains", ALICE_UID)
      .get()
  );
});
