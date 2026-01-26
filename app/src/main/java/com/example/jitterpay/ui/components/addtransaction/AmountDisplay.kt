package com.example.jitterpay.ui.components.addtransaction

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
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
import java.math.BigDecimal
import java.text.DecimalFormat

@Composable
fun AmountDisplay(
    amount: BigDecimal,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var previousAmount by remember { mutableStateOf(amount) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // 入场动画：淡入 + 轻微缩放
    val initialScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.SHORT,
            delayMillis = 150,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "amountScale"
    )

    // 数字变化时的弹跳动画
    val changeScale by animateFloatAsState(
        targetValue = if (amount != previousAmount) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "amountChangeScale",
        finishedListener = {
            previousAmount = amount
        }
    )

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.SHORT,
                delayMillis = 150,
                easing = AnimationConstants.Easing.Entrance
            )
        ),
        label = "amountDisplay"
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .scale(initialScale * changeScale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val formattedAmount = DecimalFormat("#0.00").format(amount)
            
            // 使用 AnimatedContent 实现数字切换动画
            AnimatedContent(
                targetState = formattedAmount,
                transitionSpec = {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = AnimationConstants.Duration.MICRO,
                            easing = AnimationConstants.Easing.Entrance
                        )
                    ) togetherWith fadeOut(
                        animationSpec = tween(
                            durationMillis = AnimationConstants.Duration.MICRO,
                            easing = AnimationConstants.Easing.Exit
                        )
                    )
                },
                label = "amountText"
            ) { targetAmount ->
                Text(
                    text = "$$targetAmount",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
