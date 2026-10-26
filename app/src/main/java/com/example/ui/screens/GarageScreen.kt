package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlayerProfile
import com.example.data.model.Scooter
import com.example.data.model.ScooterCatalog
import com.example.ui.viewmodel.GameViewModel

@Composable
fun GarageScreen(
    viewModel: GameViewModel,
    profile: PlayerProfile,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SANDEEP'S GARAGE",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "देसी स्कूटर गैराज",
                    color = Color(0xFFE0F2FE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Coin Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0C4A6E))
                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "₹", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "${profile.coins}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        // Scooter Cards List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(ScooterCatalog.ALL_SCOOTERS) { scooter ->
                val isUnlocked = profile.unlockedScooters.contains(scooter.id)
                val isSelected = profile.selectedScooterId == scooter.id
                val canAfford = profile.coins >= scooter.cost

                ScooterCard(
                    scooter = scooter,
                    isUnlocked = isUnlocked,
                    isSelected = isSelected,
                    canAfford = canAfford,
                    onAction = { viewModel.equipOrPurchaseScooter(scooter.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ScooterCard(
    scooter: Scooter,
    isUnlocked: Boolean,
    isSelected: Boolean,
    canAfford: Boolean,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scooter_card_${scooter.id}")
            .then(
                if (isSelected) Modifier.border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini 2D Scooter Canvas Silhouette
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(52.dp)) {
                        val prim = Color(scooter.primaryColorHex)
                        val acc = Color(scooter.accentColorHex)
                        // Body
                        drawRoundRect(
                            color = prim,
                            topLeft = Offset(8f, 18f),
                            size = Size(36f, 16f),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        // Wheels
                        drawCircle(color = Color(0xFF212121), radius = 6f, center = Offset(14f, 36f))
                        drawCircle(color = Color(0xFF212121), radius = 6f, center = Offset(38f, 36f))
                        drawCircle(color = Color.LightGray, radius = 2.5f, center = Offset(14f, 36f))
                        drawCircle(color = Color.LightGray, radius = 2.5f, center = Offset(38f, 36f))
                        // Handlebar
                        drawLine(color = Color.White, start = Offset(34f, 18f), end = Offset(36f, 8f), strokeWidth = 3f)
                        // Parcel box
                        drawRect(color = acc, topLeft = Offset(8f, 8f), size = Size(12f, 10f))
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = scooter.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isSelected) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Equipped",
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "EQUIPPED",
                                    color = Color(0xFF00E676),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = scooter.hindiTitle,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = scooter.description,
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stat bars
            StatRow(label = "Top Speed", value = (scooter.baseSpeed - 0.7f) / 1.3f, color = Color(0xFFFF5722))
            StatRow(label = "Pickup Accel", value = (scooter.baseAcceleration - 0.7f) / 1.3f, color = Color(0xFFFFB300))
            StatRow(label = "Handling", value = (scooter.baseHandling - 0.7f) / 1.3f, color = Color(0xFF00E5FF))

            Spacer(modifier = Modifier.height(8.dp))

            // Special Perk
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF334155).copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Special Perk: ",
                    color = Color(0xFFFFD54F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = scooter.specialPerk,
                    color = Color.White,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0284C7).copy(alpha = 0.25f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Currently Riding",
                            color = Color(0xFF38BDF8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (isUnlocked) {
                    Button(
                        onClick = onAction,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("equip_button_${scooter.id}")
                    ) {
                        Text(text = "Equip Ride", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onAction,
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF8F00),
                            disabledContainerColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("unlock_button_${scooter.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Unlock  •  ₹ ${scooter.cost}",
                            color = if (canAfford) Color.Black else Color(0xFF64748B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: Float, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            modifier = Modifier.width(80.dp)
        )
        LinearProgressIndicator(
            progress = { value.coerceIn(0.1f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF334155)
        )
    }
}
