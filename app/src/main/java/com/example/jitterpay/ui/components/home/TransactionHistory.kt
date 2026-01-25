package com.example.jitterpay.ui.components.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.ui.animation.AnimationConstants
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 交易记录组件 - 从数据库加载真实数据
 */
@Composable
fun TransactionHistory(
    transactions: List<TransactionEntity> = emptyList(),
    onDeleteTransaction: (TransactionEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HISTORY",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { }) {
                Text(
                    text = "VIEW ARCHIVE",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    style = LocalTextStyle.current.copy(
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (transactions.isEmpty()) {
            // 空状态显示
            EmptyTransactionState()
        } else {
            transactions.forEachIndexed { index, transaction ->
                key(transaction.id) {
                    var isVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        isVisible = true
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { it / 4 },
                            animationSpec = tween(
                                durationMillis = AnimationConstants.Duration.MEDIUM,
                                delayMillis = AnimationConstants.Stagger.listItemDelay(index),
                                easing = AnimationConstants.Easing.Entrance
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = AnimationConstants.Duration.MEDIUM,
                                delayMillis = AnimationConstants.Stagger.listItemDelay(index)
                            )
                        ),
                        exit = slideOutHorizontally(
                            targetOffsetX = { -it / 4 },
                            animationSpec = tween(
                                durationMillis = AnimationConstants.Duration.SHORT,
                                easing = AnimationConstants.Easing.Exit
                            )
                        ) + fadeOut(
                            animationSpec = tween(
                                durationMillis = AnimationConstants.Duration.SHORT
                            )
                        ),
                        label = "transaction_${transaction.id}"
                    ) {
                        TransactionItem(
                            transaction = transaction,
                            onDelete = { onDeleteTransaction(transaction) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * 空交易状态
 */
@Composable
private fun EmptyTransactionState() {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(600)) + scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(600, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.4f),
                modifier = Modifier.size(72.dp)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "No Transactions Yet",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Start tracking your spending by\nadding your first transaction",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

/**
 * 交易项组件
 */
@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    onDelete: () -> Unit = {}
) {
    val isIncome = transaction.type == TransactionType.INCOME.name

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = if (isIncome) MaterialTheme.colorScheme.primary else Color(0xFF1C1C1E)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = getCategoryIcon(transaction.category),
                    contentDescription = null,
                    tint = if (isIncome) Color.Black else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.category,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatTransactionDate(transaction.dateMillis),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = transaction.getFormattedAmount(),
                color = if (isIncome) MaterialTheme.colorScheme.primary else Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = transaction.status,
                color = Color.Gray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 获取分类对应的图标
 */
private fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "dining", "food & drink" -> Icons.Default.Restaurant
        "groceries" -> Icons.Default.ShoppingCart
        "travel", "transport" -> Icons.Default.Flight
        "shopping" -> Icons.Default.ShoppingBag
        "health" -> Icons.Default.Favorite
        "bills", "utilities" -> Icons.Default.Receipt
        "income" -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.Category
    }
}

/**
 * 格式化交易日期显示
 */
private fun formatTransactionDate(dateMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - dateMillis

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} min ago"
        diff < TimeUnit.DAYS.toMillis(1) -> {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            timeFormat.format(Date(dateMillis))
        }
        diff < TimeUnit.DAYS.toMillis(2) -> "YESTERDAY"
        diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} DAYS AGO"
        else -> {
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            dateFormat.format(Date(dateMillis))
        }
    }
}
