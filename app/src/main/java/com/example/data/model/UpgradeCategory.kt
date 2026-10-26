package com.example.data.model

enum class UpgradeType {
    // Scooter Performance
    TOP_SPEED,
    ACCELERATION,
    HANDLING,

    // Defensive Capabilities
    HORN_BLAST,
    TEMPORARY_SHIELD,

    // Sandeep's Personal Abilities
    COIN_MAGNET,
    SCORE_MULTIPLIER,
    DELIVERY_BONUS
}

enum class UpgradeSection(val title: String, val subtitle: String) {
    PERFORMANCE("Scooter Performance", "Speed, pickup & traffic agility"),
    DEFENSE("Defensive Capabilities", "Horn shockwave & crash armor"),
    ABILITIES("Sandeep's Abilities", "Magnets, multipliers & delivery tips")
}

data class UpgradeItem(
    val type: UpgradeType,
    val section: UpgradeSection,
    val title: String,
    val hindiTitle: String,
    val description: String,
    val currentLevel: Long,
    val maxLevel: Long = 5L
) {
    val isMaxLevel: Boolean
        get() = currentLevel >= maxLevel

    val nextLevelCost: Long
        get() = when (currentLevel) {
            1L -> 80L
            2L -> 160L
            3L -> 320L
            4L -> 640L
            else -> 0L
        }

    fun getEffectSummary(level: Long = currentLevel): String = when (type) {
        UpgradeType.TOP_SPEED -> "+${(level - 1) * 15}% Top Velocity (Max ${80 + (level - 1) * 18} km/h)"
        UpgradeType.ACCELERATION -> "+${(level - 1) * 20}% Pickup Acceleration"
        UpgradeType.HANDLING -> "+${(level - 1) * 18}% Steer Agility & Turn Recovery"
        UpgradeType.HORN_BLAST -> "Horn blast radius ${(120 + (level - 1) * 50)}px, ${(6.0 - (level - 1) * 0.8).coerceAtLeast(2.0)}s cooldown"
        UpgradeType.TEMPORARY_SHIELD -> "${((level + 1) / 2)} Crash Absorb Charges (Energy Barrier)"
        UpgradeType.COIN_MAGNET -> "${100 + (level - 1) * 55}px Magnetic Coin Attraction"
        UpgradeType.SCORE_MULTIPLIER -> "${5 + (level - 1) * 2.5}s Chai Power 2x Duration"
        UpgradeType.DELIVERY_BONUS -> "+${(level - 1) * 25}% Bonus Coins on Completed Parcels"
    }
}
