package com.example.data.model

data class Scooter(
    val id: String,
    val name: String,
    val hindiTitle: String,
    val description: String,
    val cost: Long,
    val baseSpeed: Float,        // km/h baseline multiplier
    val baseAcceleration: Float, // pickup speed
    val baseHandling: Float,     // steer responsiveness
    val specialPerk: String,
    val primaryColorHex: Long,
    val accentColorHex: Long
)

object ScooterCatalog {
    val ALL_SCOOTERS = listOf(
        Scooter(
            id = "chetak_125",
            name = "Desi Chetak 125",
            hindiTitle = "देसी चेतक",
            description = "The classic retro two-stroke legend. Steady handling, reliable in tight Indian gullies.",
            cost = 0L,
            baseSpeed = 1.0f,
            baseAcceleration = 1.0f,
            baseHandling = 1.0f,
            specialPerk = "Balanced cruiser & nostalgic two-stroke roar",
            primaryColorHex = 0xFF00ACC1, // Teal cyan
            accentColorHex = 0xFFFFB300   // Gold
        ),
        Scooter(
            id = "activa_super",
            name = "Speedy Activa Super",
            hindiTitle = "स्पीड एक्टिवा",
            description = "India's favorite nimble commuter. Cuts through jammed traffic with lightning lane changes.",
            cost = 300L,
            baseSpeed = 1.25f,
            baseAcceleration = 1.3f,
            baseHandling = 1.4f,
            specialPerk = "+25% faster lane swerve agility",
            primaryColorHex = 0xFFFFD600, // Vibrant yellow
            accentColorHex = 0xFF212121   // Jet black
        ),
        Scooter(
            id = "bullet_350",
            name = "Dhoom Bullet 350",
            hindiTitle = "धूम बुलेट ३५०",
            description = "Heavy cast-iron thumping beast. Powerful momentum that pushes through headwind easily.",
            cost = 800L,
            baseSpeed = 1.5f,
            baseAcceleration = 1.2f,
            baseHandling = 1.1f,
            specialPerk = "Massive top speed & thunderous exhaust",
            primaryColorHex = 0xFF212121, // Black
            accentColorHex = 0xFFD4AF37   // Royal gold
        ),
        Scooter(
            id = "bijli_ev",
            name = "Electric Bijli E-Scooter",
            hindiTitle = "बिजली ई-स्कूटर",
            description = "High-voltage futuristic EV. Instant electric pickup and magnetic induction for collecting coins.",
            cost = 1500L,
            baseSpeed = 1.4f,
            baseAcceleration = 1.6f,
            baseHandling = 1.35f,
            specialPerk = "+30% passive coin magnet pull",
            primaryColorHex = 0xFF00E676, // Neon green
            accentColorHex = 0xFF00B0FF   // Electric blue
        ),
        Scooter(
            id = "rocket_express",
            name = "Rocket Delivery Express",
            hindiTitle = "रॉकेट डिलीवरी",
            description = "Twin-turbo customized delivery monster equipped with heated parcel box & nitro exhaust.",
            cost = 3000L,
            baseSpeed = 1.8f,
            baseAcceleration = 1.75f,
            baseHandling = 1.5f,
            specialPerk = "+50% Nitro boost speed & double delivery cargo",
            primaryColorHex = 0xFFFF1744, // Flaming red
            accentColorHex = 0xFFFFD700   // Flame yellow
        )
    )

    fun getScooter(id: String): Scooter {
        return ALL_SCOOTERS.firstOrNull { it.id == id } ?: ALL_SCOOTERS[0]
    }
}
