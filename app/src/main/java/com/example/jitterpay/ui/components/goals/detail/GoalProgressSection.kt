package com.example.jitterpay.ui.components.goals.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.goals.GoalData
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GoalProgressSection(
    goal: GoalData,
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
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "goalDetailProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C1E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CURRENT PROGRESS",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale.US)
                            .format(goal.currentAmount),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color(0xFF2C2C2E),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TARGET",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale.US)
                            .format(goal.targetAmount),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (goal.isCompleted) "COMPLETED" else "REMAINING",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = if (goal.isCompleted) "🎉" else NumberFormat.getCurrencyInstance(Locale.US)
                            .format(goal.remainingAmount),
                        color = if (goal.isCompleted)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
