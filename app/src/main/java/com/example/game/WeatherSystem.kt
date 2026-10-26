package com.example.game

import kotlin.random.Random

enum class WeatherCondition(
    val title: String,
    val hindiTitle: String,
    val iconEmoji: String,
    val visibilityAlpha: Float, // 1.0f = crystal clear, 0.5f = heavy haze
    val handlingTraction: Float, // 1.0f = dry grip, 0.75f = slick skid
    val description: String
) {
    CLEAR(
        title = "Sunny Highway",
        hindiTitle = "धूप खिली सड़क",
        iconEmoji = "☀️",
        visibilityAlpha = 1.0f,
        handlingTraction = 1.0f,
        description = "Dry asphalt with maximum traction and clear open visibility."
    ),
    MONSOON_RAIN(
        title = "Monsoon Downpour",
        hindiTitle = "मानसूनी बारिश",
        iconEmoji = "🌧️",
        visibilityAlpha = 0.82f,
        handlingTraction = 0.74f,
        description = "Slick wet roads with water puddles. Scooter requires smoother steering to avoid sliding."
    ),
    HEAVY_SMOG(
        title = "Delhi Winter Smog",
        hindiTitle = "घना कोहरा व धुंध",
        iconEmoji = "🌫️",
        visibilityAlpha = 0.60f,
        handlingTraction = 0.95f,
        description = "Dense atmospheric haze. Vehicles emerge suddenly; headlight and horn are essential."
    )
}

data class RainDrop(
    var x: Float,
    var y: Float,
    var length: Float,
    var speed: Float
)

class WeatherController {
    var currentWeather: WeatherCondition = WeatherCondition.CLEAR
    val rainDrops = mutableListOf<RainDrop>()
    private var weatherTimerMs = 0L

    init {
        // Pre-initialize rain drop pool
        for (i in 0 until 90) {
            rainDrops.add(
                RainDrop(
                    x = Random.nextFloat(),
                    y = Random.nextFloat(),
                    length = Random.nextFloat() * 18f + 12f,
                    speed = Random.nextFloat() * 400f + 650f
                )
            )
        }
    }

    fun update(deltaSeconds: Float, screenHeight: Float) {
        // Update rain particles if raining
        if (currentWeather == WeatherCondition.MONSOON_RAIN) {
            for (drop in rainDrops) {
                drop.y += (drop.speed * deltaSeconds) / screenHeight
                drop.x += (deltaSeconds * 0.08f) // Slight wind angle
                if (drop.y > 1f) {
                    drop.y = -0.05f
                    drop.x = Random.nextFloat()
                }
                if (drop.x > 1f) drop.x = 0f
            }
        }

        // Slowly transition weather dynamically every 35-50 seconds during a long run
        weatherTimerMs += (deltaSeconds * 1000f).toLong()
        if (weatherTimerMs >= 40000L) {
            weatherTimerMs = 0L
            cycleNextWeather()
        }
    }

    fun setWeather(weather: WeatherCondition) {
        currentWeather = weather
        weatherTimerMs = 0L
    }

    fun cycleNextWeather() {
        val all = WeatherCondition.entries
        val nextIdx = (currentWeather.ordinal + 1) % all.size
        currentWeather = all[nextIdx]
    }
}
