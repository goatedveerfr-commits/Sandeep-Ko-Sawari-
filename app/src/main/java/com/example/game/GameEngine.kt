package com.example.game

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.audio.DesiGameAudioPlayer
import com.example.data.model.DeliveryMission
import com.example.data.model.MissionType
import com.example.data.model.PlayerProfile
import com.example.data.model.Scooter
import com.example.data.model.ScooterCatalog
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random

class GameEngine(
    private val context: Context,
    val audioPlayer: DesiGameAudioPlayer
) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Player position
    var playerLane: Int = 1 // 0 (Left), 1 (Center), 2 (Right)
    var playerVisualX: Float = 0.5f // 0f to 1f normalized across road
    var playerTargetX: Float = 0.5f
    var playerLeanAngle: Float = 0f // In degrees (-15 to +15)

    // Game runtime state
    val run = ActiveGameRun()

    // Active entities
    val trafficList = mutableListOf<TrafficVehicle>()
    val collectibles = mutableListOf<Collectible>()
    val shockwaves = mutableListOf<HornShockwave>()
    val sceneryItems = mutableListOf<SceneryItem>()
    val weatherController = WeatherController()

    // Entity ID generator
    private var nextEntityId = 1L

    // Spawning timers (in ms)
    private var trafficSpawnTimer = 0L
    private var collectibleSpawnTimer = 0L
    private var scenerySpawnTimer = 0L
    private var runElapsedMs = 0L

    // Current player equipment & upgrades
    var profile: PlayerProfile = PlayerProfile()
    var currentScooter: Scooter = ScooterCatalog.ALL_SCOOTERS[0]
    var activeMission: DeliveryMission? = null

    // Callback on game over
    var onGameOver: ((ActiveGameRun) -> Unit)? = null
    var onMissionCompleted: ((DeliveryMission) -> Unit)? = null

    fun resetRun(newProfile: PlayerProfile, mission: DeliveryMission? = null) {
        profile = newProfile
        currentScooter = ScooterCatalog.getScooter(profile.selectedScooterId)
        activeMission = mission

        playerLane = 1
        playerVisualX = 0.5f
        playerTargetX = 0.5f
        playerLeanAngle = 0f

        trafficList.clear()
        collectibles.clear()
        shockwaves.clear()
        sceneryItems.clear()

        run.status = GamePlayStatus.RIDING
        run.distanceMeters = 0f
        run.coinsCollected = 0
        run.parcelsDelivered = 0
        run.hornUses = 0
        run.nitroUses = 0
        run.crashesInRun = 0
        run.currentScore = 0L
        run.activeMissionBanner = mission?.title
        run.currentWeather = weatherController.currentWeather

        // Calculate initial shield charges from shieldLevel upgrade
        run.currentShieldCharges = ((profile.shieldLevel.toInt() + 1) / 2).coerceAtLeast(1)

        run.isNitroActive = false
        run.nitroTimeRemainingMs = 0L
        run.isMagnetActive = false
        run.magnetTimeRemainingMs = 0L
        run.hornCooldownMs = 0L

        // Base max cooldown drops as hornBlastLevel increases
        run.hornCooldownMaxMs = (4000L - (profile.hornBlastLevel - 1) * 600L).coerceAtLeast(1500L)

        // Target delivery distance
        run.targetDeliveryDistance = when (mission?.type) {
            MissionType.DISTANCE -> mission.targetValue.toFloat()
            MissionType.TIME_TRIAL -> 600f
            MissionType.NO_CRASH -> mission.targetValue.toFloat()
            else -> 800f
        }

        runElapsedMs = 0L
        trafficSpawnTimer = 600L
        collectibleSpawnTimer = 400L
        scenerySpawnTimer = 300L

        // Prepopulate some roadside scenery
        sceneryItems.add(SceneryItem(nextEntityId++, isLeft = true, y = 200f, type = SceneryType.CHAI_DHABA, text = "Sharma Chai"))
        sceneryItems.add(SceneryItem(nextEntityId++, isLeft = false, y = 450f, type = SceneryType.MILESTONE_MARKER, text = "DELHI 25 KM"))
        sceneryItems.add(SceneryItem(nextEntityId++, isLeft = true, y = 700f, type = SceneryType.PALM_TREE))
    }

    fun steerLeft() {
        if (run.status != GamePlayStatus.RIDING) return
        if (playerLane > 0) {
            playerLane--
            updateTargetX()
            vibrateBrief(20)
        }
    }

    fun steerRight() {
        if (run.status != GamePlayStatus.RIDING) return
        if (playerLane < 2) {
            playerLane++
            updateTargetX()
            vibrateBrief(20)
        }
    }

    fun setLaneByTouch(normalizedX: Float) {
        if (run.status != GamePlayStatus.RIDING) return
        val targetLane = when {
            normalizedX < 0.35f -> 0
            normalizedX > 0.65f -> 2
            else -> 1
        }
        if (targetLane != playerLane) {
            playerLane = targetLane
            updateTargetX()
            vibrateBrief(20)
        }
    }

    private fun updateTargetX() {
        playerTargetX = when (playerLane) {
            0 -> 0.22f // Left lane center
            1 -> 0.50f // Middle lane center
            else -> 0.78f // Right lane center
        }
    }

    fun triggerHornBlast(screenWidth: Float, screenHeight: Float) {
        if (run.status != GamePlayStatus.RIDING) return
        if (run.hornCooldownMs > 0) return

        run.hornCooldownMs = run.hornCooldownMaxMs
        run.hornUses++
        audioPlayer.playHorn()
        vibrateBrief(60)

        val playerPxX = playerVisualX * screenWidth
        val playerPxY = screenHeight * 0.78f

        // Radius increases with hornBlastLevel
        val blastRadius = (160f + (profile.hornBlastLevel - 1) * 60f)
        shockwaves.add(
            HornShockwave(
                originX = playerPxX,
                originY = playerPxY,
                radius = 20f,
                maxRadius = blastRadius
            )
        )

        // Clear vehicles ahead within blast radius
        for (v in trafficList) {
            val vx = getLaneCenterX(v.lane) * screenWidth
            val dist = hypot(vx - playerPxX, v.y - playerPxY)
            if (dist < blastRadius && v.y < playerPxY) {
                // Vehicle gets startled and swerves away!
                v.isAlerted = true
                if (v.lane == 1) {
                    v.swerveTargetLane = if (Random.nextBoolean()) 0 else 2
                } else if (v.lane == 0) {
                    v.swerveTargetLane = 1
                } else {
                    v.swerveTargetLane = 1
                }
            }
        }

        checkMissionProgress()
    }

    fun triggerChaiNitro() {
        if (run.status != GamePlayStatus.RIDING) return
        if (run.isNitroActive) return

        run.isNitroActive = true
        // Duration increases with scoreMultiplierLevel
        run.nitroTimeRemainingMs = 4500L + (profile.scoreMultiplierLevel - 1) * 2000L
        run.nitroUses++
        audioPlayer.playNitro()
        vibrateBrief(120)
    }

    fun update(deltaSeconds: Float, screenWidth: Float, screenHeight: Float) {
        if (run.status != GamePlayStatus.RIDING) return
        val dtMs = (deltaSeconds * 1000f).toLong().coerceIn(1L, 100L)
        runElapsedMs += dtMs

        // Update Cooldowns
        if (run.hornCooldownMs > 0) {
            run.hornCooldownMs = (run.hornCooldownMs - dtMs).coerceAtLeast(0L)
        }
        if (run.isNitroActive) {
            run.nitroTimeRemainingMs -= dtMs
            if (run.nitroTimeRemainingMs <= 0) {
                run.isNitroActive = false
            }
        }
        if (run.isMagnetActive) {
            run.magnetTimeRemainingMs -= dtMs
            if (run.magnetTimeRemainingMs <= 0) {
                run.isMagnetActive = false
            }
        }

        // Speed calculations:
        // Base scooter speed (40-60 km/h) + upgrade bonus (speedLevel) + nitro (2x)
        val baseKmh = 45f * currentScooter.baseSpeed * (1f + (profile.speedLevel - 1) * 0.15f)
        val targetSpeed = if (run.isNitroActive) baseKmh * 1.75f else baseKmh
        // Acceleration smoothing
        val accelRate = 4.0f * currentScooter.baseAcceleration * (1f + (profile.accelLevel - 1) * 0.2f)
        run.speedKmh += (targetSpeed - run.speedKmh) * (deltaSeconds * accelRate).coerceIn(0f, 1f)

        // Distance & score calculation
        val distanceTraveled = (run.speedKmh * (1000f / 3600f)) * deltaSeconds
        run.distanceMeters += distanceTraveled
        run.currentScore += (distanceTraveled * if (run.isNitroActive) 3f else 1f).toLong()

        // Update Weather System
        weatherController.update(deltaSeconds, screenHeight)
        run.currentWeather = weatherController.currentWeather

        // Smooth player X interpolation (Handling level affects responsiveness, affected by weather traction)
        val weatherTraction = weatherController.currentWeather.handlingTraction
        val handlingSpeed = 12f * currentScooter.baseHandling * (1f + (profile.handlingLevel - 1) * 0.2f) * weatherTraction
        val dx = playerTargetX - playerVisualX
        playerVisualX += dx * (deltaSeconds * handlingSpeed).coerceIn(0f, 1f)
        playerLeanAngle = (-dx * 45f).coerceIn(-18f, 18f)

        val playerPxX = playerVisualX * screenWidth
        val playerPxY = screenHeight * 0.78f

        // World scroll velocity in pixels per second
        val worldScrollPixelsPerSec = run.speedKmh * 14.5f

        // Update shockwaves
        val shockIter = shockwaves.iterator()
        while (shockIter.hasNext()) {
            val sw = shockIter.next()
            sw.radius += deltaSeconds * 400f
            sw.alpha = (1f - (sw.radius / sw.maxRadius)).coerceIn(0f, 1f)
            if (sw.radius >= sw.maxRadius) {
                shockIter.remove()
            }
        }

        // Update traffic vehicles
        updateTraffic(deltaSeconds, worldScrollPixelsPerSec, screenWidth, screenHeight, playerPxX, playerPxY)

        // Update collectibles
        updateCollectibles(deltaSeconds, worldScrollPixelsPerSec, screenWidth, screenHeight, playerPxX, playerPxY)

        // Update scenery
        updateScenery(deltaSeconds, worldScrollPixelsPerSec, screenHeight)

        // Spawners
        spawnTraffic(deltaSeconds, screenWidth)
        spawnCollectibles(deltaSeconds, screenWidth)
        spawnScenery(deltaSeconds)

        // Check mission objectives
        checkMissionProgress()
    }

    private fun updateTraffic(
        deltaSeconds: Float,
        worldSpeedPx: Float,
        screenWidth: Float,
        screenHeight: Float,
        playerPxX: Float,
        playerPxY: Float
    ) {
        val playerHitRadius = 32f
        val trafficIter = trafficList.iterator()

        while (trafficIter.hasNext()) {
            val v = trafficIter.next()
            // Relative speed: traffic moves down at (worldSpeedPx - trafficSelfSpeed)
            val relSpeed = worldSpeedPx - (v.speed * 8f)
            v.y += relSpeed * deltaSeconds

            // Handle alerted vehicles swerving away
            if (v.isAlerted && v.swerveTargetLane != null) {
                v.lane = v.swerveTargetLane!!
            }

            val vx = getLaneCenterX(v.lane) * screenWidth

            // Collision check with player scooter
            val dx = abs(vx - playerPxX)
            val dy = abs(v.y - playerPxY)

            if (dx < (v.width * 0.45f + playerHitRadius) && dy < (v.height * 0.42f + playerHitRadius)) {
                // COLLISION DETECTED!
                handleCrash(v, trafficIter)
            } else if (v.y > screenHeight + 200f || v.y < -300f) {
                trafficIter.remove()
            }
        }
    }

    private fun handleCrash(vehicle: TrafficVehicle, iterator: MutableIterator<TrafficVehicle>) {
        run.crashesInRun++
        iterator.remove()

        // Check if shield can absorb
        if (run.currentShieldCharges > 0) {
            run.currentShieldCharges--
            audioPlayer.playShieldBlock()
            vibrateBrief(150)
        } else {
            // Fatal crash!
            run.status = GamePlayStatus.CRASHED
            audioPlayer.playCrash()
            vibrateBrief(400)
            onGameOver?.invoke(run)
        }
    }

    private fun updateCollectibles(
        deltaSeconds: Float,
        worldSpeedPx: Float,
        screenWidth: Float,
        screenHeight: Float,
        playerPxX: Float,
        playerPxY: Float
    ) {
        val iter = collectibles.iterator()
        // Magnet radius increases with coinMagnetLevel
        val magnetRadius = 110f + (profile.coinMagnetLevel - 1) * 55f + if (currentScooter.id == "bijli_ev") 60f else 0f
        val isMagnetEffective = run.isMagnetActive || (profile.coinMagnetLevel > 1)

        while (iter.hasNext()) {
            val c = iter.next()
            c.y += worldSpeedPx * deltaSeconds

            // Magnetic attraction for coins
            if (c.type == CollectibleType.COIN && isMagnetEffective) {
                val dist = hypot(c.x - playerPxX, c.y - playerPxY)
                if (dist < magnetRadius) {
                    val pullSpeed = 450f * deltaSeconds
                    c.x += ((playerPxX - c.x) / dist) * pullSpeed
                    c.y += ((playerPxY - c.y) / dist) * pullSpeed
                }
            }

            // Pickup check
            val pickupDist = hypot(c.x - playerPxX, c.y - playerPxY)
            if (pickupDist < 48f) {
                c.isCollected = true
                iter.remove()
                onCollectiblePicked(c)
            } else if (c.y > screenHeight + 100f) {
                iter.remove()
            }
        }
    }

    private fun onCollectiblePicked(c: Collectible) {
        when (c.type) {
            CollectibleType.COIN -> {
                // Apply deliveryBonusLevel / multiplier
                val bonusMultiplier = if (run.isNitroActive) 2 else 1
                val coinsEarned = 1 * bonusMultiplier
                run.coinsCollected += coinsEarned
                run.currentScore += 50L * bonusMultiplier
                audioPlayer.playCoin()
                vibrateBrief(15)
            }
            CollectibleType.CHAI_NITRO -> {
                triggerChaiNitro()
            }
            CollectibleType.PARCEL -> {
                run.parcelsDelivered++
                val parcelBonusCoins = (5 + (profile.deliveryBonusLevel - 1) * 3).toInt()
                run.coinsCollected += parcelBonusCoins
                run.currentScore += 300L
                audioPlayer.playCoin()
                vibrateBrief(30)
            }
            CollectibleType.SHIELD_BUBBLE -> {
                val maxShield = ((profile.shieldLevel.toInt() + 1) / 2).coerceAtLeast(1)
                if (run.currentShieldCharges < maxShield) {
                    run.currentShieldCharges++
                }
                audioPlayer.playShieldBlock()
                vibrateBrief(40)
            }
        }
        checkMissionProgress()
    }

    private fun updateScenery(deltaSeconds: Float, worldSpeedPx: Float, screenHeight: Float) {
        val iter = sceneryItems.iterator()
        while (iter.hasNext()) {
            val item = iter.next()
            item.y += worldSpeedPx * deltaSeconds
            if (item.y > screenHeight + 150f) {
                iter.remove()
            }
        }
    }

    private fun spawnTraffic(deltaSeconds: Float, screenWidth: Float) {
        trafficSpawnTimer -= (deltaSeconds * 1000f).toLong()
        if (trafficSpawnTimer <= 0) {
            val lane = Random.nextInt(0, 3)
            val type = when (Random.nextInt(0, 100)) {
                in 0..34 -> TrafficType.AUTO_RICKSHAW
                in 35..60 -> TrafficType.TATA_TRUCK
                in 61..80 -> TrafficType.YELLOW_TAXI
                in 81..92 -> TrafficType.COMMUTER_BIKE
                in 93..97 -> TrafficType.STRAY_COW
                else -> TrafficType.POTHOLE
            }
            val speed = when (type) {
                TrafficType.TATA_TRUCK -> 18f
                TrafficType.AUTO_RICKSHAW -> 25f
                TrafficType.YELLOW_TAXI -> 32f
                TrafficType.COMMUTER_BIKE -> 36f
                TrafficType.STRAY_COW, TrafficType.POTHOLE -> 0f
            }
            val width = if (type == TrafficType.TATA_TRUCK) 76f else 54f
            val height = if (type == TrafficType.TATA_TRUCK) 120f else 85f

            trafficList.add(
                TrafficVehicle(
                    id = nextEntityId++,
                    type = type,
                    lane = lane,
                    y = -150f,
                    speed = speed,
                    width = width,
                    height = height
                )
            )
            // Spawn interval adapts with current speed
            trafficSpawnTimer = (Random.nextLong(1000, 2200) * (50f / run.speedKmh)).toLong().coerceIn(600L, 2500L)
        }
    }

    private fun spawnCollectibles(deltaSeconds: Float, screenWidth: Float) {
        collectibleSpawnTimer -= (deltaSeconds * 1000f).toLong()
        if (collectibleSpawnTimer <= 0) {
            val lane = Random.nextInt(0, 3)
            val x = getLaneCenterX(lane) * screenWidth
            val type = when (Random.nextInt(0, 100)) {
                in 0..64 -> CollectibleType.COIN
                in 65..82 -> CollectibleType.PARCEL
                in 83..94 -> CollectibleType.CHAI_NITRO
                else -> CollectibleType.SHIELD_BUBBLE
            }

            collectibles.add(
                Collectible(
                    id = nextEntityId++,
                    type = type,
                    x = x,
                    y = -50f,
                    lane = lane
                )
            )
            collectibleSpawnTimer = Random.nextLong(700L, 1600L)
        }
    }

    private fun spawnScenery(deltaSeconds: Float) {
        scenerySpawnTimer -= (deltaSeconds * 1000f).toLong()
        if (scenerySpawnTimer <= 0) {
            val isLeft = Random.nextBoolean()
            val type = when (Random.nextInt(0, 100)) {
                in 0..35 -> SceneryType.PALM_TREE
                in 36..60 -> SceneryType.CHAI_DHABA
                in 61..80 -> SceneryType.MILESTONE_MARKER
                else -> SceneryType.STREET_LAMP
            }
            val text = when (type) {
                SceneryType.MILESTONE_MARKER -> listOf("DELHI 15 KM", "MUMBAI 90 KM", "JAIPUR 40 KM", "AGRA 60 KM").random()
                SceneryType.CHAI_DHABA -> listOf("Desi Chai Tapri", "Sardarji Dhaba", "Chai Shai Point").random()
                else -> ""
            }

            sceneryItems.add(
                SceneryItem(
                    id = nextEntityId++,
                    isLeft = isLeft,
                    y = -100f,
                    type = type,
                    text = text
                )
            )
            scenerySpawnTimer = Random.nextLong(1200L, 2600L)
        }
    }

    private fun checkMissionProgress() {
        val mission = activeMission ?: return
        if (profile.completedMissions.contains(mission.id)) return

        var isCompleted = false
        when (mission.type) {
            MissionType.DISTANCE -> {
                if (run.distanceMeters >= mission.targetValue) isCompleted = true
            }
            MissionType.COLLECT_COINS -> {
                if (run.coinsCollected >= mission.targetValue) isCompleted = true
            }
            MissionType.TIME_TRIAL -> {
                val timeLimitSec = mission.targetValue
                val elapsedSec = runElapsedMs / 1000f
                if (run.distanceMeters >= 600f && elapsedSec <= timeLimitSec) {
                    isCompleted = true
                }
            }
            MissionType.NO_CRASH -> {
                if (run.distanceMeters >= mission.targetValue && run.crashesInRun == 0) {
                    isCompleted = true
                }
            }
            MissionType.USE_HORN -> {
                if (run.hornUses >= mission.targetValue) isCompleted = true
            }
            MissionType.DELIVER_PARCELS -> {
                if (run.parcelsDelivered >= mission.targetValue) isCompleted = true
            }
        }

        if (isCompleted) {
            audioPlayer.playVictory()
            vibrateBrief(200)
            onMissionCompleted?.invoke(mission)
        }
    }

    private fun vibrateBrief(millis: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(millis)
            }
        } catch (e: Exception) {
            // Ignore vibration error on unsupported emulators
        }
    }

    companion object {
        fun getLaneCenterX(lane: Int): Float = when (lane) {
            0 -> 0.22f
            1 -> 0.50f
            else -> 0.78f
        }
    }
}
