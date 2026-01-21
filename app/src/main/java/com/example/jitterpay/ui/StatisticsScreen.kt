package com.example.jitterpay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.jitterpay.constants.NavigationTabs
import com.example.jitterpay.ui.components.BottomNavBar
import com.example.jitterpay.ui.components.statistics.*

@Composable
fun StatisticsScreen(
    onBackClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(TimePeriod.MONTHLY) }
    
    // Sample data - replace with actual data from ViewModel/Repository
    val spendingData = remember(selectedPeriod) {
        SpendingData(
            totalSpent = 1280.00,
            percentageChange = 8.2,
            categories = listOf(
                CategorySpending(
                    name = "Food & Drinks",
                    amount = 448.00,
                    percentage = 35.0,
                    color = Color(0xFFE1FF00)
                ),
                CategorySpending(
                    name = "Transport",
                    amount = 192.00,
                    percentage = 15.0,
                    color = Color(0xFF5E5E5E)
                ),
                CategorySpending(
                    name = "Utilities",
                    amount = 320.00,
                    percentage = 25.0,
                    color = Color(0xFF8E8E93)
                ),
                CategorySpending(
                    name = "Entertainment",
                    amount = 320.00,
                    percentage = 25.0,
                    color = Color(0xFF3A3A3C)
                )
            )
        )
    }
    
    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = NavigationTabs.STATS,
                onTabSelected = { tab ->
                    when (tab) {
                        NavigationTabs.HOME -> onNavigateToHome()
                        // Add other navigation cases here
                    }
                },
                onAddClick = onAddTransactionClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            StatisticsHeader(
                onBackClick = onBackClick,
                onShareClick = { /* TODO: Implement share */ }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Period Selector
            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it }
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
