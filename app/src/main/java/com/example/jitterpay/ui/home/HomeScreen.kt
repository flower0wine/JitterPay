package com.example.jitterpay.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.components.home.BalanceCard
import com.example.jitterpay.ui.components.home.QuickActions
import com.example.jitterpay.ui.components.home.TopHeader
import com.example.jitterpay.ui.components.home.TransactionHistory
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 格式化金额显示
    val balanceFormatted = formatCurrency(uiState.totalBalance)
    val incomeFormatted = formatCurrency(uiState.totalBalance.coerceAtLeast(0L))
    val spentFormatted = formatCurrency((-uiState.totalBalance).coerceAtLeast(0L))

    // 计算月度收入和支出（简化计算）
    val monthlyIncome = uiState.transactions
        .filter { it.type == TransactionType.INCOME.name }
        .sumOf { it.amountCents }
    val monthlySpent = uiState.transactions
        .filter { it.type == TransactionType.EXPENSE.name }
        .sumOf { it.amountCents }

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
                TopHeader()

                Spacer(modifier = Modifier.height(20.dp))

                BalanceCard(
                    balance = balanceFormatted,
                    monthlyIncome = formatCurrency(monthlyIncome),
                    monthlySpent = formatCurrency(monthlySpent)
                )

                QuickActions()

                // 传递真实交易数据到TransactionHistory
                TransactionHistory(
                    transactions = uiState.transactions,
                    onDeleteTransaction = { viewModel.deleteTransaction(it) }
                )

                // Add extra space at the bottom to ensure content isn't covered by bottom nav
                Spacer(modifier = Modifier.height(100.dp))
            }
    }
}

/**
 * 格式化货币金额（分转换为美元）
 */
private fun formatCurrency(cents: Long): String {
    val amount = cents.toDouble() / 100.0
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
}
