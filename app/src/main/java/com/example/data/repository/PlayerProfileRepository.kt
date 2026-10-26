package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.PlayerProfile
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PlayerProfileRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    fun observeProfile(): Flow<PlayerProfile?> = flow {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("profile").document("game_data")
        val snapshotsFlow = docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(PlayerProfile::class.java)
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, docRef.path)
                }
                throw error
            }
        snapshotsFlow.collect { profile ->
            emit(profile)
        }
    }

    suspend fun getProfile(): PlayerProfile? {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("profile").document("game_data")
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(PlayerProfile::class.java)
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun saveProfile(profile: PlayerProfile) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("profile").document("game_data")
        val payload = mapOf(
            "userId" to uid,
            "displayName" to profile.displayName,
            "coins" to profile.coins,
            "highScore" to profile.highScore,
            "totalDistanceMeters" to profile.totalDistanceMeters,
            "parcelsDelivered" to profile.parcelsDelivered,
            "selectedScooterId" to profile.selectedScooterId,
            "unlockedScooters" to profile.unlockedScooters,
            "speedLevel" to profile.speedLevel,
            "accelLevel" to profile.accelLevel,
            "handlingLevel" to profile.handlingLevel,
            "hornBlastLevel" to profile.hornBlastLevel,
            "shieldLevel" to profile.shieldLevel,
            "coinMagnetLevel" to profile.coinMagnetLevel,
            "scoreMultiplierLevel" to profile.scoreMultiplierLevel,
            "deliveryBonusLevel" to profile.deliveryBonusLevel,
            "completedMissions" to profile.completedMissions,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }
}
