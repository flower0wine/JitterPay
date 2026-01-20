package com.example.jitterpay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.components.BottomNavBar
import com.example.jitterpay.ui.components.profile.*

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            ProfileTopBar(onBackClick = onBackClick)
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = "PROFILE",
                onTabSelected = { tab ->
                    when (tab) {
                        "CORE" -> onNavigateToHome()
                        "DATA" -> onNavigateToStatistics()
                        "PROFILE" -> { /* Already on profile */ }
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
                .verticalScroll(rememberScrollState())
        ) {
            ProfileHeader(
                userName = "Alex Morgan",
                userEmail = "alex.morgan@flowpay.io",
                isPro = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsSection(title = "BOOKKEEPING") {
                SettingsItem(
                    icon = Icons.Default.AccountBalance,
                    iconTint = Color.Black,
                    iconBackground = MaterialTheme.colorScheme.primary,
                    title = "Budget Settings",
                    onClick = { /* TODO */ }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                SettingsItem(
                    icon = Icons.Default.CurrencyExchange,
                    iconTint = Color.Black,
                    iconBackground = MaterialTheme.colorScheme.primary,
                    title = "Multi-currency",
                    onClick = { /* TODO */ }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                SettingsItem(
                    icon = Icons.Default.Upload,
                    iconTint = Color.Black,
                    iconBackground = MaterialTheme.colorScheme.primary,
                    title = "Data Export",
                    onClick = { /* TODO */ }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsSection(title = "PREFERENCES") {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    iconTint = Color.Black,
                    iconBackground = MaterialTheme.colorScheme.primary,
                    title = "Appearance",
                    trailingText = "Dark",
                    onClick = { /* TODO */ }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                SettingsItem(
                    icon = Icons.Default.Info,
                    iconTint = Color.Black,
                    iconBackground = MaterialTheme.colorScheme.primary,
                    title = "About",
                    onClick = { /* TODO */ }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            ProfileActions(
                onSwitchAccount = { /* TODO */ },
                onSignOut = { /* TODO */ }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProfileTopBar(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "Profile",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
