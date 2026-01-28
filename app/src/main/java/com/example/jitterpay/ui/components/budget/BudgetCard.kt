package com.example.jitterpay.ui.components.budget

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.budget.BudgetData
import com.example.jitterpay.ui.budget.BudgetStatus
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetCard(
    budget: BudgetData,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = budget.progress,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.LONG,
            delayMillis = 200,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "budgetProgress"
    )

    val progressColor = when (budget.status) {
        BudgetStatus.HEALTHY -> MaterialTheme.colorScheme.primary
        BudgetStatus.WARNING -> Color(0xFFFFCC00)
        BudgetStatus.CRITICAL -> Color(0xFFFF9500)
        BudgetStatus.OVER_BUDGET -> Color(0xFFFF3B30)
    }

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
                    Column {
                        Text(
                            text = budget.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = budget.periodDescription,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = progressColor.copy(alpha = 0.2f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (budget.status) {
                                    BudgetStatus.HEALTHY -> Icons.Default.CheckCircle
                                    BudgetStatus.WARNING -> Icons.Default.Warning
                                    BudgetStatus.CRITICAL -> Icons.Default.Error
                                    BudgetStatus.OVER_BUDGET -> Icons.Default.Cancel
                                },
                                contentDescription = null,
                                tint = progressColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "SPENT",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale.US)
                                .format(budget.spentAmount),
                            color = progressColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "OF ${NumberFormat.getCurrencyInstance(Locale.US).format(budget.amount)}",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!budget.isOverBudget) {
                            Text(
                                text = "${NumberFormat.getCurrencyInstance(Locale.US).format(budget.remainingAmount)} left",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "${NumberFormat.getCurrencyInstance(Locale.US).format(budget.spentAmount - budget.amount)} over",
                                color = progressColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = Color(0xFF2C2C2E),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${(budget.progress * 100).toInt()}% used",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
}
