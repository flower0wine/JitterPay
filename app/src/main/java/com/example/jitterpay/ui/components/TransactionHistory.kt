package com.example.jitterpay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Transaction(
    val title: String,
    val category: String,
    val time: String,
    val amount: String,
    val status: String,
    val icon: ImageVector,
    val isIncome: Boolean = false
)

@Composable
fun TransactionHistory(
    modifier: Modifier = Modifier
) {
    val transactions = listOf(
        Transaction("Starbucks Coffee", "FOOD & DRINK", "09:15 AM", "-$5.50", "COMPLETED", Icons.Default.LocalCafe),
        Transaction("Uber Premier", "TRANSPORT", "YESTERDAY", "-$15.00", "COMPLETED", Icons.Default.DirectionsCar),
        Transaction("Apple Retail", "SHOPPING", "2 DAYS AGO", "-$129.00", "COMPLETED", Icons.Default.ShoppingCart),
        Transaction("Monthly Salary", "INCOME", "AUG 01", "+$5,000.00", "RECEIVED", Icons.Default.AccountBalanceWallet, true)
    )

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
        
        transactions.forEach { transaction ->
            TransactionItem(transaction)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = if (transaction.isIncome) MaterialTheme.colorScheme.primary else Color(0xFF1C1C1E)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = transaction.icon,
                    contentDescription = null,
                    tint = if (transaction.isIncome) Color.Black else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${transaction.category} • ${transaction.time}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = transaction.amount,
                color = if (transaction.isIncome) MaterialTheme.colorScheme.primary else Color.White,
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
