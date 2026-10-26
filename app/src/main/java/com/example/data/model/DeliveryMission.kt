package com.example.data.model

enum class MissionType {
    DISTANCE,
    COLLECT_COINS,
    TIME_TRIAL,
    NO_CRASH,
    USE_HORN,
    DELIVER_PARCELS
}

enum class MissionDifficulty(val label: String, val colorHex: Long) {
    EASY("EASY", 0xFF4CAF50),
    MEDIUM("MEDIUM", 0xFFFF9800),
    HARD("HARD", 0xFFF44336),
    LEGENDARY("LEGENDARY", 0xFF9C27B0)
}

data class DeliveryMission(
    val id: String,
    val title: String,
    val hindiTitle: String,
    val description: String,
    val type: MissionType,
    val targetValue: Long,
    val rewardCoins: Long,
    val difficulty: MissionDifficulty,
    val unlockRequirement: String = ""
)

object MissionCatalog {
    val ALL_MISSIONS = listOf(
        DeliveryMission(
            id = "mission_chai_express",
            title = "Cutting Chai Express",
            hindiTitle = "कटिंग चाय एक्सप्रेस",
            description = "Navigate through traffic and travel 500 meters to deliver morning chai.",
            type = MissionType.DISTANCE,
            targetValue = 500L,
            rewardCoins = 100L,
            difficulty = MissionDifficulty.EASY
        ),
        DeliveryMission(
            id = "mission_coin_rush",
            title = "Tip Collector Rush",
            hindiTitle = "टिप कलेक्टर",
            description = "Gather 30 golden coins from the highway in a single delivery run.",
            type = MissionType.COLLECT_COINS,
            targetValue = 30L,
            rewardCoins = 150L,
            difficulty = MissionDifficulty.EASY
        ),
        DeliveryMission(
            id = "mission_samosa_speed",
            title = "Hot Samosa Time Trial",
            hindiTitle = "गरम समोसा डिलीवरी",
            description = "Deliver piping hot samosas within 40 seconds before they get cold!",
            type = MissionType.TIME_TRIAL,
            targetValue = 40L, // seconds limit to reach 600m
            rewardCoins = 220L,
            difficulty = MissionDifficulty.MEDIUM
        ),
        DeliveryMission(
            id = "mission_horn_master",
            title = "Horn OK Please!",
            hindiTitle = "हॉर्न ओके प्लीज!",
            description = "Blast the sonic horn 6 times to clear stubborn autos and tata trucks.",
            type = MissionType.USE_HORN,
            targetValue = 6L,
            rewardCoins = 180L,
            difficulty = MissionDifficulty.EASY
        ),
        DeliveryMission(
            id = "mission_fragile_matka",
            title = "Fragile Clay Pots (Matka)",
            hindiTitle = "मिट्टी का मटका",
            description = "Travel 800 meters with zero crashes. Any hit breaks the fragile cargo!",
            type = MissionType.NO_CRASH,
            targetValue = 800L,
            rewardCoins = 350L,
            difficulty = MissionDifficulty.HARD
        ),
        DeliveryMission(
            id = "mission_parcel_frenzy",
            title = "E-Commerce Mega Drop",
            hindiTitle = "पार्सल मेला",
            description = "Pick up and deliver 4 delivery boxes in a single run.",
            type = MissionType.DELIVER_PARCELS,
            targetValue = 4L,
            rewardCoins = 300L,
            difficulty = MissionDifficulty.MEDIUM
        ),
        DeliveryMission(
            id = "mission_grand_trunk",
            title = "Grand Trunk Highway Legend",
            hindiTitle = "जी.टी. रोड उस्ताद",
            description = "Survive the bustling highway traffic for 2000 meters in a single run.",
            type = MissionType.DISTANCE,
            targetValue = 2000L,
            rewardCoins = 600L,
            difficulty = MissionDifficulty.LEGENDARY,
            unlockRequirement = "Unlocks Rocket Delivery Express discount"
        )
    )

    fun getMission(id: String): DeliveryMission? = ALL_MISSIONS.firstOrNull { it.id == id }
}
