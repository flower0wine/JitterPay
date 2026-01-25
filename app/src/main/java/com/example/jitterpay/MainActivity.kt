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
import com.example.jitterpay.ui.addtransaction.AddTransactionScreen
import com.example.jitterpay.ui.home.HomeScreen
import com.example.jitterpay.ui.ProfileScreen
import com.example.jitterpay.ui.search.SearchScreen
import com.example.jitterpay.ui.statistics.StatisticsScreen
import com.example.jitterpay.ui.addtransaction.AddTransactionViewModel
import com.example.jitterpay.ui.components.BottomNavBar
import com.example.jitterpay.ui.theme.JitterPayTheme
import com.example.jitterpay.ui.animation.SlideTransitions
import com.example.jitterpay.navigation.*

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
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationRoutes.HOME,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(
                route = NavigationRoutes.HOME,
                enterTransition = { with(this) { getBottomNavEnterTransition() } },
                exitTransition = { with(this) { getBottomNavExitTransition() } },
                popEnterTransition = { with(this) { getBottomNavPopEnterTransition() } },
                popExitTransition = { with(this) { getBottomNavPopExitTransition() } }
            ) {
                HomeScreen(navController = navController)
            }

            composable(
                route = NavigationRoutes.STATS,
                enterTransition = { with(this) { getBottomNavEnterTransition() } },
                exitTransition = { with(this) { getBottomNavExitTransition() } },
                popEnterTransition = { with(this) { getBottomNavPopEnterTransition() } },
                popExitTransition = { with(this) { getBottomNavPopExitTransition() } }
            ) {
                StatisticsScreen()
            }

            composable(
                route = NavigationRoutes.WALLET,
                enterTransition = { with(this) { getBottomNavEnterTransition() } },
                exitTransition = { with(this) { getBottomNavExitTransition() } },
                popEnterTransition = { with(this) { getBottomNavPopEnterTransition() } },
                popExitTransition = { with(this) { getBottomNavPopExitTransition() } }
            ) {
                // Placeholder for Wallet screen
                HomeScreen(navController = navController)
            }

            composable(
                route = NavigationRoutes.PROFILE,
                enterTransition = { with(this) { getBottomNavEnterTransition() } },
                exitTransition = { with(this) { getBottomNavExitTransition() } },
                popEnterTransition = { with(this) { getBottomNavPopEnterTransition() } },
                popExitTransition = { with(this) { getBottomNavPopExitTransition() } }
            ) {
                ProfileScreen()
            }

            composable(
                route = NavigationRoutes.ADD_TRANSACTION,
                enterTransition = { SlideTransitions.slideInRight() },
                exitTransition = { SlideTransitions.slideOutRight() },
                popEnterTransition = { SlideTransitions.slideInRight() },
                popExitTransition = { SlideTransitions.slideOutRight() }
            ) {
                AddTransactionScreen(
                    onClose = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = NavigationRoutes.SEARCH,
                enterTransition = { SlideTransitions.slideInRight() },
                exitTransition = { SlideTransitions.slideOutRight() },
                popEnterTransition = { SlideTransitions.slideInRight() },
                popExitTransition = { SlideTransitions.slideOutRight() }
            ) {
                SearchScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
