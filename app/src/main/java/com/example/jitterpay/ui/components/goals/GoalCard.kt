package com.example.jitterpay.ui.components.goals

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.goals.GoalData
import com.example.jitterpay.ui.goals.GoalIconType
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GoalCard(
    goal: GoalData,
    onCardClick: () -> Unit,
    onAddFunds: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) goal.progress else 0f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.LONG,
            delayMillis = 300,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "goalProgress"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM
            )
        ) + slideInHorizontally(
            initialOffsetX = { it / 2 },
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM,
                easing = AnimationConstants.Easing.Entrance
            )
        ),
        label = "goalCard_${goal.id}"
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable(onClick = onCardClick),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1C1C1E)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = if (goal.isCompleted) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = getGoalIcon(goal.iconType),
                                    contentDescription = goal.title,
                                    tint = if (goal.isCompleted) Color.Black else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = goal.title,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (goal.isCompleted) "Completed!" else 
                                    "${NumberFormat.getCurrencyInstance(Locale.US).format(goal.remainingAmount)} to go",
                                color = if (goal.isCompleted) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (!goal.isCompleted) {
                        IconButton(
                            onClick = onAddFunds,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Funds",
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color(0xFF2C2C2E),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale.US)
                            .format(goal.currentAmount),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale.US)
                            .format(goal.targetAmount),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }

                if (goal.isCompleted) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AchievementBadge()
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = "Achievement",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Goal Achieved!",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun getGoalIcon(iconType: GoalIconType): ImageVector {
    return when (iconType) {
        GoalIconType.SHIELD -> Icons.Default.Shield
        GoalIconType.FLIGHT -> Icons.Default.Flight
        GoalIconType.LAPTOP -> Icons.Default.Laptop
        GoalIconType.HOME -> Icons.Default.Home
        GoalIconType.CAR -> Icons.Default.DirectionsCar
        GoalIconType.EDUCATION -> Icons.Default.School
        GoalIconType.HEALTH -> Icons.Default.FavoriteBorder
        GoalIconType.GIFT -> Icons.Default.CardGiftcard
    }
}
