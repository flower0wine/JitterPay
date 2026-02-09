package com.example.jitterpay.ui.components.selectbudget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun BudgetSelectionCard(
    budget: BudgetData?,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressColor = budget?.let {
        when (it.status) {
            BudgetStatus.HEALTHY -> Color(0xFF34C759)
            BudgetStatus.WARNING -> Color(0xFFFFCC00)
            BudgetStatus.CRITICAL -> Color(0xFFFF9500)
            BudgetStatus.OVER_BUDGET -> Color(0xFFFF3B30)
        }
    } ?: Color.Gray

    val animatedProgress by animateFloatAsState(
        targetValue = budget?.progress ?: 0f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.MEDIUM,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "budgetProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2C2C2E) else Color(0xFF1C1C1E)
        ),
        border = if (isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            if (budget == null) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "No Budget",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Don't link to any budget",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = budget.title,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(budget.progress * 100).toInt()}%",
                            color = progressColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = progressColor,
                        trackColor = Color(0xFF3A3A3C),
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${NumberFormat.getCurrencyInstance(Locale.US).format(budget.spentAmount)} of ${NumberFormat.getCurrencyInstance(Locale.US).format(budget.amount)}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
