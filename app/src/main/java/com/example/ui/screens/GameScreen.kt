package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlayerProfile
import com.example.game.ActiveGameRun
import com.example.game.GameCanvas
import com.example.game.GameEngine
import com.example.game.GamePlayStatus
import com.example.ui.viewmodel.GameScreenTab
import com.example.ui.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    profile: PlayerProfile,
    isSoundMuted: Boolean,
    onExitToLobby: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val engine = viewModel.engine
    var scrollOffset by remember { mutableFloatStateOf(0f) }
    var frameTick by remember { mutableStateOf(0L) }

    // Start game on launch if ready
    LaunchedEffect(profile.selectedScooterId) {
        if (engine.run.status == GamePlayStatus.READY) {
            viewModel.startNewGame()
        }
    }

    // 60 FPS Game Loop
    LaunchedEffect(engine.run.status) {
        var lastNanos = System.nanoTime()
        while (true) {
            withFrameNanos { nowNanos ->
                val dtSec = ((nowNanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos = nowNanos

                if (engine.run.status == GamePlayStatus.RIDING) {
                    scrollOffset += engine.run.speedKmh * 14.5f * dtSec
                    // Trigger canvas redraw
                    frameTick = nowNanos
                }
            }
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize().background(Color.Black)) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        // Update physics loop based on frameTick
        LaunchedEffect(frameTick) {
            if (engine.run.status == GamePlayStatus.RIDING && screenWidth > 0 && screenHeight > 0) {
                engine.update(1f / 60f, screenWidth, screenHeight)
            }
        }

        // 1. Game Canvas Drawing Layer
        GameCanvas(
            engine = engine,
            scrollOffset = scrollOffset,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Heads-Up Display (HUD) Layer
        GameHud(
            engine = engine,
            profile = profile,
            isSoundMuted = isSoundMuted,
            onToggleSound = { viewModel.toggleSound() },
            onPause = {
                if (engine.run.status == GamePlayStatus.RIDING) {
                    engine.run.status = GamePlayStatus.PAUSED
                } else if (engine.run.status == GamePlayStatus.PAUSED) {
                    engine.run.status = GamePlayStatus.RIDING
                }
            },
            onExit = onExitToLobby,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        )

        // 3. On-Screen Ride Controls Layer
        if (engine.run.status == GamePlayStatus.RIDING) {
            RideControls(
                engine = engine,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 76.dp)
            )
        }

        // 4. Game Over / Crash Summary Overlay
        if (engine.run.status == GamePlayStatus.CRASHED) {
            GameOverDialog(
                run = engine.run,
                onRestart = { viewModel.startNewGame() },
                onGoToUpgrades = {
                    viewModel.exitToLobby()
                    viewModel.selectTab(GameScreenTab.UPGRADES)
                },
                onExitToLobby = onExitToLobby,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 5. Pause Overlay
        if (engine.run.status == GamePlayStatus.PAUSED) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.padding(28.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DELIVERY PAUSED",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { engine.run.status = GamePlayStatus.RIDING },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Resume Delivery", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onExitToLobby,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Lobby (लॉबी)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameHud(
    engine: GameEngine,
    profile: PlayerProfile,
    isSoundMuted: Boolean,
    onToggleSound: () -> Unit,
    onPause: () -> Unit,
    onExit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top status row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speedometer gauge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${engine.run.speedKmh.toInt()}",
                    color = if (engine.run.isNitroActive) Color(0xFFFF3D00) else Color(0xFFFFD54F),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "KM/H", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            // Coin counter
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF263238))
                    .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(0xFFFFD54F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "₹", color = Color(0xFFE65100), fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "+${engine.run.coinsCollected}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Weather indicator badge
            val weather = engine.weatherController.currentWeather
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = weather.iconEmoji, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = weather.title.split(" ").first(),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Shield Charges
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(engine.run.currentShieldCharges) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Shield charge",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier
                            .size(20.dp)
                            .padding(horizontal = 2.dp)
                    )
                }
            }

            // Audio, Pause & Exit controls
            Row {
                IconButton(onClick = onToggleSound, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isSoundMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Toggle Audio",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onPause, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onExit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit to Lobby",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Delivery Target Progress Bar
        val progress = (engine.run.distanceMeters / engine.run.targetDeliveryDistance).coerceIn(0f, 1f)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = engine.run.activeMissionBanner ?: "Highway Delivery",
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${engine.run.distanceMeters.toInt()}m / ${engine.run.targetDeliveryDistance.toInt()}m",
                color = Color(0xFFFFD54F),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFFFF8F00),
            trackColor = Color(0xFF334155)
        )

        // Nitro active banner
        if (engine.run.isNitroActive) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFF3D00))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "CHAI NITRO BOOST ACTIVE!",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun RideControls(
    engine: GameEngine,
    screenWidth: Float,
    screenHeight: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Steer Button
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                .border(2.dp, Color(0xFF38BDF8), CircleShape)
                .clickable { engine.steerLeft() }
                .testTag("steer_left_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Steer Left",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        // Center Action Buttons: Horn Blast & Chai Nitro
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            // HORN BLAST BUTTON ("POM POM!")
            val isHornReady = engine.run.hornCooldownMs <= 0
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .background(if (isHornReady) Color(0xFFFF6D00) else Color(0xFF475569))
                    .border(2.dp, if (isHornReady) Color(0xFFFFD54F) else Color.Transparent, CircleShape)
                    .clickable(enabled = isHornReady) {
                        engine.triggerHornBlast(screenWidth, screenHeight)
                    }
                    .testTag("horn_blast_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "POM POM!",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (isHornReady) "हॉर्न" else "${(engine.run.hornCooldownMs / 1000f).toInt() + 1}s",
                        color = Color(0xFFFFF9C4),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // CHAI NITRO BUTTON
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(if (!engine.run.isNitroActive) Color(0xFFD84315) else Color(0xFFFF3D00))
                    .border(2.dp, Color(0xFFFFB300), CircleShape)
                    .clickable { engine.triggerChaiNitro() }
                    .testTag("chai_nitro_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LocalCafe,
                        contentDescription = "Chai Nitro",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "CHAI",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // Right Steer Button
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B).copy(alpha = 0.85f))
                .border(2.dp, Color(0xFF38BDF8), CircleShape)
                .clickable { engine.steerRight() }
                .testTag("steer_right_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Steer Right",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun GameOverDialog(
    run: com.example.game.ActiveGameRun,
    onRestart: () -> Unit,
    onGoToUpgrades: () -> Unit,
    onExitToLobby: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth(0.9f)
            .border(2.dp, Color(0xFFE65100), RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DELIVERY CRASH!",
                color = Color(0xFFFF5252),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "ट्रैफिक में टक्कर हो गई!",
                color = Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Score and Coins summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "DISTANCE", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${run.distanceMeters.toInt()} m", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "COINS EARNED", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "₹ ${run.coinsCollected}", color = Color(0xFFFFD54F), fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "PARCELS", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${run.parcelsDelivered}", color = Color(0xFF00E5FF), fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("restart_run_button")
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Deliver Again (सवारी शुरू)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onGoToUpgrades,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("upgrade_scooter_button")
            ) {
                Icon(imageVector = Icons.Default.Upgrade, contentDescription = null, tint = Color(0xFFFFD54F))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upgrade Scooter & Abilities", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onExitToLobby,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("exit_to_lobby_button")
            ) {
                Text("Back to Lobby (मुख्य मेन्यू)", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
