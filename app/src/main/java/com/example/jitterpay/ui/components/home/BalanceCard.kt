package com.example.jitterpay.ui.components.home

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BalanceCard(
    balance: String,
    monthlyIncome: String,
    monthlySpent: String,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Animate card entrance with scale and fade
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "cardScale"
    )

    // Parse values for counting animation
    val balanceValue = remember(balance) {
        balance.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
    }
    val incomeValue = remember(monthlyIncome) {
        monthlyIncome.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
    }
    val spentValue = remember(monthlySpent) {
        monthlySpent.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
    }

    // Animated values with counting effect
    val animatedBalance by animateFloatAsState(
        targetValue = if (isVisible) balanceValue.toFloat() else 0f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.COUNTING,
            delayMillis = 200,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "balance"
    )

    val animatedIncome by animateFloatAsState(
        targetValue = if (isVisible) incomeValue.toFloat() else 0f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.LONG,
            delayMillis = 400,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "income"
    )

    val animatedSpent by animateFloatAsState(
        targetValue = if (isVisible) spentValue.toFloat() else 0f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.LONG,
            delayMillis = 400,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "spent"
    )

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.SHORT,
                easing = AnimationConstants.Easing.Entrance
            )
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.LONG,
                delayMillis = 100,
                easing = AnimationConstants.Easing.Entrance
            )
        ),
        label = "balanceCard"
    ) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .scale(scale)
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background design element (circles) could be added here
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "CURRENT BALANCE",
                            color = Color.Black.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale.US)
                                .format(animatedBalance.toDouble()),
                            color = Color.Black,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Surface(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        color = Color.Black
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "QR Code",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "MONTHLY INCOME",
                            color = Color.Black.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "↗",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = NumberFormat.getCurrencyInstance(Locale.US)
                                    .format(animatedIncome.toDouble()),
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            text = "MONTHLY SPENT",
                            color = Color.Black.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "↙",
                                color = Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = NumberFormat.getCurrencyInstance(Locale.US)
                                    .format(animatedSpent.toDouble()),
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
            }
        }
        }
    }
}

        }
    }
}
