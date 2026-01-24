package com.example.jitterpay.ui.statistics

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.components.statistics.*

@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 转换分类数据为StatisticsScreen需要的格式
    val spendingData = remember(uiState.categories, uiState.totalSpent) {
        SpendingData(
            totalSpent = uiState.totalSpent,
            percentageChange = 0.0, // 可以从历史数据计算
            categories = uiState.categories.map { categorySpending ->
                CategorySpending(
                    name = categorySpending.name,
                    amount = categorySpending.amount,
                    percentage = categorySpending.percentage,
                    color = getCategoryColor(categorySpending.name)
                )
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Header - simplified without back button since it's now a main destination
            StatisticsHeader(
                onBackClick = { /* Back navigation handled by system */ },
                onShareClick = { /* TODO: Implement share */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Period Selector
            PeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = { viewModel.selectPeriod(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Donut Chart
            SpendingDonutChart(data = spendingData)

            Spacer(modifier = Modifier.height(24.dp))

            // Category Breakdown
            CategoryBreakdownList(categories = spendingData.categories)

            Spacer(modifier = Modifier.height(24.dp))

            // Download Button
            DownloadReportButton(
                onClick = { /* TODO: Implement PDF download */ }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * 获取分类对应的颜色
 */
private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "dining", "food & drink" -> Color(0xFFE1FF00)
        "groceries" -> Color(0xFF4CAF50)
        "travel", "transport" -> Color(0xFF5E5E5E)
        "shopping" -> Color(0xFFE91E63)
        "health" -> Color(0xFFFF5722)
        "bills", "utilities" -> Color(0xFF8E8E93)
        "entertainment" -> Color(0xFF3A3A3C)
        else -> Color(0xFF5E5E5E)
    }
}
