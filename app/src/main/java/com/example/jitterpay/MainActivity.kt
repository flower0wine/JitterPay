package com.example.jitterpay

import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState

import dagger.hilt.android.AndroidEntryPoint

import com.example.jitterpay.constants.NavigationRoutes
import com.example.jitterpay.ui.AddTransactionScreen
import com.example.jitterpay.ui.HomeScreen
import com.example.jitterpay.ui.ProfileScreen
import com.example.jitterpay.ui.StatisticsScreen
import com.example.jitterpay.ui.addtransaction.AddTransactionViewModel
import com.example.jitterpay.ui.components.BottomNavBar
import com.example.jitterpay.ui.theme.JitterPayTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JitterPayTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Determine if we should show the bottom nav bar
                val showBottomNav = currentRoute in listOf(
                    NavigationRoutes.HOME,
                    NavigationRoutes.STATS,
                    NavigationRoutes.WALLET,
                    NavigationRoutes.PROFILE
                )

                JitterPayApp(
                    navController = navController,
                    showBottomNav = showBottomNav
                )
            }
        }
    }
}

@Composable
fun JitterPayApp(
    navController: NavHostController,
    showBottomNav: Boolean = true
) {
    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    navController = navController,
                    onAddClick = {
                        navController.navigate(NavigationRoutes.ADD_TRANSACTION)
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationRoutes.HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavigationRoutes.HOME) {
                HomeScreen(
                    onAddTransactionClick = {
                        navController.navigate(NavigationRoutes.ADD_TRANSACTION)
                    }
                )
            }

            composable(NavigationRoutes.STATS) {
                StatisticsScreen(
                    onAddTransactionClick = {
                        navController.navigate(NavigationRoutes.ADD_TRANSACTION)
                    }
                )
            }

            composable(NavigationRoutes.WALLET) {
                // Placeholder for Wallet screen
                HomeScreen(
                    onAddTransactionClick = {
                        navController.navigate(NavigationRoutes.ADD_TRANSACTION)
                    }
                )
            }

            composable(NavigationRoutes.PROFILE) {
                ProfileScreen(
                    onAddTransactionClick = {
                        navController.navigate(NavigationRoutes.ADD_TRANSACTION)
                    }
                )
            }

            composable(NavigationRoutes.ADD_TRANSACTION) {
                val viewModel: AddTransactionViewModel = hiltViewModel()

                AddTransactionScreen(
                    onClose = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
