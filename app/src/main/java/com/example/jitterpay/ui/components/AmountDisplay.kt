package com.example.jitterpay.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.theme.GrayText
import java.math.BigDecimal
import java.text.DecimalFormat

@Composable
fun AmountDisplay(
    amount: BigDecimal,
    modifier: Modifier = Modifier
) {
    // UI层格式化：始终保留两位小数
    val formattedAmount = DecimalFormat("#0.00").format(amount)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$$formattedAmount",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
