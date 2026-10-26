package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlayerProfile
import com.example.data.model.UpgradeItem
import com.example.data.model.UpgradeSection
import com.example.data.model.UpgradeType
import com.example.ui.viewmodel.GameViewModel

@Composable
fun UpgradeMenuScreen(
    viewModel: GameViewModel,
    profile: PlayerProfile,
    modifier: Modifier = Modifier
) {
    var selectedSectionIndex by remember { mutableStateOf(0) }
    val sections = listOf(UpgradeSection.PERFORMANCE, UpgradeSection.DEFENSE, UpgradeSection.ABILITIES)

    val currentUpgrades = when (sections[selectedSectionIndex]) {
        UpgradeSection.PERFORMANCE -> listOf(
            viewModel.getUpgradeItem(UpgradeType.TOP_SPEED, profile),
            viewModel.getUpgradeItem(UpgradeType.ACCELERATION, profile),
            viewModel.getUpgradeItem(UpgradeType.HANDLING, profile)
        )
        UpgradeSection.DEFENSE -> listOf(
            viewModel.getUpgradeItem(UpgradeType.HORN_BLAST, profile),
            viewModel.getUpgradeItem(UpgradeType.TEMPORARY_SHIELD, profile)
        )
        UpgradeSection.ABILITIES -> listOf(
            viewModel.getUpgradeItem(UpgradeType.COIN_MAGNET, profile),
            viewModel.getUpgradeItem(UpgradeType.SCORE_MULTIPLIER, profile),
            viewModel.getUpgradeItem(UpgradeType.DELIVERY_BONUS, profile)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        // Top Header Banner with Coin Balance
        UpgradeHeader(coins = profile.coins)

        // Section Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSectionIndex,
            containerColor = Color(0xFF1E293B),
            contentColor = Color(0xFFFFB300),
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            sections.forEachIndexed { index, section ->
                Tab(
                    selected = selectedSectionIndex == index,
                    onClick = { selectedSectionIndex = index },
                    modifier = Modifier.testTag("tab_${section.name.lowercase()}"),
                    text = {
                        Text(
                            text = section.title,
                            fontWeight = if (selectedSectionIndex == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        // Section Subtitle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = sections[selectedSectionIndex].subtitle,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }

        // Upgrades List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(currentUpgrades) { item ->
                UpgradeCard(
                    item = item,
                    playerCoins = profile.coins,
                    onUpgrade = { viewModel.purchaseUpgrade(item.type) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun UpgradeHeader(coins: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFE65100), Color(0xFFFF8F00))
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "UPGRADE WORKSHOP",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "संदीप की सवारी वर्कशॉप",
                color = Color(0xFFFFF9C4),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Coin Wallet Badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF212121))
                .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(0xFFFFD54F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "₹",
                    color = Color(0xFFE65100),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$coins",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun UpgradeCard(
    item: UpgradeItem,
    playerCoins: Long,
    onUpgrade: () -> Unit
) {
    val canAfford = playerCoins >= item.nextLevelCost && !item.isMaxLevel
    val icon = getUpgradeIcon(item.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upgrade_card_${item.type.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF334155)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = item.title,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Level badge
                        Text(
                            text = if (item.isMaxLevel) "MAX" else "Lv. ${item.currentLevel}/${item.maxLevel}",
                            color = if (item.isMaxLevel) Color(0xFF00E676) else Color(0xFFFFB300),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = item.hindiTitle,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Level Progress Indicator
            LinearProgressIndicator(
                progress = { (item.currentLevel.toFloat() / item.maxLevel.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (item.isMaxLevel) Color(0xFF00E676) else Color(0xFFFFB300),
                trackColor = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Effect Summary Description
            Text(
                text = item.getEffectSummary(),
                color = Color(0xFF38BDF8),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = item.description,
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Upgrade Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (item.isMaxLevel) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF00E676).copy(alpha = 0.15f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "MAX LEVEL REACHED",
                            color = Color(0xFF00E676),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = onUpgrade,
                        enabled = canAfford,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF8F00),
                            disabledContainerColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("upgrade_button_${item.type.name.lowercase()}")
                    ) {
                        Text(
                            text = "Upgrade  •  ₹ ${item.nextLevelCost}",
                            color = if (canAfford) Color.Black else Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

private fun getUpgradeIcon(type: UpgradeType): ImageVector = when (type) {
    UpgradeType.TOP_SPEED -> Icons.Default.Speed
    UpgradeType.ACCELERATION -> Icons.Default.ElectricBolt
    UpgradeType.HANDLING -> Icons.Default.TwoWheeler
    UpgradeType.HORN_BLAST -> Icons.Default.VolumeUp
    UpgradeType.TEMPORARY_SHIELD -> Icons.Default.Security
    UpgradeType.COIN_MAGNET -> Icons.Default.LocalAtm
    UpgradeType.SCORE_MULTIPLIER -> Icons.Default.Timer
    UpgradeType.DELIVERY_BONUS -> Icons.Default.LocalAtm
}
