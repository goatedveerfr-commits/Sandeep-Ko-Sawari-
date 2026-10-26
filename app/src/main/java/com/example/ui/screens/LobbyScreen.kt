package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryMission
import com.example.data.model.PlayerProfile
import com.example.data.model.ScooterCatalog
import com.example.game.WeatherCondition
import com.example.ui.viewmodel.GameScreenTab
import com.example.ui.viewmodel.GameViewModel

@Composable
fun LobbyScreen(
    viewModel: GameViewModel,
    profile: PlayerProfile,
    activeMission: DeliveryMission?,
    isSoundMuted: Boolean,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scooter = ScooterCatalog.getScooter(profile.selectedScooterId)
    val weather = viewModel.engine.weatherController.currentWeather
    val scrollState = rememberScrollState()

    // Pulse animation for the Start Delivery Button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .verticalScroll(scrollState)
    ) {
        // 1. Top Bar Header
        LobbyTopBar(
            profile = profile,
            isSoundMuted = isSoundMuted,
            onToggleSound = { viewModel.toggleSound() },
            onGoToProfile = { viewModel.selectTab(GameScreenTab.PROFILE) }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2. Equipped Scooter Showcase Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT SAWARI (राइड)",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = scooter.name,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = scooter.hindiTitle,
                                color = Color(0xFFFFB300),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Equipped Scooter mini Canvas visual
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F172A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(56.dp)) {
                                val prim = Color(scooter.primaryColorHex)
                                val acc = Color(scooter.accentColorHex)
                                drawRoundRect(
                                    color = prim,
                                    topLeft = Offset(8f, 18f),
                                    size = Size(38f, 18f),
                                    cornerRadius = CornerRadius(6f, 6f)
                                )
                                drawCircle(color = Color(0xFF212121), radius = 6.5f, center = Offset(14f, 38f))
                                drawCircle(color = Color(0xFF212121), radius = 6.5f, center = Offset(40f, 38f))
                                drawLine(color = Color.White, start = Offset(36f, 18f), end = Offset(38f, 8f), strokeWidth = 3f)
                                drawRect(color = acc, topLeft = Offset(8f, 8f), size = Size(12f, 10f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = scooter.specialPerk,
                        color = Color(0xFF38BDF8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickStatBadge(label = "Top Speed", value = "Lv. ${profile.speedLevel}")
                        QuickStatBadge(label = "Pickup", value = "Lv. ${profile.accelLevel}")
                        QuickStatBadge(label = "Agility", value = "Lv. ${profile.handlingLevel}")
                        QuickStatBadge(label = "Shield", value = "${((profile.shieldLevel + 1) / 2)} HP")
                    }
                }
            }

            // 3. GIANT START DELIVERY BUTTON
            Button(
                onClick = onStartGame,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF8F00)
                ),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .scale(buttonScale)
                    .testTag("start_delivery_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "START DELIVERY",
                            color = Color.Black,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "राइड शुरू करें • निकल पड़ो सड़क पर",
                            color = Color(0xFF422006),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4. Dynamic Weather Radar Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = weather.iconEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = weather.title,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = weather.hindiTitle,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Cycle Weather Button
                        Button(
                            onClick = { viewModel.engine.weatherController.cycleNextWeather() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("cycle_weather_button")
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Change", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = weather.description,
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Traction: ${(weather.handlingTraction * 100).toInt()}%",
                            color = if (weather == WeatherCondition.MONSOON_RAIN) Color(0xFFFF5252) else Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Visibility: ${(weather.visibilityAlpha * 100).toInt()}%",
                            color = if (weather == WeatherCondition.HEAVY_SMOG) Color(0xFFFFB300) else Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 5. Active Contract Card
            activeMission?.let { mission ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE CONTRACT",
                                color = Color(0xFFC084FC),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mission.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mission.description,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF4C1D95))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+₹ ${mission.rewardCoins}",
                                color = Color(0xFFFFD54F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 6. Hub Navigation Grid (Workshop, Garage, Contracts)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HubNavCard(
                    title = "Workshop",
                    hindi = "अपग्रेड",
                    icon = Icons.Default.Upgrade,
                    color = Color(0xFFFF8F00),
                    onClick = { viewModel.selectTab(GameScreenTab.UPGRADES) },
                    modifier = Modifier.weight(1f)
                )
                HubNavCard(
                    title = "Garage",
                    hindi = "गाड़ियाँ",
                    icon = Icons.Default.Build,
                    color = Color(0xFF0284C7),
                    onClick = { viewModel.selectTab(GameScreenTab.GARAGE) },
                    modifier = Modifier.weight(1f)
                )
                HubNavCard(
                    title = "Contracts",
                    hindi = "मिशन",
                    icon = Icons.Default.Assignment,
                    color = Color(0xFF7C3AED),
                    onClick = { viewModel.selectTab(GameScreenTab.MISSIONS) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun LobbyTopBar(
    profile: PlayerProfile,
    isSoundMuted: Boolean,
    onToggleSound: () -> Unit,
    onGoToProfile: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFE65100), Color(0xFFFF8F00))
                )
            )
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onGoToProfile() }
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF212121)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = profile.displayName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Sandeep Ki Sawari",
                    color = Color(0xFFFFF9C4),
                    fontSize = 10.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Coin Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF212121))
                    .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Color(0xFFFFD54F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "₹", color = Color(0xFFE65100), fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "${profile.coins}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(onClick = onToggleSound) {
                Icon(
                    imageVector = if (isSoundMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Toggle Audio",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun QuickStatBadge(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0F172A))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 10.sp)
        Text(text = value, color = Color(0xFFFFD54F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HubNavCard(
    title: String,
    hindi: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = hindi, color = Color(0xFF94A3B8), fontSize = 10.sp)
        }
    }
}
