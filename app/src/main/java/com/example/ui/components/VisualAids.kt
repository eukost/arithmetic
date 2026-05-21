package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// We can define playful colors for our children theme.
val PastelBlue = Color(0xFFE3F2FD)
val KidPrimary = Color(0xFF4CAF50)
val KidSecondary = Color(0xFF2196F3)
val KidAccent = Color(0xFFFF9800)
val KidPink = Color(0xFFE91E63)
val KidYellow = Color(0xFFFFEB3B)
val KidPurple = Color(0xFF9C27B0)

@Composable
fun MainVisualAid(
    operation: String, // "+", "-", "×", "÷"
    num1: Int,
    num2: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💡 Visual Magic Helper",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = KidPrimary
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            when (operation) {
                "+" -> AdditionVisualAid(num1, num2)
                "-" -> SubtractionVisualAid(num1, num2)
                "×" -> MultiplicationVisualAid(num1, num2)
                "÷" -> DivisionVisualAid(num1, num2)
                else -> Text("Help comes here!")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdditionVisualAid(num1: Int, num2: Int) {
    // Playful anim to float items up and down slightly
    val infiniteTransition = rememberInfiniteTransition(label = "additionFloat")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingOffset"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group 1
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .background(PastelBlue, RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "$num1",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = KidSecondary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                ContextFlowRow(
                    modifier = Modifier.wrapContentSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..num1) {
                        Text(
                            text = "🍎",
                            fontSize = 24.sp,
                            modifier = Modifier
                                .padding(2.dp)
                                .offset(y = if (i % 2 == 0) offsetY.dp else (-offsetY).dp)
                        )
                    }
                }
            }

            // Plus inside a circle
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(36.dp)
                    .background(KidAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            // Group 2
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFFFFF3E0), RoundedCornerShape(16.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "$num2",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = KidAccent
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                ContextFlowRow(
                    modifier = Modifier.wrapContentSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..num2) {
                        Text(
                            text = "🍊",
                            fontSize = 24.sp,
                            modifier = Modifier
                                .padding(2.dp)
                                .offset(y = if (i % 2 != 0) offsetY.dp else (-offsetY).dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Count them all together:",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Total combined group
        ContextFlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE8F5E9), RoundedCornerShape(20.dp))
                .border(2.dp, KidPrimary, RoundedCornerShape(20.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            for (i in 1..num1) {
                Text(
                    text = "🍎",
                    fontSize = 26.sp,
                    modifier = Modifier.padding(4.dp)
                )
            }
            for (i in 1..num2) {
                Text(
                    text = "🍊",
                    fontSize = 26.sp,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Total counts = ${num1 + num2}!",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = KidPrimary
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubtractionVisualAid(num1: Int, num2: Int) {
    val remaining = (num1 - num2).coerceAtLeast(0)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Start with $num1 balloons, fly away $num2:",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))

        ContextFlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF2F2), RoundedCornerShape(20.dp))
                .border(2.dp, Color(0xFFFFB2B2), RoundedCornerShape(20.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            // Draw active remaining balloons
            for (i in 1..remaining) {
                Box(
                    modifier = Modifier.padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎈", fontSize = 28.sp)
                }
            }

            // Draw popped / crossed-off balloons
            for (i in 1..num2) {
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .scale(0.85f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎈",
                        fontSize = 28.sp,
                        color = Color.Gray.copy(alpha = 0.3f), // Faded out
                        modifier = Modifier.scale(0.8f)
                    )
                    Text(
                        text = "❌",
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Balloons left = $remaining!",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = KidPink
            )
        )
    }
}

@Composable
fun MultiplicationVisualAid(num1: Int, num2: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$num1 rows of $num2 stars:",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Grid of stars
        Column(
            modifier = Modifier
                .background(Color(0xFFFFFDE7), RoundedCornerShape(20.dp))
                .border(2.dp, Color(0xFFFFF59D), RoundedCornerShape(20.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (r in 1..num1) {
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small row index indicator
                    Text(
                        text = "Row $r:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )

                    for (c in 1..num2) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Star",
                            tint = KidAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "$num1 × $num2 = ${num1 * num2} stars total!",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = KidAccent
            )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DivisionVisualAid(num1: Int, num2: Int) {
    val shareAmount = if (num2 > 0) num1 / num2 else 0
    val remainder = if (num2 > 0) num1 % num2 else 0

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Share $num1 cookies evenly among $num2 plates:",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Plates row / grid
        ContextFlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            for (p in 1..num2) {
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(90.dp)
                        .background(Color(0xFFECEFF1), CircleShape)
                        .border(2.dp, Color(0xFFCFD8DC), CircleShape)
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Plate $p",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ContextFlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Center
                    ) {
                        for (c in 1..shareAmount) {
                            Text(
                                text = "🍪",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(1.dp)
                            )
                        }
                    }
                }
            }
        }

        if (remainder > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .background(Color(0xFFFFF9C4), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Leftover cookies: ",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                repeat(remainder) {
                    Text(text = "🍪", fontSize = 16.sp, modifier = Modifier.padding(1.dp))
                }
                Text(
                    text = " ($remainder left)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Each plate gets $shareAmount cookies!",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = KidPurple
            )
        )
    }
}

// FlowRow layout wrapper for backwards compatibility or older compose runtime
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContextFlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    content: @Composable FlowRowScope.() -> Unit
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        maxItemsInEachRow = maxItemsInEachRow,
        content = content
    )
}
