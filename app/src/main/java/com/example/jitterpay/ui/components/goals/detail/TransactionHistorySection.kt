package com.example.jitterpay.ui.components.goals.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionHistorySection(
    goalId: Long,
    modifier: Modifier = Modifier
) {
    // TODO: Replace with ViewModel data
    val transactions = remember {
        listOf(
            GoalTransaction(
                id = 1,
                amount = 2500.0,
                type = GoalTransactionType.DEPOSIT,
                date = Date(System.currentTimeMillis() - 86400000 * 2),
                note = "Initial deposit"
            ),
            GoalTransaction(
                id = 2,
                amount = 3000.0,
                type = GoalTransactionType.DEPOSIT,
                date = Date(System.currentTimeMillis() - 86400000 * 5),
                note = "Monthly savings"
            ),
            GoalTransaction(
                id = 3,
                amount = 2000.0,
                type = GoalTransactionType.DEPOSIT,
                date = Date(System.currentTimeMillis() - 86400000 * 10),
                note = "Bonus"
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRANSACTION HISTORY",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${transactions.size} transactions",
                color = Color.Gray,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (transactions.isEmpty()) {
            EmptyTransactionState()
        } else {
            transactions.forEach { transaction ->
                TransactionItem(transaction = transaction)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: GoalTransaction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C1E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (transaction.type == GoalTransactionType.DEPOSIT)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else
                    Color(0xFF2C2C2E)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (transaction.type == GoalTransactionType.DEPOSIT)
                            Icons.AutoMirrored.Filled.TrendingUp
                        else
                            Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = transaction.type.name,
                        tint = if (transaction.type == GoalTransactionType.DEPOSIT)
                            MaterialTheme.colorScheme.primary
                        else
                            Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note ?: if (transaction.type == GoalTransactionType.DEPOSIT)
                        "Deposit"
                    else
                        "Withdrawal",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatDate(transaction.date),
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "${if (transaction.type == GoalTransactionType.DEPOSIT) "+" else "-"}${
                    NumberFormat.getCurrencyInstance(Locale.US).format(transaction.amount)
                }",
                color = if (transaction.type == GoalTransactionType.DEPOSIT)
                    MaterialTheme.colorScheme.primary
                else
                    Color.Gray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyTransactionState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C1E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = "No transactions",
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No transactions yet",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Start adding funds to track your progress",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}

private fun formatDate(date: Date): String {
    val now = Date()
    val diff = now.time - date.time
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        days == 0L -> "Today"
        days == 1L -> "Yesterday"
        days < 7 -> "$days days ago"
        else -> SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date)
    }
}

data class GoalTransaction(
    val id: Long,
    val amount: Double,
    val type: GoalTransactionType,
    val date: Date,
    val note: String? = null
)

enum class GoalTransactionType {
    DEPOSIT,
    WITHDRAWAL
}
