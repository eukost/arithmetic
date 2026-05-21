package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MathViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: MathViewModel = viewModel()
                val userStats by viewModel.userStats.collectAsStateWithLifecycle()
                val history by viewModel.gameHistory.collectAsStateWithLifecycle()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val stats = userStats
                    if (stats == null) {
                        // Cozy starting loading screen while Room fetches stats
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFFAFAFA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🌱", fontSize = 48.sp)
                                Spacer(modifier = Modifier.padding(10.dp))
                                Text(
                                    text = "Readying Your Playground...",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.padding(4.dp))
                                CircularProgressIndicator(color = Color(0xFF2196F3))
                            }
                        }
                    } else {
                        MainNavigationContainer(
                            viewModel = viewModel,
                            userStats = stats,
                            history = history
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainNavigationContainer(
    viewModel: MathViewModel,
    userStats: com.example.data.UserStats,
    history: List<com.example.data.GameHistory>
) {
    // Current active tab when not in an active game session: "home", "sandbox", "hall_of_fame"
    var currentScreenTab by remember { mutableStateOf("home") }
    val activeColor = getAvatarColor(userStats.avatar)

    // Game Mode switches active screen completely to fullscreen immersive math solver
    if (viewModel.activeGameMode != null) {
        GameScreen(viewModel = viewModel, userStats = userStats)
    } else {
        Scaffold(
            bottomBar = {
                KidsBottomNavBar(
                    currentScreen = currentScreenTab,
                    onNavigate = { currentScreenTab = it },
                    activeColor = activeColor
                )
            },
            containerColor = Color(0xFFFAFAFA)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreenTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "tabSwitcher"
                ) { tab ->
                    when (tab) {
                        "home" -> HomeScreen(
                            viewModel = viewModel,
                            userStats = userStats,
                            onNavigateToSandbox = { currentScreenTab = "sandbox" },
                            onNavigateToAwards = { currentScreenTab = "hall_of_fame" }
                        )
                        "sandbox" -> SandboxScreen(
                            viewModel = viewModel,
                            userStats = userStats
                        )
                        "hall_of_fame" -> HallOfFameScreen(
                            viewModel = viewModel,
                            userStats = userStats,
                            history = history
                        )
                    }
                }
            }
        }
    }
}
