package com.example.jitterpay.ui.components.statistics

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.theme.GrayText

@Composable
fun CategoryBreakdownItem(
    icon: ImageVector,
    iconBackgroundColor: Color,
    categoryName: String,
    percentage: Double,
    amount: Double,
    modifier: Modifier = Modifier,
    animationDelay: Int = 0
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Animate amount value with counting effect
    val animatedAmount by animateFloatAsState(
        targetValue = if (isVisible) amount.toFloat() else 0f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.LONG,
            delayMillis = animationDelay + 100,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "amount"
    )

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.SHORT,
                delayMillis = animationDelay,
                easing = AnimationConstants.Easing.Entrance
            )
        ) + slideInHorizontally(
            initialOffsetX = { it / 4 },
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM,
                delayMillis = animationDelay,
                easing = AnimationConstants.Easing.Entrance
            )
        ),
        label = "categoryItem"
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = categoryName,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = categoryName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${String.format("%.0f", percentage)}% of total spending",
                        fontSize = 12.sp,
                        color = GrayText
                    )
                }
            }

            Text(
                text = "$${String.format("%.2f", animatedAmount.toDouble())}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
