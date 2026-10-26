package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class PlayerProfile(
    @DocumentId val id: String = "game_data",
    val userId: String = "",
    val displayName: String = "Sandeep",
    val coins: Long = 100L,
    val highScore: Long = 0L,
    val totalDistanceMeters: Long = 0L,
    val parcelsDelivered: Long = 0L,
    val selectedScooterId: String = "chetak_125",
    val unlockedScooters: List<String> = listOf("chetak_125"),
    val speedLevel: Long = 1L,
    val accelLevel: Long = 1L,
    val handlingLevel: Long = 1L,
    val hornBlastLevel: Long = 1L,
    val shieldLevel: Long = 1L,
    val coinMagnetLevel: Long = 1L,
    val scoreMultiplierLevel: Long = 1L,
    val deliveryBonusLevel: Long = 1L,
    val completedMissions: List<String> = emptyList(),
    @ServerTimestamp val updatedAt: Timestamp? = null
)
