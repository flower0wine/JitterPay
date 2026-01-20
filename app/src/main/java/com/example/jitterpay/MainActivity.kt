package com.example.jitterpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.jitterpay.ui.AddTransactionScreen
import com.example.jitterpay.ui.HomeScreen
import com.example.jitterpay.ui.ProfileScreen
import com.example.jitterpay.ui.StatisticsScreen
import com.example.jitterpay.ui.theme.JitterPayTheme

enum class Screen {
    HOME, STATISTICS, ADD_TRANSACTION, PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JitterPayTheme {
                var currentScreen by remember { mutableStateOf(Screen.HOME) }
                
                when (currentScreen) {
                    Screen.HOME -> {
                        HomeScreen(
                            onAddTransactionClick = { currentScreen = Screen.ADD_TRANSACTION },
                            onNavigateToStatistics = { currentScreen = Screen.STATISTICS },
                            onNavigateToProfile = { currentScreen = Screen.PROFILE }
                        )
                    }
                    Screen.STATISTICS -> {
                        StatisticsScreen(
                            onBackClick = { currentScreen = Screen.HOME },
                            onAddTransactionClick = { currentScreen = Screen.ADD_TRANSACTION },
                            onNavigateToHome = { currentScreen = Screen.HOME }
                        )
                    }
                    Screen.ADD_TRANSACTION -> {
                        AddTransactionScreen(
                            onClose = { currentScreen = Screen.HOME },
                            onSave = { type, amount, category, date ->
                                // TODO: Save transaction to database/state
                                println("Transaction saved: $type, $amount, $category, $date")
                                currentScreen = Screen.HOME
                            }
                        )
                    }
                    Screen.PROFILE -> {
                        ProfileScreen(
                            onBackClick = { currentScreen = Screen.HOME },
                            onAddTransactionClick = { currentScreen = Screen.ADD_TRANSACTION },
                            onNavigateToHome = { currentScreen = Screen.HOME },
                            onNavigateToStatistics = { currentScreen = Screen.STATISTICS }
                        )
                    }
                }
            }
        }
    }
}
