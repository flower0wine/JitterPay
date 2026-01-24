package com.example.jitterpay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.jitterpay.constants.ContentDescriptions
import com.example.jitterpay.constants.NavigationRoutes
import com.example.jitterpay.constants.NavigationTabs

/**
 * Navigation routes for bottom navigation
 */
sealed class BottomNavRoute(
    val route: String,
    val tab: String,
    val icon: ImageVector,
    val label: String
) {
    data object Home : BottomNavRoute(
        route = NavigationRoutes.HOME,
        tab = NavigationTabs.HOME,
        icon = Icons.Default.Home,
        label = NavigationTabs.HOME
    )

    data object Stats : BottomNavRoute(
        route = NavigationRoutes.STATS,
        tab = NavigationTabs.STATS,
        icon = Icons.Default.BarChart,
        label = NavigationTabs.STATS
    )

    data object Wallet : BottomNavRoute(
        route = NavigationRoutes.WALLET,
        tab = NavigationTabs.WALLET,
        icon = Icons.Default.AccountBalanceWallet,
        label = NavigationTabs.WALLET
    )

    data object Profile : BottomNavRoute(
        route = NavigationRoutes.PROFILE,
        tab = NavigationTabs.PROFILE,
        icon = Icons.Default.Person,
        label = NavigationTabs.PROFILE
    )

    companion object {
        val items = listOf(Home, Stats, Wallet, Profile)
    }
}

@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Bottom Bar Background
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color.Black)
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First two items
            BottomNavRoute.items.take(2).forEach { navItem ->
                NavBarItem(
                    icon = navItem.icon,
                    label = navItem.label,
                    isSelected = currentRoute == navItem.route,
                    onClick = {
                        navController.navigate(navItem.route) {
                            // Pop up to the target route to clear intermediate entries
                            popUpTo(navItem.route) { inclusive = true }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Spacer for the center floating button
            Spacer(modifier = Modifier.weight(1f))

            // Last two items
            BottomNavRoute.items.drop(2).forEach { navItem ->
                NavBarItem(
                    icon = navItem.icon,
                    label = navItem.label,
                    isSelected = currentRoute == navItem.route,
                    onClick = {
                        navController.navigate(navItem.route) {
                            // Pop up to the target route to clear intermediate entries
                            popUpTo(navItem.route) { inclusive = true }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Floating Add Button
        Box(
            modifier = Modifier
                .padding(bottom = 30.dp)
                .size(64.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary,
                    spotColor = MaterialTheme.colorScheme.primary
                )
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = {
                    navController.navigate(NavigationRoutes.ADD_TRANSACTION)
                }),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = ContentDescriptions.ADD_BUTTON,
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun NavBarItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
