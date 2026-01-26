package com.example.jitterpay.ui.components.recurring

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import com.example.jitterpay.ui.recurring.RecurringFrequency
import com.example.jitterpay.ui.recurring.RecurringTransaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecurringCard(
    recurring: RecurringTransaction,
    onCardClick: () -> Unit,
    onToggleActive: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(150)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM,
                easing = AnimationConstants.Easing.Entrance
            )
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM,
                easing = AnimationConstants.Easing.Entrance
            )
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1C1C1E))
                .clickable(onClick = onCardClick)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(getCategoryColor(recurring.category).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(recurring.category),
                        contentDescription = recurring.category,
                        tint = getCategoryColor(recurring.category),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Transaction Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recurring.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = recurring.frequency.displayName,
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = " • ",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Next: ${formatDate(recurring.nextExecutionDate)}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Amount and Toggle
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(recurring.amount),
                        color = if (recurring.type == "INCOME") 
                            MaterialTheme.colorScheme.primary 
                        else 
                            Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Switch(
                        checked = recurring.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF2C2C2E)
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
    }
}

private fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "transport" -> Icons.Default.DirectionsCar
        "entertainment" -> Icons.Default.Movie
        "income" -> Icons.Default.AttachMoney
        "food" -> Icons.Default.Restaurant
        "shopping" -> Icons.Default.ShoppingBag
        "bills" -> Icons.Default.Receipt
        "health" -> Icons.Default.LocalHospital
        else -> Icons.Default.Category
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "transport" -> Color(0xFF5AC8FA)
        "entertainment" -> Color(0xFFFF9500)
        "income" -> Color(0xFF34C759)
        "food" -> Color(0xFFFF3B30)
        "shopping" -> Color(0xFFAF52DE)
        "bills" -> Color(0xFFFF2D55)
        "health" -> Color(0xFF32ADE6)
        else -> Color(0xFF8E8E93)
    }
}

private fun formatCurrency(cents: Long): String {
    val amount = cents.toDouble() / 100.0
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
}

private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = timestamp - now
    val days = diff / (1000 * 60 * 60 * 24)
    
    return when {
        days == 0L -> "Today"
        days == 1L -> "Tomorrow"
        days < 7 -> "${days}d"
        days < 30 -> "${days / 7}w"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
    }
}
