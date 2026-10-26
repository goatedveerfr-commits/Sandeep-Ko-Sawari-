package com.example.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.sin

@Composable
fun GameCanvas(
    engine: GameEngine,
    scrollOffset: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val normX = offset.x / size.width
                    engine.setLaneByTouch(normX)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount.x < -15) {
                        engine.steerLeft()
                    } else if (dragAmount.x > 15) {
                        engine.steerRight()
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // 1. Draw Roadside Landscape & Road
        drawIndianRoad(w, h, scrollOffset)

        // 2. Draw Roadside Scenery (Dhabas, trees, milestones)
        for (item in engine.sceneryItems) {
            drawSceneryItem(item, w)
        }

        // 3. Draw Collectibles (Coins, Chai cups, Parcels, Shields)
        for (c in engine.collectibles) {
            drawCollectible(c)
        }

        // 4. Draw Traffic Vehicles (Trucks, Autos, Cabs, Cows)
        for (v in engine.trafficList) {
            drawTrafficVehicle(v, w)
        }

        // 5. Draw Horn Shockwave Blast Rings
        for (sw in engine.shockwaves) {
            drawCircle(
                color = Color(0xFFFFD54F).copy(alpha = sw.alpha * 0.7f),
                radius = sw.radius,
                center = Offset(sw.originX, sw.originY),
                style = Stroke(width = 6f)
            )
            drawCircle(
                color = Color(0xFFFF6F00).copy(alpha = sw.alpha * 0.4f),
                radius = sw.radius * 0.75f,
                center = Offset(sw.originX, sw.originY),
                style = Stroke(width = 3f)
            )
        }

        // 6. Draw Sandeep on his Scooter
        val playerPxX = engine.playerVisualX * w
        val playerPxY = h * 0.78f
        drawSandeepScooter(
            x = playerPxX,
            y = playerPxY,
            leanAngle = engine.playerLeanAngle,
            engine = engine
        )

        // 7. Dynamic Weather Atmospheric Layers
        val weather = engine.weatherController.currentWeather
        if (weather == WeatherCondition.HEAVY_SMOG) {
            // Atmospheric Delhi Smog haze across top of the road
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xD98D6E63).copy(alpha = 0.75f),
                        Color(0xB364748B).copy(alpha = 0.50f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.65f
                ),
                topLeft = Offset.Zero,
                size = Size(w, h * 0.65f)
            )
        } else if (weather == WeatherCondition.MONSOON_RAIN) {
            // Monsoon Rain Streaks
            val rainDrops = engine.weatherController.rainDrops
            for (drop in rainDrops) {
                val rx = drop.x * w
                val ry = drop.y * h
                drawLine(
                    color = Color(0xB3E0F2FE),
                    start = Offset(rx, ry),
                    end = Offset(rx + 4f, ry + drop.length),
                    strokeWidth = 1.8f
                )
            }
            // Wet sheen road tint
            drawRect(
                color = Color(0x180284C7),
                topLeft = Offset.Zero,
                size = Size(w, h)
            )
        }
    }
}

private fun DrawScope.drawIndianRoad(w: Float, h: Float, scrollOffset: Float) {
    // Roadside Sandy Shoulder / Greenery
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF8D6E63), Color(0xFF5D4037))
        ),
        topLeft = Offset(0f, 0f),
        size = Size(w * 0.08f, h)
    )
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF5D4037), Color(0xFF8D6E63))
        ),
        topLeft = Offset(w * 0.92f, 0f),
        size = Size(w * 0.08f, h)
    )

    // Main Asphalt Highway
    val roadLeft = w * 0.08f
    val roadWidth = w * 0.84f
    drawRect(
        color = Color(0xFF263238),
        topLeft = Offset(roadLeft, 0f),
        size = Size(roadWidth, h)
    )

    // White Boundary Lines
    drawLine(
        color = Color.White.copy(alpha = 0.9f),
        start = Offset(roadLeft + 4f, 0f),
        end = Offset(roadLeft + 4f, h),
        strokeWidth = 6f
    )
    drawLine(
        color = Color.White.copy(alpha = 0.9f),
        start = Offset(roadLeft + roadWidth - 4f, 0f),
        end = Offset(roadLeft + roadWidth - 4f, h),
        strokeWidth = 6f
    )

    // Lane Divider Dashes (2 divider lines for 3 lanes)
    val lane1X = roadLeft + (roadWidth / 3f)
    val lane2X = roadLeft + (roadWidth * 2f / 3f)

    val dashLength = 48f
    val gapLength = 36f
    val totalDashPeriod = dashLength + gapLength
    val offsetMod = scrollOffset % totalDashPeriod

    var curY = -totalDashPeriod + offsetMod
    while (curY < h + totalDashPeriod) {
        // Yellow center dashes
        drawLine(
            color = Color(0xFFFFD54F),
            start = Offset(lane1X, curY),
            end = Offset(lane1X, curY + dashLength),
            strokeWidth = 5f
        )
        drawLine(
            color = Color(0xFFFFD54F),
            start = Offset(lane2X, curY),
            end = Offset(lane2X, curY + dashLength),
            strokeWidth = 5f
        )
        curY += totalDashPeriod
    }
}

private fun DrawScope.drawSceneryItem(item: SceneryItem, w: Float) {
    if (item.y < -80f || item.y > size.height + 80f) return

    val x = if (item.isLeft) w * 0.035f else w * 0.965f
    val y = item.y

    when (item.type) {
        SceneryType.PALM_TREE -> {
            drawLine(
                color = Color(0xFF795548),
                start = Offset(x, y + 25f),
                end = Offset(x, y - 25f),
                strokeWidth = 7f
            )
            drawCircle(color = Color(0xFF2E7D32), radius = 22f, center = Offset(x, y - 25f))
            drawCircle(color = Color(0xFF388E3C), radius = 16f, center = Offset(x - 10f, y - 32f))
            drawCircle(color = Color(0xFF388E3C), radius = 16f, center = Offset(x + 10f, y - 32f))
        }
        SceneryType.MILESTONE_MARKER -> {
            val mw = 26f
            val mh = 36f
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(x - mw / 2, y - mh / 2),
                size = Size(mw, mh),
                cornerRadius = CornerRadius(12f, 12f)
            )
            drawRoundRect(
                color = Color(0xFFFFB300),
                topLeft = Offset(x - mw / 2, y - mh / 2),
                size = Size(mw, mh * 0.45f),
                cornerRadius = CornerRadius(12f, 12f)
            )
        }
        SceneryType.CHAI_DHABA -> {
            val sw = 36f
            val sh = 40f
            drawRect(
                color = Color(0xFFBCAAA4),
                topLeft = Offset(x - sw / 2, y - sh / 2),
                size = Size(sw, sh)
            )
            drawRect(
                color = Color(0xFF1976D2),
                topLeft = Offset(x - sw / 2 - 4f, y - sh / 2 - 6f),
                size = Size(sw + 8f, 12f)
            )
        }
        else -> {
            drawCircle(color = Color(0xFFFFF176), radius = 8f, center = Offset(x, y))
            drawLine(color = Color(0xFF37474F), start = Offset(x, y), end = Offset(x, y + 24f), strokeWidth = 3f)
        }
    }
}

private fun DrawScope.drawCollectible(c: Collectible) {
    if (c.y < -40f || c.y > size.height + 40f) return

    when (c.type) {
        CollectibleType.COIN -> {
            // Shiny Golden Indian Rupee Coin
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = 16f,
                center = Offset(c.x, c.y)
            )
            drawCircle(
                color = Color(0xFFFF8F00),
                radius = 16f,
                center = Offset(c.x, c.y),
                style = Stroke(width = 2.5f)
            )
            // Crisp Vector Rupee Symbol
            drawLine(
                color = Color(0xFFB78103),
                start = Offset(c.x - 6f, c.y - 6f),
                end = Offset(c.x + 6f, c.y - 6f),
                strokeWidth = 2.2f
            )
            drawLine(
                color = Color(0xFFB78103),
                start = Offset(c.x - 6f, c.y - 2f),
                end = Offset(c.x + 4f, c.y - 2f),
                strokeWidth = 2.2f
            )
            drawLine(
                color = Color(0xFFB78103),
                start = Offset(c.x - 2f, c.y - 6f),
                end = Offset(c.x - 2f, c.y + 2f),
                strokeWidth = 2.2f
            )
            drawLine(
                color = Color(0xFFB78103),
                start = Offset(c.x - 2f, c.y + 2f),
                end = Offset(c.x + 5f, c.y + 8f),
                strokeWidth = 2.2f
            )
        }
        CollectibleType.CHAI_NITRO -> {
            drawRoundRect(
                color = Color(0xFFFFF9C4),
                topLeft = Offset(c.x - 10f, c.y - 12f),
                size = Size(20f, 24f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = Color(0xFFD84315),
                topLeft = Offset(c.x - 8f, c.y - 4f),
                size = Size(16f, 14f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawCircle(color = Color.White.copy(alpha = 0.7f), radius = 3f, center = Offset(c.x - 3f, c.y - 16f))
            drawCircle(color = Color.White.copy(alpha = 0.7f), radius = 3.5f, center = Offset(c.x + 3f, c.y - 19f))
        }
        CollectibleType.PARCEL -> {
            drawRoundRect(
                color = Color(0xFFFFB74D),
                topLeft = Offset(c.x - 14f, c.y - 14f),
                size = Size(28f, 28f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = Color(0xFFE65100),
                topLeft = Offset(c.x - 14f, c.y - 14f),
                size = Size(28f, 28f),
                cornerRadius = CornerRadius(5f, 5f),
                style = Stroke(width = 2f)
            )
            drawLine(
                color = Color(0xFFD32F2F),
                start = Offset(c.x, c.y - 14f),
                end = Offset(c.x, c.y + 14f),
                strokeWidth = 4f
            )
            drawLine(
                color = Color(0xFFD32F2F),
                start = Offset(c.x - 14f, c.y),
                end = Offset(c.x + 14f, c.y),
                strokeWidth = 4f
            )
        }
        CollectibleType.SHIELD_BUBBLE -> {
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.35f),
                radius = 18f,
                center = Offset(c.x, c.y)
            )
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = 18f,
                center = Offset(c.x, c.y),
                style = Stroke(width = 3f)
            )
            drawLine(
                color = Color.White,
                start = Offset(c.x, c.y - 8f),
                end = Offset(c.x, c.y + 8f),
                strokeWidth = 3f
            )
            drawLine(
                color = Color.White,
                start = Offset(c.x - 8f, c.y),
                end = Offset(c.x + 8f, c.y),
                strokeWidth = 3f
            )
        }
    }
}

private fun DrawScope.drawTrafficVehicle(v: TrafficVehicle, w: Float) {
    if (v.y < -130f || v.y > size.height + 130f) return

    val x = GameEngine.getLaneCenterX(v.lane) * w
    val y = v.y

    when (v.type) {
        TrafficType.TATA_TRUCK -> {
            val tw = 72f
            val th = 118f
            // Truck Wooden Cargo Bed (Vibrant Green & Blue)
            drawRoundRect(
                color = Color(0xFF1B5E20),
                topLeft = Offset(x - tw / 2, y - th / 2),
                size = Size(tw, th * 0.72f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Truck Cabin (Yellow/Red)
            drawRoundRect(
                color = Color(0xFFE53935),
                topLeft = Offset(x - tw / 2, y + th * 0.22f - th / 2),
                size = Size(tw, th * 0.28f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Windshield
            drawRoundRect(
                color = Color(0xFFB0BEC5),
                topLeft = Offset(x - tw / 2 + 8f, y + th * 0.28f - th / 2),
                size = Size(tw - 16f, 14f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            // Tailgate Indian Pattern & Reflectors
            drawRoundRect(
                color = Color(0xFFFFD600),
                topLeft = Offset(x - tw / 2 + 4f, y - th / 2 + 4f),
                size = Size(tw - 8f, 20f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Ornate chevron stripes representing "HORN OK PLEASE"
            drawLine(color = Color(0xFFD50000), start = Offset(x - 24f, y - th / 2 + 10f), end = Offset(x - 16f, y - th / 2 + 18f), strokeWidth = 3f)
            drawLine(color = Color(0xFFD50000), start = Offset(x - 16f, y - th / 2 + 18f), end = Offset(x - 8f, y - th / 2 + 10f), strokeWidth = 3f)
            drawCircle(color = Color(0xFF29B6F6), radius = 5f, center = Offset(x, y - th / 2 + 14f))
            drawLine(color = Color(0xFFD50000), start = Offset(x + 8f, y - th / 2 + 10f), end = Offset(x + 16f, y - th / 2 + 18f), strokeWidth = 3f)
            drawLine(color = Color(0xFFD50000), start = Offset(x + 16f, y - th / 2 + 18f), end = Offset(x + 24f, y - th / 2 + 10f), strokeWidth = 3f)

            // Wheels
            drawRect(color = Color(0xFF212121), topLeft = Offset(x - tw / 2 - 4f, y - 20f), size = Size(5f, 24f))
            drawRect(color = Color(0xFF212121), topLeft = Offset(x + tw / 2 - 1f, y - 20f), size = Size(5f, 24f))
        }
        TrafficType.AUTO_RICKSHAW -> {
            val aw = 52f
            val ah = 76f
            drawRoundRect(
                color = Color(0xFFFFD600),
                topLeft = Offset(x - aw / 2, y - ah / 2),
                size = Size(aw, ah * 0.55f),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = Color(0xFF2E7D32),
                topLeft = Offset(x - aw / 2 + 4f, y + ah * 0.55f - ah / 2),
                size = Size(aw - 8f, ah * 0.45f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRect(
                color = Color(0xFFECEFF1),
                topLeft = Offset(x - aw / 2 + 8f, y + 5f),
                size = Size(aw - 16f, 12f)
            )
            drawCircle(color = Color(0xFFFFF9C4), radius = 6f, center = Offset(x, y + ah / 2 - 2f))
            drawRect(color = Color.Black, topLeft = Offset(x - 7f, y - 10f), size = Size(14f, 8f))
        }
        TrafficType.YELLOW_TAXI -> {
            val cw = 56f
            val ch = 84f
            drawRoundRect(
                color = Color(0xFF212121),
                topLeft = Offset(x - cw / 2, y - ch / 2),
                size = Size(cw, ch),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = Color(0xFFFFEB3B),
                topLeft = Offset(x - cw / 2 + 5f, y - ch / 2 + 15f),
                size = Size(cw - 10f, ch * 0.45f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Taxi Top Carrier Sign with checkered pattern
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(x - 12f, y - 6f),
                size = Size(24f, 10f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRect(color = Color.Black, topLeft = Offset(x - 8f, y - 4f), size = Size(5f, 6f))
            drawRect(color = Color.Black, topLeft = Offset(x + 3f, y - 4f), size = Size(5f, 6f))
        }
        TrafficType.COMMUTER_BIKE -> {
            val bw = 30f
            val bh = 60f
            drawCircle(color = Color(0xFF1E88E5), radius = 9f, center = Offset(x, y - 10f))
            drawRoundRect(
                color = Color(0xFFD32F2F),
                topLeft = Offset(x - 6f, y - bh / 2),
                size = Size(12f, bh),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawLine(color = Color.Black, start = Offset(x - bw / 2, y + 10f), end = Offset(x + bw / 2, y + 10f), strokeWidth = 3f)
        }
        TrafficType.STRAY_COW -> {
            drawRoundRect(
                color = Color(0xFFD7CCC8),
                topLeft = Offset(x - 22f, y - 18f),
                size = Size(44f, 36f),
                cornerRadius = CornerRadius(14f, 14f)
            )
            drawCircle(color = Color(0xFFBCAAA4), radius = 12f, center = Offset(x, y - 22f))
            drawLine(color = Color(0xFF3E2723), start = Offset(x - 8f, y - 22f), end = Offset(x - 16f, y - 32f), strokeWidth = 3.5f)
            drawLine(color = Color(0xFF3E2723), start = Offset(x + 8f, y - 22f), end = Offset(x + 16f, y - 32f), strokeWidth = 3.5f)
        }
        TrafficType.POTHOLE -> {
            drawOval(
                color = Color(0xFF0F172A),
                topLeft = Offset(x - 24f, y - 14f),
                size = Size(48f, 28f)
            )
            drawOval(
                color = Color(0xFF455A64),
                topLeft = Offset(x - 24f, y - 14f),
                size = Size(48f, 28f),
                style = Stroke(width = 2.5f)
            )
        }
    }

    // Alerted "!" icon when blasted by horn (Rendered safely using vector lines)
    if (v.isAlerted) {
        val alertY = y - v.height / 2 - 14f
        if (alertY > 10f) {
            drawCircle(color = Color(0xFFFF5252), radius = 12f, center = Offset(x, alertY))
            drawLine(
                color = Color.White,
                start = Offset(x, alertY - 6f),
                end = Offset(x, alertY + 1f),
                strokeWidth = 3.2f
            )
            drawCircle(color = Color.White, radius = 1.6f, center = Offset(x, alertY + 5.5f))
        }
    }
}

private fun DrawScope.drawSandeepScooter(
    x: Float,
    y: Float,
    leanAngle: Float,
    engine: GameEngine
) {
    val scooter = engine.currentScooter
    val primaryColor = Color(scooter.primaryColorHex)
    val accentColor = Color(scooter.accentColorHex)

    rotate(degrees = leanAngle, pivot = Offset(x, y)) {
        // Headlight beam illuminating the road ahead
        val beamPath = Path().apply {
            moveTo(x - 12f, y - 40f)
            lineTo(x - 90f, y - 280f)
            lineTo(x + 90f, y - 280f)
            lineTo(x + 12f, y - 40f)
            close()
        }
        drawPath(
            path = beamPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x35FFF9C4), Color(0x00FFF9C4)),
                startY = y - 40f,
                endY = y - 280f
            )
        )

        // Nitro exhaust fire trails
        if (engine.run.isNitroActive) {
            val flameLen = 45f + (sin(System.currentTimeMillis() * 0.05f) * 12f)
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFD600), Color(0xFFFF3D00), Color.Transparent),
                    startY = y + 25f,
                    endY = y + 25f + flameLen
                ),
                topLeft = Offset(x - 12f, y + 25f),
                size = Size(8f, flameLen),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFD600), Color(0xFFFF3D00), Color.Transparent),
                    startY = y + 25f,
                    endY = y + 25f + flameLen
                ),
                topLeft = Offset(x + 4f, y + 25f),
                size = Size(8f, flameLen),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }

        // Scooter Rear Wheel
        drawRoundRect(
            color = Color(0xFF212121),
            topLeft = Offset(x - 6f, y + 16f),
            size = Size(12f, 26f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Scooter Main Body Chassis
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(x - 16f, y - 22f),
            size = Size(32f, 44f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Floorboard Footrest
        drawRoundRect(
            color = accentColor,
            topLeft = Offset(x - 14f, y - 8f),
            size = Size(28f, 16f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // Front Apron & Steering Column
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(x - 12f, y - 46f),
            size = Size(24f, 28f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Front Wheel
        drawRoundRect(
            color = Color(0xFF212121),
            topLeft = Offset(x - 5f, y - 62f),
            size = Size(10f, 22f),
            cornerRadius = CornerRadius(5f, 5f)
        )

        // Handlebars with side mirrors
        drawLine(
            color = Color(0xFF263238),
            start = Offset(x - 28f, y - 42f),
            end = Offset(x + 28f, y - 42f),
            strokeWidth = 4.5f
        )
        // Mirrors
        drawCircle(color = Color(0xFFECEFF1), radius = 5f, center = Offset(x - 28f, y - 45f))
        drawCircle(color = Color(0xFFECEFF1), radius = 5f, center = Offset(x + 28f, y - 45f))

        // Bright Headlight
        drawCircle(color = Color(0xFFFFF9C4), radius = 7f, center = Offset(x, y - 48f))

        // Sandeep's Delivery Cargo Box on the back
        drawRoundRect(
            color = Color(0xFFFF9800),
            topLeft = Offset(x - 18f, y + 4f),
            size = Size(36f, 26f),
            cornerRadius = CornerRadius(5f, 5f)
        )
        drawRoundRect(
            color = Color(0xFFE65100),
            topLeft = Offset(x - 18f, y + 4f),
            size = Size(36f, 26f),
            cornerRadius = CornerRadius(5f, 5f),
            style = Stroke(width = 2f)
        )
        // Delivery strap tape and postal stamp
        drawLine(color = Color(0xFFD84315), start = Offset(x - 18f, y + 17f), end = Offset(x + 18f, y + 17f), strokeWidth = 3f)
        drawRect(color = Color.White, topLeft = Offset(x - 8f, y + 8f), size = Size(16f, 10f))
        drawRect(color = Color(0xFF1976D2), topLeft = Offset(x - 6f, y + 10f), size = Size(12f, 6f))

        // Rider Sandeep Torso (Sporty jacket)
        drawCircle(
            color = Color(0xFFD50000),
            radius = 12f,
            center = Offset(x, y - 16f)
        )

        // Rider Helmet with dark visor
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = 11f,
            center = Offset(x, y - 28f)
        )
        drawRoundRect(
            color = Color(0xFF212121),
            topLeft = Offset(x - 8f, y - 36f),
            size = Size(16f, 7f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // Active Shield Energy Barrier
        if (engine.run.currentShieldCharges > 0) {
            val pulse = (sin(System.currentTimeMillis() * 0.008f) * 3f)
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.28f),
                radius = 54f + pulse,
                center = Offset(x, y - 10f)
            )
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = 54f + pulse,
                center = Offset(x, y - 10f),
                style = Stroke(width = 3.5f)
            )
        }
    }
}
