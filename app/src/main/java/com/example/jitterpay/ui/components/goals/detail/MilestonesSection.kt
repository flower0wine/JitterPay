package com.example.jitterpay.ui.components.goals.detail

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.goals.Milestone
import com.example.jitterpay.ui.goals.MilestoneIconType
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MilestonesSection(
    milestones: List<Milestone>,
    currentAmount: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "ACHIEVEMENTS",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        milestones.forEachIndexed { index, milestone ->
            MilestoneItem(
                milestone = milestone,
                isAchieved = milestone.isAchieved(currentAmount),
                animationDelay = index * 100
            )
            if (index < milestones.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MilestoneItem(
    milestone: Milestone,
    isAchieved: Boolean,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM
            )
        ) + slideInHorizontally(
            initialOffsetX = { -it / 2 },
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM,
                easing = AnimationConstants.Easing.Entrance
            )
        )
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAchieved)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else
                    Color(0xFF1C1C1E)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = if (isAchieved)
                        MaterialTheme.colorScheme.primary
                    else
                        Color(0xFF2C2C2E)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getMilestoneIcon(milestone.iconType),
                            contentDescription = milestone.title,
                            tint = if (isAchieved) Color.Black else Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = milestone.title,
                            color = if (isAchieved) Color.White else Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isAchieved) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Achieved",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = milestone.description,
                        color = if (isAchieved)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${milestone.percentage}%",
                        color = if (isAchieved) MaterialTheme.colorScheme.primary else Color.Gray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale.US)
                            .format(milestone.amount),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

private fun getMilestoneIcon(iconType: MilestoneIconType): ImageVector {
    return when (iconType) {
        MilestoneIconType.STAR -> Icons.Default.Star
        MilestoneIconType.TROPHY -> Icons.Default.EmojiEvents
        MilestoneIconType.MEDAL -> Icons.Default.MilitaryTech
        MilestoneIconType.CROWN -> Icons.Default.Verified
    }
}
