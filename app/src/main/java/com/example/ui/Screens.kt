package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.GameHistory
import com.example.data.UserStats
import com.example.ui.components.*
import com.example.viewmodel.MathViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Helper functions for user profile / avatar choices
fun getAvatarEmoji(avatar: String): String {
    return when (avatar.lowercase()) {
        "bear" -> "🐻"
        "rabbit" -> "🐰"
        "frog" -> "🐸"
        "lion" -> "🦁"
        "monkey" -> "🐵"
        else -> "🐻"
    }
}

fun getAvatarColor(avatar: String): Color {
    return when (avatar.lowercase()) {
        "bear" -> KidSecondary // Blue
        "rabbit" -> KidPink
        "frog" -> KidPrimary // Green
        "lion" -> KidAccent // Orange
        "monkey" -> KidPurple
        else -> KidSecondary
    }
}

fun getAvatarLabel(avatar: String): String {
    return when (avatar.lowercase()) {
        "bear" -> "Smart Bear"
        "rabbit" -> "Bouncy Bunny"
        "frog" -> "Leap Frog"
        "lion" -> "Cheery Lion"
        "monkey" -> "Clever Monkey"
        else -> "Smart Bear"
    }
}

// Custom Bottom Navigation Bar
@Composable
fun KidsBottomNavBar(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    activeColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        tonalElevation = 8.dp,
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                label = "Play",
                icon = Icons.Default.Games,
                isSelected = currentScreen == "home",
                activeColor = activeColor,
                onClick = { onNavigate("home") }
            )
            NavBarItem(
                label = "Sandbox",
                icon = Icons.Default.Palette,
                isSelected = currentScreen == "sandbox",
                activeColor = activeColor,
                onClick = { onNavigate("sandbox") }
            )
            NavBarItem(
                label = "Awards",
                icon = Icons.Default.EmojiEvents,
                isSelected = currentScreen == "hall_of_fame",
                activeColor = activeColor,
                onClick = { onNavigate("hall_of_fame") }
            )
        }
    }
}

@Composable
fun NavBarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(targetValue = if (isSelected) 1.15f else 1.0f, label = "navScale")
    val color = if (isSelected) activeColor else Color.Gray

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

// ---------------- HOME SCREEN -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MathViewModel,
    userStats: UserStats,
    onNavigateToSandbox: () -> Unit,
    onNavigateToAwards: () -> Unit
) {
    val activeColor = getAvatarColor(userStats.avatar)
    var showProfileEdit by remember { mutableStateOf(false) }
    var selectedOpForLevel by remember { mutableStateOf<String?>(null) } // "ADDITION", "SUBTRACTION", etc.

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Arithmetic Playground",
                        color = activeColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                },
                actions = {
                    IconButton(onClick = { showProfileEdit = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Edit Profile",
                            tint = Color.DarkGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF9F9F9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Card Top Banner
            KidProfileCard(userStats = userStats, onClickEdit = { showProfileEdit = true }, activeColor = activeColor)

            Spacer(modifier = Modifier.height(20.dp))

            // Main Play Card Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👇 Choose Your Math Quest!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.DarkGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Math Quests Grid Style (Vertical Column of Cards)
            QuestRowCard(
                title = "Addition Orchard",
                description = "Count apples and solve friendly additions!",
                symbol = "🍎",
                operatorSymbol = "＋",
                themeColor = KidPrimary,
                levelUnlocked = userStats.additionLevel,
                onClick = { selectedOpForLevel = "ADDITION" }
            )

            QuestRowCard(
                title = "Balloon Subtraction",
                description = "Pop colorful balloons and take away numbers!",
                symbol = "🎈",
                operatorSymbol = "－",
                themeColor = KidPink,
                levelUnlocked = userStats.subtractionLevel,
                onClick = { selectedOpForLevel = "SUBTRACTION" }
            )

            QuestRowCard(
                title = "Star Multiplication",
                description = "Build bright matrices of stars!",
                symbol = "⭐️",
                operatorSymbol = "×",
                themeColor = KidAccent,
                levelUnlocked = userStats.multiplicationLevel,
                onClick = { selectedOpForLevel = "MULTIPLICATION" }
            )

            QuestRowCard(
                title = "Cookie Division Share",
                description = "Divide cookies evenly into cute animal bowls!",
                symbol = "🍪",
                operatorSymbol = "÷",
                themeColor = KidPurple,
                levelUnlocked = userStats.divisionLevel,
                onClick = { selectedOpForLevel = "DIVISION" }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Helper sandbox shortcut
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable(onClick = onNavigateToSandbox),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color(0xFFFFF176))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Interactive Sandbox Lab",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Free exploration with sliders and real-time drawings!",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Go",
                        tint = Color(0xFFE65100)
                    )
                }
            }
        }
    }

    // Modal Profile Editor
    if (showProfileEdit) {
        ProfileEditDialog(
            userStats = userStats,
            onDismiss = { showProfileEdit = false },
            onSave = { name, avatar ->
                viewModel.updateProfile(name, avatar)
                showProfileEdit = false
            }
        )
    }

    // Level Picker Bottom Dialog
    selectedOpForLevel?.let { operation ->
        val currentMaxLevel = when (operation) {
            "ADDITION" -> userStats.additionLevel
            "SUBTRACTION" -> userStats.subtractionLevel
            "MULTIPLICATION" -> userStats.multiplicationLevel
            else -> userStats.divisionLevel
        }
        LevelPickerDialog(
            operation = operation,
            maxUnlockedLevel = currentMaxLevel,
            onDismiss = { selectedOpForLevel = null },
            onSelectLevel = { level ->
                viewModel.startGame(operation, level)
                selectedOpForLevel = null
            }
        )
    }
}

// Profile Header Card on home
@Composable
fun KidProfileCard(userStats: UserStats, onClickEdit: () -> Unit, activeColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clickable(onClick = onClickEdit),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Emoji Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(activeColor.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, activeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getAvatarEmoji(userStats.avatar),
                    fontSize = 38.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userStats.name,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = getAvatarLabel(userStats.avatar),
                    fontSize = 12.sp,
                    color = activeColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Stars badge
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFFFFDE7), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⭐", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${userStats.stars} Stars",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57F17)
                        )
                    }

                    // Streak badge
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFFFE0B2), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${userStats.streak} Days",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Profile",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Bouncy Row Card for operation quests
@Composable
fun QuestRowCard(
    title: String,
    description: String,
    symbol: String,
    operatorSymbol: String,
    themeColor: Color,
    levelUnlocked: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Operator Sign Circular Badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(themeColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .border(2.dp, themeColor, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = operatorSymbol, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = themeColor)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$title $symbol",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Level Tracker
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Unlocked: ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    repeat(5) { ind ->
                        val isLit = ind < levelUnlocked
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Level Up Unlocked",
                            tint = if (isLit) Color(0xFFFFD54F) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Start game",
                tint = themeColor,
                modifier = Modifier
                    .size(32.dp)
                    .background(themeColor.copy(alpha = 0.15f), CircleShape)
                    .padding(4.dp)
            )
        }
    }
}

// ----------------- PROFILE EDIT DIALOG -----------------
@Composable
fun ProfileEditDialog(
    userStats: UserStats,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(userStats.name) }
    var selectedAvatar by remember { mutableStateOf(userStats.avatar) }

    val avatarsList = listOf("bear", "rabbit", "frog", "lion", "monkey")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚙️ Choose Your Explorer!",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input Field for Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 15) name = it },
                    label = { Text("Explorer's Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Select Avatar (changes app colors!)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Avatars row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    avatarsList.forEach { avatar ->
                        val isSelected = selectedAvatar == avatar
                        val avColor = getAvatarColor(avatar)
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) avColor.copy(alpha = 0.2f) else Color.Transparent)
                                .border(
                                    border = BorderStroke(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) avColor else Color.LightGray
                                    ),
                                    shape = CircleShape
                                )
                                .clickable { selectedAvatar = avatar },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = getAvatarEmoji(avatar), fontSize = 28.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { if (name.isNotBlank()) onSave(name, selectedAvatar) },
                        colors = ButtonDefaults.buttonColors(containerColor = getAvatarColor(selectedAvatar)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Profile")
                    }
                }
            }
        }
    }
}

// ----------------- LEVEL PICKER DIALOG -----------------
@Composable
fun LevelPickerDialog(
    operation: String,
    maxUnlockedLevel: Int,
    onDismiss: () -> Unit,
    onSelectLevel: (Int) -> Unit
) {
    val themeColor = when (operation) {
        "ADDITION" -> KidPrimary
        "SUBTRACTION" -> KidPink
        "MULTIPLICATION" -> KidAccent
        else -> KidPurple
    }

    val levelsInfo = listOf(
        "Level 1: Novice (Easy 1 - 5)" to "Perfect for absolute beginners",
        "Level 2: Apprentice (Numbers to 10)" to "Slightly bigger challenges",
        "Level 3: Explorer (Numbers to 20)" to "Great practice level!",
        "Level 4: Scholar (Challenging to 50)" to "Sharpen your quick recall",
        "Level 5: Legend (Mega Math Mastery)" to "Ultimate math wizard mode!"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆 Select Your Level",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = operation,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = themeColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    levelsInfo.forEachIndexed { index, pair ->
                        val currentLevelNum = index + 1
                        val isLocked = currentLevelNum > maxUnlockedLevel

                        val background = if (isLocked) Color(0xFFF5F5F5) else themeColor.copy(alpha = 0.05f)
                        val borderColor = if (isLocked) Color.LightGray else themeColor

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(background, RoundedCornerShape(14.dp))
                                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                                .clickable(enabled = !isLocked) { onSelectLevel(currentLevelNum) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(if (isLocked) Color.LightGray else themeColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$currentLevelNum",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pair.first,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isLocked) Color.Gray else Color.DarkGray
                                )
                                Text(
                                    text = if (isLocked) "🔒 Locked: Beat previous levels to unlock!" else pair.second,
                                    fontSize = 10.sp,
                                    color = if (isLocked) Color.LightGray else Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Close", color = Color.Gray)
                }
            }
        }
    }
}

// ----------------- GAME SCREEN -----------------
@Composable
fun GameScreen(
    viewModel: MathViewModel,
    userStats: UserStats
) {
    val activeColor = getAvatarColor(userStats.avatar)
    val haptic = LocalHapticFeedback.current

    if (viewModel.isGameFinished) {
        GameFinishedSummary(viewModel = viewModel, activeColor = activeColor)
    } else {
        viewModel.currentQuestion?.let { q ->
            Scaffold(
                containerColor = Color(0xFFFBFBFB)
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top stats line: Exit, Progress indicator, score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.exitGame() },
                            modifier = Modifier
                                .background(Color.White, CircleShape)
                                .shadow(2.dp, CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Game", tint = Color.DarkGray)
                        }

                        // Snail progress tracker
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🐌",
                                indexRange(viewModel.currentQuestionIndex, viewModel.totalQuestionsCount),
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            LinearProgressIndicator(
                                progress = { (viewModel.currentQuestionIndex.toFloat() + 1) / viewModel.totalQuestionsCount },
                                color = activeColor,
                                trackColor = Color(0xFFEEEEEE),
                                strokeCap = strokeCapSquare(),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                        }

                        Text(
                            text = "Score: ${viewModel.gameScore}",
                            fontWeight = FontWeight.Bold,
                            color = activeColor,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Big Question display
                    Text(
                        text = "Compute this:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${q.num1}  ${q.operation}  ${q.num2}  =  ?",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.DarkGray,
                                fontSize = 34.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Main Visual Aid (apples/balloons/stars/cookies)
                    Box(modifier = Modifier.weight(1f)) {
                        MainVisualAid(operation = q.operation, num1 = q.num1, num2 = q.num2)
                    }

                    // Bottom Choice selectors
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Arrange answers in a 2x2 grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GameChoiceButton(
                                answer = q.options.getOrNull(0) ?: 0,
                                isAnswerChecked = viewModel.isAnswerChecked,
                                correctAnswer = q.correctAnswer,
                                selectedAnswer = viewModel.selectedAnswer,
                                tag = "choice_1",
                                activeColor = activeColor,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.submitAnswer(it)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            GameChoiceButton(
                                answer = q.options.getOrNull(1) ?: 0,
                                isAnswerChecked = viewModel.isAnswerChecked,
                                correctAnswer = q.correctAnswer,
                                selectedAnswer = viewModel.selectedAnswer,
                                tag = "choice_2",
                                activeColor = activeColor,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.submitAnswer(it)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GameChoiceButton(
                                answer = q.options.getOrNull(2) ?: 0,
                                isAnswerChecked = viewModel.isAnswerChecked,
                                correctAnswer = q.correctAnswer,
                                selectedAnswer = viewModel.selectedAnswer,
                                tag = "choice_3",
                                activeColor = activeColor,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.submitAnswer(it)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            GameChoiceButton(
                                answer = q.options.getOrNull(3) ?: 0,
                                isAnswerChecked = viewModel.isAnswerChecked,
                                correctAnswer = q.correctAnswer,
                                selectedAnswer = viewModel.selectedAnswer,
                                tag = "choice_4",
                                activeColor = activeColor,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.submitAnswer(it)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Explanation / Banner when answers checked
                    AnimatedVisibility(
                        visible = viewModel.isAnswerChecked,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(20.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = if (viewModel.isAnswerCorrect) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(
                                2.3.dp,
                                if (viewModel.isAnswerCorrect) KidPrimary else KidAccent
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (viewModel.isAnswerCorrect) "🎉" else "💡",
                                    fontSize = 32.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (viewModel.isAnswerCorrect) "Incredible Job!" else "Almost got it!",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = if (viewModel.isAnswerCorrect) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                                    )
                                    Text(
                                        text = q.explanation,
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }

                                Button(
                                    onClick = { viewModel.nextQuestion() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (viewModel.isAnswerCorrect) KidPrimary else KidAccent
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Next ➔",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun indexRange(ind: Int, total: Int): androidx.compose.ui.Modifier {
    // Spacer helper logic or unused parameter resolver
    return Modifier
}

private fun strokeCapSquare() = androidx.compose.ui.graphics.StrokeCap.Round

@Composable
fun GameChoiceButton(
    answer: Int,
    isAnswerChecked: Boolean,
    correctAnswer: Int,
    selectedAnswer: Int?,
    tag: String,
    activeColor: Color,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentSelection = selectedAnswer == answer
    val isCorrect = answer == correctAnswer

    val cardColor = when {
        isAnswerChecked && isCorrect -> Color(0xFFE8F5E9)          // Green correct
        isAnswerChecked && isCurrentSelection && !isCorrect -> Color(0xFFFFE9E9) // Red wrong
        isCurrentSelection -> activeColor.copy(alpha = 0.15f)     // Normal active pressed
        else -> Color.White
    }

    val borderColor = when {
        isAnswerChecked && isCorrect -> KidPrimary
        isAnswerChecked && isCurrentSelection && !isCorrect -> KidPink
        isCurrentSelection -> activeColor
        else -> Color(0xFFE0E0E0)
    }

    val borderWidth = if (isCurrentSelection || (isAnswerChecked && isCorrect)) 3.dp else 1.dp

    Card(
        modifier = modifier
            .shadow(if (isCurrentSelection) 4.dp else 1.dp, RoundedCornerShape(16.dp))
            .clickable(enabled = !isAnswerChecked) { onClick(answer) },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$answer",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = when {
                        isAnswerChecked && isCorrect -> Color(0xFF2E7D32)
                        isAnswerChecked && isCurrentSelection && !isCorrect -> Color(0xFFC62828)
                        else -> Color.DarkGray
                    }
                )

                if (isAnswerChecked) {
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isCorrect) {
                        Text("⭐", fontSize = 16.sp)
                    } else if (isCurrentSelection) {
                        Text("❌", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// Celebration summary at end of 5 questions
@Composable
fun GameFinishedSummary(
    viewModel: MathViewModel,
    activeColor: Color
) {
    val correct = viewModel.correctAnswersCount
    val stars = viewModel.starsEarnedInGame

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Triumphal Badge Icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(activeColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (correct) {
                    5 -> "👑"
                    4 -> "🦁"
                    3 -> "🐨"
                    else -> "🐸"
                },
                fontSize = 58.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (correct) {
                5 -> "PERFECT MATH MAGIC!"
                4 -> "Outstanding Job!"
                3 -> "Super Explorer!"
                else -> "Great Try, Explorer!"
            },
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center
        )

        Text(
            text = "You solved $correct out of 5 math quests correctly!",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Physical golden stars
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..3) {
                val earned = i <= stars
                val scale by animateFloatAsState(
                    targetValue = if (earned) 1.2f else 0.85f,
                    animationSpec = spring(dampingRatio = 0.45f), label = "starCelebration"
                )

                Column(
                    modifier = Modifier.scale(scale),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (earned) "⭐" else "☆",
                        fontSize = 44.sp,
                        color = if (earned) Color(0xFFFFD54F) else Color.LightGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Level Unlock Notification Card
        if (correct >= 3) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEE)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color(0xFFFFCA28))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔑", fontSize = 28.sp, modifier = Modifier.padding(end = 10.dp))
                    Column {
                        Text(
                            text = "Next Quest Unlocked!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57F17),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Level up progress saved to profile!",
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Action controls
        Button(
            onClick = { viewModel.startGame(viewModel.activeGameMode ?: "ADDITION", viewModel.activeGameLevel) },
            colors = ButtonDefaults.buttonColors(containerColor = activeColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Play Again")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play This Quest Again", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { viewModel.exitGame() },
            border = BorderStroke(1.5.dp, activeColor),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Back to Playground Home", fontWeight = FontWeight.Bold, color = activeColor)
        }
    }
}


// ----------------- SANDBOX SCREEN -----------------
@Composable
fun SandboxScreen(viewModel: MathViewModel, userStats: UserStats) {
    val activeColor = getAvatarColor(userStats.avatar)

    val ops = listOf("+", "-", "×", "÷")

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Text(
                        text = "🎨 Dynamic Sandbox Lab",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = activeColor
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFFAFAFA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Slide values to see mathematical visual aids update live! There is no stress or timer here. Just explore!",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Segmented control of operators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ops.forEach { op ->
                    val isSelected = viewModel.sandboxOp == op
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) activeColor else Color.Transparent)
                            .clickable { viewModel.updateSandbox(viewModel.sandboxNum1, viewModel.sandboxNum2, op) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = op,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = if (isSelected) Color.White else Color.DarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Num 1 Slider Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Number A (First group)", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("${viewModel.sandboxNum1}", fontSize = 16.sp, color = activeColor, fontWeight = FontWeight.Black)
                    }
                    Slider(
                        value = viewModel.sandboxNum1.toFloat(),
                        onValueChange = { viewModel.updateSandbox(it.toInt(), viewModel.sandboxNum2, viewModel.sandboxOp) },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            inactiveTrackColor = Color(0xFFEFEFEF),
                            activeTrackColor = activeColor,
                            thumbColor = activeColor
                        )
                    )
                }
            }

            // Num 2 Slider Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Number B (Second group)", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("${viewModel.sandboxNum2}", fontSize = 16.sp, color = activeColor, fontWeight = FontWeight.Black)
                    }
                    Slider(
                        value = viewModel.sandboxNum2.toFloat(),
                        onValueChange = { viewModel.updateSandbox(viewModel.sandboxNum1, it.toInt(), viewModel.sandboxOp) },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            inactiveTrackColor = Color(0xFFEFEFEF),
                            activeTrackColor = activeColor,
                            thumbColor = activeColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual drawings aid
            MainVisualAid(
                operation = viewModel.sandboxOp,
                num1 = viewModel.sandboxNum1,
                num2 = if (viewModel.sandboxOp == "-" && viewModel.sandboxNum2 > viewModel.sandboxNum1) {
                    viewModel.sandboxNum1 // prevent negative balloon visual errors
                } else viewModel.sandboxNum2
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}


// ----------------- AWARDS / HALL OF FAME SCREEN -----------------
@Composable
fun HallOfFameScreen(viewModel: MathViewModel, userStats: UserStats, history: List<GameHistory>) {
    val activeColor = getAvatarColor(userStats.avatar)
    val unlockedBadges = determineEarnedBadges(userStats, history)

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Text(
                        text = "🏆 Awards Hall & Stats",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = activeColor
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFFAFAFA)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Stats Summary Group
                item {
                    StatsHeaderRow(stats = userStats, totalGames = history.size, activeColor = activeColor)
                }

                // Badges/Medals Section Title
                item {
                    Text(
                        text = "🎖️ Your Badges (${unlockedBadges.size}/7)",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = Color.DarkGray
                    )
                }

                // Row/Grid of badges
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0xFFECEFF1), RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val allBadges = getBadgesConfig()
                        allBadges.chunked(2).forEach { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                chunk.forEach { badge ->
                                    val isEarned = unlockedBadges.contains(badge.id)
                                    BadgeGridItem(badge = badge, isEarned = isEarned, activeColor = activeColor, modifier = Modifier.weight(1f))
                                }
                                if (chunk.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Recent History Logs
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📜 Quest History Logs",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                        if (history.isNotEmpty()) {
                            Text(
                                text = "Clear Logs",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.clickable { viewModel.clearGameHistory() }
                            )
                        }
                    }
                }

                if (history.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🌱", fontSize = 32.sp)
                                Text(
                                    "No logs yet. Play a quest above to record your history!",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(history) { log ->
                        HistoryLogItem(log = log)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun StatsHeaderRow(stats: UserStats, totalGames: Int, activeColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0xFFECEFF1))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SingleStatBox(title = "Total Stars", value = "⭐ ${stats.stars}", color = Color(0xFFF57F17))
            SingleStatBox(title = "Active Streak", value = "🔥 ${stats.streak}", color = Color(0xFFEF6C00))
            SingleStatBox(title = "Quests Played", value = "🎮 $totalGames", color = activeColor)
        }
    }
}

@Composable
fun SingleStatBox(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontSize = 18.sp, color = color, fontWeight = FontWeight.Black)
    }
}

data class BadgeTemplate(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val backColor: Color
)

fun getBadgesConfig() = listOf(
    BadgeTemplate("first_add", "First Steps", "Play 1 Addition match", "🍎", Color(0xFFE8F5E9)),
    BadgeTemplate("first_sub", "Balloon Clipper", "Play 1 Subtraction match", "🎈", Color(0xFFFFF2F2)),
    BadgeTemplate("first_mul", "Matrix Sparkler", "Play 1 Multiplication match", "⭐️", Color(0xFFFFFDE7)),
    BadgeTemplate("first_div", "Cookie Sharer", "Play 1 Division match", "🍪", Color(0xFFF3E5F5)),
    BadgeTemplate("perfect_run", "Perfect Genius", "Earn 100% correct answers", "🏆", Color(0xFFFFF9C4)),
    BadgeTemplate("streak_hero", "Daily Dynamo", "Achieve a 2+ active day streak", "⚡", Color(0xFFFFECE0)),
    BadgeTemplate("star_collector", "Cosmic Lord", "Collect over 25 total Stars", "👑", Color(0xFFE3F2FD))
)

fun determineEarnedBadges(stats: UserStats, history: List<GameHistory>): Set<String> {
    val earned = mutableSetOf<String>()
    if (history.any { it.operation == "ADDITION" }) earned.add("first_add")
    if (history.any { it.operation == "SUBTRACTION" }) earned.add("first_sub")
    if (history.any { it.operation == "MULTIPLICATION" }) earned.add("first_mul")
    if (history.any { it.operation == "DIVISION" }) earned.add("first_div")
    if (history.any { it.correctAnswers == 5 }) earned.add("perfect_run")
    if (stats.streak >= 2) earned.add("streak_hero")
    if (stats.stars >= 25) earned.add("star_collector")
    return earned
}

@Composable
fun BadgeGridItem(badge: BadgeTemplate, isEarned: Boolean, activeColor: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(6.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(if (isEarned) badge.backColor else Color(0xFFF0F0F0), CircleShape)
                .border(1.5.dp, if (isEarned) activeColor else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isEarned) badge.emoji else "❓",
                fontSize = 24.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = badge.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isEarned) Color.DarkGray else Color.Gray
            )
            Text(
                text = badge.description,
                fontSize = 9.sp,
                color = Color.LightGray,
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
fun HistoryLogItem(log: GameHistory) {
    val date = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
    val opSymbol = when (log.operation) {
        "ADDITION" -> "🍎 Addition"
        "SUBTRACTION" -> "🎈 Subtraction"
        "MULTIPLICATION" -> "⭐️ Multiplication"
        else -> "🍪 Division"
    }
    val themeColor = when (log.operation) {
        "ADDITION" -> KidPrimary
        "SUBTRACTION" -> KidPink
        "MULTIPLICATION" -> KidAccent
        else -> KidPurple
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFECEFF1))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accent left block helper vertical strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(themeColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$opSymbol (Lvl ${log.level})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Text(
                    text = date,
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${log.correctAnswers}/${log.totalQuestions} Right",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColor
                )
                Text(
                    text = "Score: ${log.score}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
