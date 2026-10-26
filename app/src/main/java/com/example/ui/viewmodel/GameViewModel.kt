package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.DesiGameAudioPlayer
import com.example.data.model.DeliveryMission
import com.example.data.model.MissionCatalog
import com.example.data.model.PlayerProfile
import com.example.data.model.ScooterCatalog
import com.example.data.model.UpgradeItem
import com.example.data.model.UpgradeSection
import com.example.data.model.UpgradeType
import com.example.data.repository.PlayerProfileRepository
import com.example.game.ActiveGameRun
import com.example.game.GameEngine
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GameScreenTab {
    PLAY,
    UPGRADES,
    GARAGE,
    MISSIONS,
    PROFILE
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("sandeep_ki_sawari_save", Context.MODE_PRIVATE)
    val audioPlayer = DesiGameAudioPlayer()
    val engine = GameEngine(application, audioPlayer)
    private val repository = PlayerProfileRepository(application)
    private val auth: FirebaseAuth = Firebase.auth

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _profile = MutableStateFlow(loadLocalProfile())
    val profile: StateFlow<PlayerProfile> = _profile.asStateFlow()

    private val _currentTab = MutableStateFlow(GameScreenTab.PLAY)
    val currentTab: StateFlow<GameScreenTab> = _currentTab.asStateFlow()

    private val _isLoggedInOrGuest = MutableStateFlow(auth.currentUser != null || prefs.getBoolean("is_guest_allowed", false))
    val isLoggedInOrGuest: StateFlow<Boolean> = _isLoggedInOrGuest.asStateFlow()

    private val _isPlayingGame = MutableStateFlow(false)
    val isPlayingGame: StateFlow<Boolean> = _isPlayingGame.asStateFlow()

    private val _activeMission = MutableStateFlow<DeliveryMission?>(MissionCatalog.ALL_MISSIONS[0])
    val activeMission: StateFlow<DeliveryMission?> = _activeMission.asStateFlow()

    private val _cloudSyncStatus = MutableStateFlow("Local storage ready")
    val cloudSyncStatus: StateFlow<String> = _cloudSyncStatus.asStateFlow()

    private val _isSoundMuted = MutableStateFlow(false)
    val isSoundMuted: StateFlow<Boolean> = _isSoundMuted.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _currentUser.value = user
        if (user != null) {
            _isLoggedInOrGuest.value = true
            syncWithCloud(user.uid)
        }
    }

    init {
        auth.addAuthStateListener(authListener)
        audioPlayer.startCatchyBgm()

        engine.onGameOver = { finishedRun ->
            handleRunEnded(finishedRun)
        }

        engine.onMissionCompleted = { completedMission ->
            claimMissionReward(completedMission.id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
        audioPlayer.release()
    }

    fun selectTab(tab: GameScreenTab) {
        _currentTab.value = tab
    }

    fun selectMission(mission: DeliveryMission) {
        _activeMission.value = mission
    }

    fun toggleSound() {
        val newMuted = !_isSoundMuted.value
        _isSoundMuted.value = newMuted
        audioPlayer.isMuted = newMuted
    }

    fun continueAsGuest() {
        prefs.edit().putBoolean("is_guest_allowed", true).apply()
        _isLoggedInOrGuest.value = true
    }

    fun startGame() {
        _isPlayingGame.value = true
        startNewGame()
    }

    fun exitToLobby() {
        _isPlayingGame.value = false
        engine.run.status = com.example.game.GamePlayStatus.READY
    }

    fun startNewGame() {
        engine.resetRun(_profile.value, _activeMission.value)
    }

    fun purchaseUpgrade(type: UpgradeType): Boolean {
        val currentP = _profile.value
        val item = getUpgradeItem(type, currentP)
        if (item.isMaxLevel) return false
        val cost = item.nextLevelCost
        if (currentP.coins < cost) return false

        val updated = when (type) {
            UpgradeType.TOP_SPEED -> currentP.copy(coins = currentP.coins - cost, speedLevel = currentP.speedLevel + 1)
            UpgradeType.ACCELERATION -> currentP.copy(coins = currentP.coins - cost, accelLevel = currentP.accelLevel + 1)
            UpgradeType.HANDLING -> currentP.copy(coins = currentP.coins - cost, handlingLevel = currentP.handlingLevel + 1)
            UpgradeType.HORN_BLAST -> currentP.copy(coins = currentP.coins - cost, hornBlastLevel = currentP.hornBlastLevel + 1)
            UpgradeType.TEMPORARY_SHIELD -> currentP.copy(coins = currentP.coins - cost, shieldLevel = currentP.shieldLevel + 1)
            UpgradeType.COIN_MAGNET -> currentP.copy(coins = currentP.coins - cost, coinMagnetLevel = currentP.coinMagnetLevel + 1)
            UpgradeType.SCORE_MULTIPLIER -> currentP.copy(coins = currentP.coins - cost, scoreMultiplierLevel = currentP.scoreMultiplierLevel + 1)
            UpgradeType.DELIVERY_BONUS -> currentP.copy(coins = currentP.coins - cost, deliveryBonusLevel = currentP.deliveryBonusLevel + 1)
        }

        _profile.value = updated
        saveLocalProfile(updated)
        pushProfileToCloud(updated)
        audioPlayer.playVictory()
        return true
    }

    fun equipOrPurchaseScooter(scooterId: String): Boolean {
        val currentP = _profile.value
        val scooter = ScooterCatalog.getScooter(scooterId)

        if (currentP.unlockedScooters.contains(scooterId)) {
            val updated = currentP.copy(selectedScooterId = scooterId)
            _profile.value = updated
            saveLocalProfile(updated)
            pushProfileToCloud(updated)
            return true
        } else {
            if (currentP.coins >= scooter.cost) {
                val newUnlocked = currentP.unlockedScooters + scooterId
                val updated = currentP.copy(
                    coins = currentP.coins - scooter.cost,
                    unlockedScooters = newUnlocked,
                    selectedScooterId = scooterId
                )
                _profile.value = updated
                saveLocalProfile(updated)
                pushProfileToCloud(updated)
                audioPlayer.playVictory()
                return true
            }
            return false
        }
    }

    fun claimMissionReward(missionId: String) {
        val currentP = _profile.value
        if (currentP.completedMissions.contains(missionId)) return
        val mission = MissionCatalog.getMission(missionId) ?: return

        val updated = currentP.copy(
            coins = currentP.coins + mission.rewardCoins,
            completedMissions = currentP.completedMissions + missionId
        )
        _profile.value = updated
        saveLocalProfile(updated)
        pushProfileToCloud(updated)
        audioPlayer.playVictory()
    }

    private fun handleRunEnded(run: ActiveGameRun) {
        val currentP = _profile.value
        val coinsEarned = run.coinsCollected.toLong()
        val newHighScore = maxOf(currentP.highScore, run.currentScore)
        val newDistance = currentP.totalDistanceMeters + run.distanceMeters.toLong()
        val newParcels = currentP.parcelsDelivered + run.parcelsDelivered.toLong()

        val updated = currentP.copy(
            coins = currentP.coins + coinsEarned,
            highScore = newHighScore,
            totalDistanceMeters = newDistance,
            parcelsDelivered = newParcels
        )
        _profile.value = updated
        saveLocalProfile(updated)
        pushProfileToCloud(updated)
    }

    fun getUpgradeItem(type: UpgradeType, currentP: PlayerProfile = _profile.value): UpgradeItem {
        return when (type) {
            UpgradeType.TOP_SPEED -> UpgradeItem(
                type = type,
                section = UpgradeSection.PERFORMANCE,
                title = "Top Speed Velocity",
                hindiTitle = "अधिकतम रफ्तार",
                description = "Increase maximum cruise speed by +15% per level on Indian highways.",
                currentLevel = currentP.speedLevel
            )
            UpgradeType.ACCELERATION -> UpgradeItem(
                type = type,
                section = UpgradeSection.PERFORMANCE,
                title = "Pickup Acceleration",
                hindiTitle = "पिकअप त्वरण",
                description = "Quicker throttle response and faster return to top speed after braking.",
                currentLevel = currentP.accelLevel
            )
            UpgradeType.HANDLING -> UpgradeItem(
                type = type,
                section = UpgradeSection.PERFORMANCE,
                title = "Lane Agility & Steering",
                hindiTitle = "स्टीयरिंग नियंत्रण",
                description = "Snappy lane changes to weave between auto-rickshaws and tata trucks.",
                currentLevel = currentP.handlingLevel
            )
            UpgradeType.HORN_BLAST -> UpgradeItem(
                type = type,
                section = UpgradeSection.DEFENSE,
                title = "Horn OK Please Shockwave",
                hindiTitle = "धमाकेदार हॉर्न",
                description = "Sonic blast honk forces vehicles ahead to swerve aside. Increases cone radius and decreases cooldown.",
                currentLevel = currentP.hornBlastLevel
            )
            UpgradeType.TEMPORARY_SHIELD -> UpgradeItem(
                type = type,
                section = UpgradeSection.DEFENSE,
                title = "Delivery Armor Shield",
                hindiTitle = "सुरक्षा कवच",
                description = "Reinforced energy barrier absorbs crash impacts, protecting Sandeep and his parcels.",
                currentLevel = currentP.shieldLevel
            )
            UpgradeType.COIN_MAGNET -> UpgradeItem(
                type = type,
                section = UpgradeSection.ABILITIES,
                title = "Coin Jhol Magnet",
                hindiTitle = "सिक्का चुंबक",
                description = "Pulls golden rupee coins from distant lanes directly towards Sandeep.",
                currentLevel = currentP.coinMagnetLevel
            )
            UpgradeType.SCORE_MULTIPLIER -> UpgradeItem(
                type = type,
                section = UpgradeSection.ABILITIES,
                title = "Chai Power Multiplier",
                hindiTitle = "चाय पावर मल्टीप्लायर",
                description = "Hot cutting chai boost lasts longer, awarding 2x-3x score multiplier.",
                currentLevel = currentP.scoreMultiplierLevel
            )
            UpgradeType.DELIVERY_BONUS -> UpgradeItem(
                type = type,
                section = UpgradeSection.ABILITIES,
                title = "Express Tip Bonus",
                hindiTitle = "डिलीवरी टिप बोनस",
                description = "Earn bonus coins for every on-time parcel delivered successfully.",
                currentLevel = currentP.deliveryBonusLevel
            )
        }
    }

    private fun loadLocalProfile(): PlayerProfile {
        val uid = auth.currentUser?.uid ?: prefs.getString("local_uid", "guest_driver") ?: "guest_driver"
        return PlayerProfile(
            userId = uid,
            displayName = prefs.getString("display_name", "Sandeep") ?: "Sandeep",
            coins = prefs.getLong("coins", 150L),
            highScore = prefs.getLong("high_score", 0L),
            totalDistanceMeters = prefs.getLong("total_distance", 0L),
            parcelsDelivered = prefs.getLong("parcels_delivered", 0L),
            selectedScooterId = prefs.getString("selected_scooter", "chetak_125") ?: "chetak_125",
            unlockedScooters = prefs.getStringSet("unlocked_scooters", setOf("chetak_125"))?.toList() ?: listOf("chetak_125"),
            speedLevel = prefs.getLong("speed_level", 1L),
            accelLevel = prefs.getLong("accel_level", 1L),
            handlingLevel = prefs.getLong("handling_level", 1L),
            hornBlastLevel = prefs.getLong("horn_blast_level", 1L),
            shieldLevel = prefs.getLong("shield_level", 1L),
            coinMagnetLevel = prefs.getLong("coin_magnet_level", 1L),
            scoreMultiplierLevel = prefs.getLong("score_multiplier_level", 1L),
            deliveryBonusLevel = prefs.getLong("delivery_bonus_level", 1L),
            completedMissions = prefs.getStringSet("completed_missions", emptySet())?.toList() ?: emptyList()
        )
    }

    private fun saveLocalProfile(p: PlayerProfile) {
        prefs.edit()
            .putString("display_name", p.displayName)
            .putLong("coins", p.coins)
            .putLong("high_score", p.highScore)
            .putLong("total_distance", p.totalDistanceMeters)
            .putLong("parcels_delivered", p.parcelsDelivered)
            .putString("selected_scooter", p.selectedScooterId)
            .putStringSet("unlocked_scooters", p.unlockedScooters.toSet())
            .putLong("speed_level", p.speedLevel)
            .putLong("accel_level", p.accelLevel)
            .putLong("handlingLevel", p.handlingLevel)
            .putLong("horn_blast_level", p.hornBlastLevel)
            .putLong("shield_level", p.shieldLevel)
            .putLong("coin_magnet_level", p.coinMagnetLevel)
            .putLong("score_multiplier_level", p.scoreMultiplierLevel)
            .putLong("delivery_bonus_level", p.deliveryBonusLevel)
            .putStringSet("completed_missions", p.completedMissions.toSet())
            .apply()
    }

    private fun pushProfileToCloud(profileToSave: PlayerProfile) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                _cloudSyncStatus.value = "Syncing to Cloud..."
                val p = profileToSave.copy(userId = user.uid, displayName = user.displayName ?: profileToSave.displayName)
                repository.saveProfile(p)
                _cloudSyncStatus.value = "Synced with Cloud"
            } catch (e: Exception) {
                _cloudSyncStatus.value = "Cloud sync pending (saved locally)"
            }
        }
    }

    private fun syncWithCloud(uid: String) {
        viewModelScope.launch {
            try {
                _cloudSyncStatus.value = "Connecting to Firestore..."
                val remoteProfile = repository.getProfile()
                if (remoteProfile != null) {
                    // Merge remote and local (take higher coins & higher scores)
                    val merged = _profile.value.copy(
                        userId = uid,
                        displayName = auth.currentUser?.displayName ?: remoteProfile.displayName,
                        coins = maxOf(_profile.value.coins, remoteProfile.coins),
                        highScore = maxOf(_profile.value.highScore, remoteProfile.highScore),
                        totalDistanceMeters = maxOf(_profile.value.totalDistanceMeters, remoteProfile.totalDistanceMeters),
                        parcelsDelivered = maxOf(_profile.value.parcelsDelivered, remoteProfile.parcelsDelivered),
                        unlockedScooters = (_profile.value.unlockedScooters + remoteProfile.unlockedScooters).distinct(),
                        speedLevel = maxOf(_profile.value.speedLevel, remoteProfile.speedLevel),
                        accelLevel = maxOf(_profile.value.accelLevel, remoteProfile.accelLevel),
                        handlingLevel = maxOf(_profile.value.handlingLevel, remoteProfile.handlingLevel),
                        hornBlastLevel = maxOf(_profile.value.hornBlastLevel, remoteProfile.hornBlastLevel),
                        shieldLevel = maxOf(_profile.value.shieldLevel, remoteProfile.shieldLevel),
                        coinMagnetLevel = maxOf(_profile.value.coinMagnetLevel, remoteProfile.coinMagnetLevel),
                        scoreMultiplierLevel = maxOf(_profile.value.scoreMultiplierLevel, remoteProfile.scoreMultiplierLevel),
                        deliveryBonusLevel = maxOf(_profile.value.deliveryBonusLevel, remoteProfile.deliveryBonusLevel),
                        completedMissions = (_profile.value.completedMissions + remoteProfile.completedMissions).distinct()
                    )
                    _profile.value = merged
                    saveLocalProfile(merged)
                    repository.saveProfile(merged)
                    _cloudSyncStatus.value = "Synced with Cloud"
                } else {
                    // First time user on cloud: upload current profile
                    val toSave = _profile.value.copy(
                        userId = uid,
                        displayName = auth.currentUser?.displayName ?: _profile.value.displayName
                    )
                    repository.saveProfile(toSave)
                    _cloudSyncStatus.value = "Synced with Cloud"
                }
            } catch (e: Exception) {
                _cloudSyncStatus.value = "Local progress preserved (offline)"
            }
        }
    }
}
