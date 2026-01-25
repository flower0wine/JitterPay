package com.example.jitterpay.ui.components.goals

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TotalProgressCard(
    totalTarget: Double,
    totalCurrent: Double,
    completedGoals: Int,
    totalGoals: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "cardScale"
    )

    val animatedCurrent by animateFloatAsState(
        targetValue = if (isVisible) totalCurrent.toFloat() else 0f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.COUNTING,
            delayMillis = 200,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "totalCurrent"
    )

    val progress = (totalCurrent / totalTarget).toFloat().coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
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
                        text = "TOTAL SAVED",
                        color = Color.Black.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale.US)
                            .format(animatedCurrent.toDouble()),
                        color = Color.Black,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (completedGoals > 0) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Achievement",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color.Black,
                trackColor = Color.Black.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress * 100).toInt()}% Complete",
                    color = Color.Black.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$completedGoals/$totalGoals Goals",
                    color = Color.Black.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
