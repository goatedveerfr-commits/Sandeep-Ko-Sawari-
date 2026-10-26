package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.PlayerProfile
import com.example.data.repository.PlayerProfileRepository
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PlayerProfileRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun unauthenticatedUserCannotSaveProfile() = runBlocking {
        auth.signOut()
        val repo = PlayerProfileRepository(firestore)
        try {
            repo.saveProfile(PlayerProfile(userId = "unauthed", coins = 100))
            fail("Expected exception when unauthenticated")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("User must be signed in") == true)
        }
    }

    @Test
    fun authenticatedUserCanSaveAndRetrieveProfile() = runBlocking {
        val uid = signInTestUser("sandeep_player@example.com")
        val repo = PlayerProfileRepository(firestore)

        val profile = PlayerProfile(
            userId = uid,
            displayName = "Sandeep Express",
            coins = 500L,
            highScore = 1500L,
            selectedScooterId = "activa_super",
            speedLevel = 2L,
            accelLevel = 2L
        )

        repo.saveProfile(profile)

        val retrieved = repo.getProfile()
        assertNotNull(retrieved)
        assertEquals(uid, retrieved?.userId)
        assertEquals(500L, retrieved?.coins)
        assertEquals(1500L, retrieved?.highScore)
        assertEquals("activa_super", retrieved?.selectedScooterId)
        assertEquals(2L, retrieved?.speedLevel)
    }

    @Test
    fun crossUserAccessFailsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser("alice_driver@example.com")
        val aliceRepo = PlayerProfileRepository(firestore)
        aliceRepo.saveProfile(PlayerProfile(userId = aliceUid, coins = 300L, highScore = 900L))

        // Switch to Bob
        val bobUid = signInTestUser("bob_driver@example.com")
        // Bob tries directly reading Alice's document through Firestore
        try {
            val task = firestore.collection("users").document(aliceUid).collection("profile").document("game_data").get()
            task.await()
            fail("Bob should not be able to read Alice's profile")
        } catch (e: Exception) {
            val firestoreEx = e.unwrapFirestoreException()
            assertNotNull(firestoreEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
        }
    }
}
