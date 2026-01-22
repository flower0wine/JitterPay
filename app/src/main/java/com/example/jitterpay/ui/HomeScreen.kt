package com.example.jitterpay.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.jitterpay.ui.components.*

@Composable
fun HomeScreen(
    onAddTransactionClick: () -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            // Navigation is now handled by BottomNavBar internally via NavController
            // No need to pass navigation callbacks
        },
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
                balance = "$4,250.00",
                monthlyIncome = "$5,000",
                monthlySpent = "$750"
            )
            
            QuickActions()
            
            TransactionHistory()
            
            // Add extra space at the bottom to ensure content isn't covered by bottom nav
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
