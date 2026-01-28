package com.example.jitterpay

import android.os.Bundle
import android.util.Log
import android.content.res.Resources

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState

import dagger.hilt.android.AndroidEntryPoint

import com.example.jitterpay.constants.NavigationRoutes
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.jitterpay.ui.addtransaction.AddTransactionScreen
import com.example.jitterpay.ui.avatar.AvatarSelectionScreen
import com.example.jitterpay.ui.edittransaction.EditTransactionScreen
import com.example.jitterpay.ui.goals.AddFundsScreen
import com.example.jitterpay.ui.goals.CreateGoalScreen
import com.example.jitterpay.ui.goals.EditGoalScreen
import com.example.jitterpay.ui.goals.GoalDetailScreen
import com.example.jitterpay.ui.goals.GoalsScreen
import com.example.jitterpay.ui.goals.WithdrawFundsScreen
import com.example.jitterpay.ui.home.HomeScreen
import com.example.jitterpay.ui.profile.ProfileScreen
import com.example.jitterpay.ui.recurring.AddRecurringScreen
import com.example.jitterpay.ui.recurring.RecurringScreen
import com.example.jitterpay.ui.search.SearchScreen
import com.example.jitterpay.ui.statistics.StatisticsScreen
import com.example.jitterpay.ui.components.BottomNavBar
import com.example.jitterpay.ui.theme.JitterPayTheme
import com.example.jitterpay.ui.animation.SlideTransitions
import com.example.jitterpay.navigation.*
import com.example.jitterpay.ui.recurring.RecurringDetailScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display with error handling
        try {
            enableEdgeToEdge()
        } catch (e: Resources.NotFoundException) {
            Log.e("MainActivity", "Resource not found for EdgeToEdge", e)
            // Continue without edge-to-edge if resources are missing
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to enable edge-to-edge", e)
            // Continue without edge-to-edge for stability
        }
        setContent {
            JitterPayTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Determine if we should show the bottom nav bar
                val showBottomNav = currentRoute in listOf(
                    NavigationRoutes.HOME,
                    NavigationRoutes.STATS,
                    NavigationRoutes.GOALS,
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
        ProvideNavController(navController) { modifier ->
            NavHost(
                navController = navController,
                startDestination = NavigationRoutes.HOME,
                modifier = modifier.padding(paddingValues)
            ) {
                composable(
                    route = NavigationRoutes.HOME,
                    enterTransition = { with(this) { getBottomNavEnterTransition() } },
                    exitTransition = { with(this) { getBottomNavExitTransition() } },
                    popEnterTransition = { with(this) { getBottomNavPopEnterTransition() } },
                    popExitTransition = { with(this) { getBottomNavPopExitTransition() } }
                ) {
                    HomeScreen()
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
                    route = NavigationRoutes.GOALS,
                    enterTransition = { with(this) { getBottomNavEnterTransition() } },
                    exitTransition = { with(this) { getBottomNavExitTransition() } },
                    popEnterTransition = { with(this) { getBottomNavPopEnterTransition() } },
                    popExitTransition = { with(this) { getBottomNavPopExitTransition() } }
                ) {
                    GoalsScreen()
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
                    route = NavigationRoutes.EDIT_TRANSACTION,
                    arguments = listOf(
                        navArgument("transactionId") { type = NavType.LongType }
                    ),
                    enterTransition = { SlideTransitions.slideInRight() },
                    exitTransition = { SlideTransitions.slideOutRight() },
                    popEnterTransition = { SlideTransitions.slideInRight() },
                    popExitTransition = { SlideTransitions.slideOutRight() }
                ) {
                    EditTransactionScreen(
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

                composable(
                    route = NavigationRoutes.AVATAR_SELECTION,
                    enterTransition = { SlideTransitions.slideInRight() },
                    exitTransition = { SlideTransitions.slideOutRight() },
                    popEnterTransition = { SlideTransitions.slideInRight() },
                    popExitTransition = { SlideTransitions.slideOutRight() }
                ) {
                    AvatarSelectionScreen()
                }

                composable(
                    route = NavigationRoutes.ADD_GOAL,
                    enterTransition = { SlideTransitions.slideInRight() },
                    exitTransition = { SlideTransitions.slideOutRight() },
                    popEnterTransition = { SlideTransitions.slideInRight() },
                    popExitTransition = { SlideTransitions.slideOutRight() }
                ) {
                    CreateGoalScreen()
                }

                composable(
                    route = NavigationRoutes.GOAL_DETAIL,
                    arguments = listOf(
                        navArgument("goalId") { type = NavType.LongType }
                    ),
                    enterTransition = { SlideTransitions.slideInRight() },
                    exitTransition = { SlideTransitions.slideOutRight() },
                    popEnterTransition = { SlideTransitions.slideInRight() },
                    popExitTransition = { SlideTransitions.slideOutRight() }
                ) { backStackEntry ->
                    val goalId = backStackEntry.arguments?.getLong("goalId") ?: 0L
                    GoalDetailScreen(
                        goalId = goalId
                    )
                }

                composable(
                    route = NavigationRoutes.ADD_FUNDS,
                    arguments = listOf(
                        navArgument("goalId") { type = NavType.LongType }
                    ),
                    enterTransition = { SlideTransitions.slideInRight() },
                    exitTransition = { SlideTransitions.slideOutRight() },
                    popEnterTransition = { SlideTransitions.slideInRight() },
                    popExitTransition = { SlideTransitions.slideOutRight() }
                ) { backStackEntry ->
                    val goalId = backStackEntry.arguments?.getLong("goalId") ?: 0L
                    AddFundsScreen(
                        goalId = goalId
                    )
                }

                composable(
                    route = NavigationRoutes.WITHDRAW_FUNDS,
                    arguments = listOf(
                        navArgument("goalId") { type = NavType.LongType }
                    ),
                    enterTransition = { SlideTransitions.slideInRight() },
                    exitTransition = { SlideTransitions.slideOutRight() },
                    popEnterTransition = { SlideTransitions.slideInRight() },
                    popExitTransition = { SlideTransitions.slideOutRight() }
                ) { backStackEntry ->
                    val goalId = backStackEntry.arguments?.getLong("goalId") ?: 0L
                    WithdrawFundsScreen(
                        goalId = goalId
                    )
                }

                composable(
                    route = NavigationRoutes.EDIT_GOAL,
                    arguments = listOf(
                        navArgument("goalId") { type = NavType.LongType }
                    ),
                    enterTransition = { SlideTransitions.slideInRight() },
                    exitTransition = { SlideTransitions.slideOutRight() },
                    popEnterTransition = { SlideTransitions.slideInRight() },
                    popExitTransition = { SlideTransitions.slideOutRight() }
                ) { backStackEntry ->
                    val goalId = backStackEntry.arguments?.getLong("goalId") ?: 0L
                    EditGoalScreen(
                        goalId = goalId
                    )
                }

                composable(
                    route = NavigationRoutes.RECURRING,
                    enterTransition = { SlideTransitions.slideInRight() },
                    exitTransition = { SlideTransitions.slideOutRight() },
                    popEnterTransition = { SlideTransitions.slideInRight() },
                    popExitTransition = { SlideTransitions.slideOutRight() }
                ) {
                    RecurringScreen()
                }

                composable(
                    route = NavigationRoutes.ADD_RECURRING,
                    enterTransition = { SlideTransitions.slideInRight() },
                    exitTransition = { SlideTransitions.slideOutRight() },
                    popEnterTransition = { SlideTransitions.slideInRight() },
                    popExitTransition = { SlideTransitions.slideOutRight() }
                ) {
                    AddRecurringScreen(
                        onClose = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = NavigationRoutes.RECURRING_DETAIL,
                    arguments = listOf(
                        navArgument("recurringId") { type = NavType.LongType }
                    ),
                    enterTransition = { SlideTransitions.slideInRight() },
                    exitTransition = { SlideTransitions.slideOutRight() },
                    popEnterTransition = { SlideTransitions.slideInRight() },
                    popExitTransition = { SlideTransitions.slideOutRight() }
                ) {
                    RecurringDetailScreen()
                }
            }
        }
    }
}
