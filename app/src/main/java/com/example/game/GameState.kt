package com.example.game

enum class TrafficType {
    AUTO_RICKSHAW,
    TATA_TRUCK,
    YELLOW_TAXI,
    COMMUTER_BIKE,
    STRAY_COW,
    POTHOLE
}

data class TrafficVehicle(
    val id: Long,
    val type: TrafficType,
    var lane: Int,         // 0 (Left), 1 (Center), 2 (Right)
    var y: Float,          // Screen Y coordinate in pixels
    var speed: Float,      // Downward scrolling velocity
    var isAlerted: Boolean = false, // When blasted by horn, startled and swerving
    var swerveTargetLane: Int? = null,
    val width: Float = 60f,
    val height: Float = 100f
)

enum class CollectibleType {
    COIN,
    CHAI_NITRO,
    PARCEL,
    SHIELD_BUBBLE
}

data class Collectible(
    val id: Long,
    val type: CollectibleType,
    var x: Float,
    var y: Float,
    val lane: Int,
    var isCollected: Boolean = false,
    val rotation: Float = 0f
)

data class HornShockwave(
    val originX: Float,
    val originY: Float,
    var radius: Float,
    val maxRadius: Float,
    var alpha: Float = 1f
)

data class SceneryItem(
    val id: Long,
    val isLeft: Boolean,
    var y: Float,
    val type: SceneryType,
    val text: String = ""
)

enum class SceneryType {
    CHAI_DHABA,
    PALM_TREE,
    MILESTONE_MARKER,
    STREET_LAMP,
    COLORFUL_BANNER
}

enum class GamePlayStatus {
    READY,
    RIDING,
    PAUSED,
    CRASHED
}

data class ActiveGameRun(
    var status: GamePlayStatus = GamePlayStatus.READY,
    var distanceMeters: Float = 0f,
    var speedKmh: Float = 45f,
    var coinsCollected: Int = 0,
    var parcelsDelivered: Int = 0,
    var hornUses: Int = 0,
    var nitroUses: Int = 0,
    var crashesInRun: Int = 0,
    var currentShieldCharges: Int = 1,
    var isNitroActive: Boolean = false,
    var nitroTimeRemainingMs: Long = 0L,
    var isMagnetActive: Boolean = false,
    var magnetTimeRemainingMs: Long = 0L,
    var hornCooldownMs: Long = 0L,
    var hornCooldownMaxMs: Long = 4000L,
    var currentScore: Long = 0L,
    var targetDeliveryDistance: Float = 600f,
    var activeMissionBanner: String? = null,
    var currentWeather: WeatherCondition = WeatherCondition.CLEAR
)
