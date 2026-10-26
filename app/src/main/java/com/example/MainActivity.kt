package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.LobbyScreen
import com.example.ui.screens.LoginSignupScreen
import com.example.ui.screens.MissionsScreen
import com.example.ui.screens.UpgradeMenuScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameScreenTab
import com.example.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: GameViewModel) {
    val isLoggedInOrGuest by viewModel.isLoggedInOrGuest.collectAsState()
    val isPlayingGame by viewModel.isPlayingGame.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isSoundMuted by viewModel.isSoundMuted.collectAsState()
    val activeMission by viewModel.activeMission.collectAsState()
    val syncStatus by viewModel.cloudSyncStatus.collectAsState()

    // 1. Separate Login and Signup page if not yet logged in or continued as guest
    if (!isLoggedInOrGuest) {
        LoginSignupScreen(
            viewModel = viewModel,
            onContinueAsGuest = { viewModel.continueAsGuest() }
        )
        return
    }

    // 2. Active Ride gameplay screen when user pressed "START DELIVERY"
    if (isPlayingGame) {
        BackHandler {
            viewModel.exitToLobby()
        }

        GameScreen(
            viewModel = viewModel,
            profile = profile,
            isSoundMuted = isSoundMuted,
            onExitToLobby = { viewModel.exitToLobby() }
        )
        return
    }

    // 3. Main Lobby & Garage Hub Scaffold with Navigation Bar
    BackHandler(enabled = currentTab != GameScreenTab.PLAY) {
        viewModel.selectTab(GameScreenTab.PLAY)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                contentColor = Color(0xFFFFB300),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == GameScreenTab.PLAY,
                    onClick = { viewModel.selectTab(GameScreenTab.PLAY) },
                    icon = { Icon(imageVector = Icons.Default.TwoWheeler, contentDescription = "Lobby") },
                    label = { Text("Lobby", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color(0xFFFFB300),
                        indicatorColor = Color(0xFFFFB300),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_item_ride")
                )

                NavigationBarItem(
                    selected = currentTab == GameScreenTab.UPGRADES,
                    onClick = { viewModel.selectTab(GameScreenTab.UPGRADES) },
                    icon = { Icon(imageVector = Icons.Default.Upgrade, contentDescription = "Upgrades") },
                    label = { Text("Upgrades", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color(0xFFFFB300),
                        indicatorColor = Color(0xFFFFB300),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_item_upgrades")
                )

                NavigationBarItem(
                    selected = currentTab == GameScreenTab.GARAGE,
                    onClick = { viewModel.selectTab(GameScreenTab.GARAGE) },
                    icon = { Icon(imageVector = Icons.Default.Build, contentDescription = "Garage") },
                    label = { Text("Garage", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color(0xFFFFB300),
                        indicatorColor = Color(0xFFFFB300),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_item_garage")
                )

                NavigationBarItem(
                    selected = currentTab == GameScreenTab.MISSIONS,
                    onClick = { viewModel.selectTab(GameScreenTab.MISSIONS) },
                    icon = { Icon(imageVector = Icons.Default.Assignment, contentDescription = "Contracts") },
                    label = { Text("Contracts", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color(0xFFFFB300),
                        indicatorColor = Color(0xFFFFB300),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_item_missions")
                )

                NavigationBarItem(
                    selected = currentTab == GameScreenTab.PROFILE,
                    onClick = { viewModel.selectTab(GameScreenTab.PROFILE) },
                    icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Account") },
                    label = { Text("Cloud", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color(0xFFFFB300),
                        indicatorColor = Color(0xFFFFB300),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier.testTag("nav_item_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0F172A))
        ) {
            when (currentTab) {
                GameScreenTab.PLAY -> LobbyScreen(
                    viewModel = viewModel,
                    profile = profile,
                    activeMission = activeMission,
                    isSoundMuted = isSoundMuted,
                    onStartGame = { viewModel.startGame() }
                )
                GameScreenTab.UPGRADES -> UpgradeMenuScreen(
                    viewModel = viewModel,
                    profile = profile
                )
                GameScreenTab.GARAGE -> GarageScreen(
                    viewModel = viewModel,
                    profile = profile
                )
                GameScreenTab.MISSIONS -> MissionsScreen(
                    viewModel = viewModel,
                    profile = profile,
                    activeMission = activeMission
                )
                GameScreenTab.PROFILE -> AuthScreen(
                    viewModel = viewModel,
                    profile = profile,
                    currentUser = currentUser,
                    syncStatus = syncStatus
                )
            }
        }
    }
}
