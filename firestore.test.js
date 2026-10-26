const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("Unauthenticated read must fail", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.doc("users/alice/profile/game_data").get());
});

test("Unauthenticated write must fail", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.doc("users/alice/profile/game_data").set({
    userId: "alice",
    coins: 100,
    highScore: 500,
    selectedScooterId: "chetak_125"
  }));
});

test("Cross-user read must fail", async () => {
  const bobDb = testEnv.authenticatedContext("bob").firestore();
  await assertFails(bobDb.doc("users/alice/profile/game_data").get());
});

test("Cross-user write must fail", async () => {
  const bobDb = testEnv.authenticatedContext("bob").firestore();
  await assertFails(bobDb.doc("users/alice/profile/game_data").set({
    userId: "alice",
    coins: 100,
    highScore: 500,
    selectedScooterId: "chetak_125"
  }));
});

test("Owner write with valid profile must succeed", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await assertSucceeds(aliceDb.doc("users/alice/profile/game_data").set({
    userId: "alice",
    displayName: "Sandeep Fan",
    coins: 250,
    highScore: 1200,
    selectedScooterId: "chetak_125",
    totalDistanceMeters: 4500,
    parcelsDelivered: 8,
    unlockedScooters: ["chetak_125", "activa_super"],
    speedLevel: 2,
    accelLevel: 1,
    handlingLevel: 2,
    hornBlastLevel: 1,
    shieldLevel: 1,
    coinMagnetLevel: 2,
    scoreMultiplierLevel: 1,
    deliveryBonusLevel: 1,
    completedMissions: ["mission_samosa_rush"]
  }));
});

test("Owner write with missing required field must fail", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await assertFails(aliceDb.doc("users/alice/profile/game_data").set({
    userId: "alice",
    displayName: "Sandeep Fan"
    // missing coins, highScore, selectedScooterId
  }));
});

test("Owner read of own profile must succeed", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await aliceDb.doc("users/alice/profile/game_data").set({
    userId: "alice",
    coins: 50,
    highScore: 200,
    selectedScooterId: "chetak_125"
  });
  await assertSucceeds(aliceDb.doc("users/alice/profile/game_data").get());
});
