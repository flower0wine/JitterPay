package com.example.jitterpay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.constants.ContentDescriptions
import com.example.jitterpay.constants.NavigationTabs

@Composable
fun BottomNavBar(
    selectedTab: String = NavigationTabs.HOME,
    onTabSelected: (String) -> Unit = {},
    onAddClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Default.Home,
                label = NavigationTabs.HOME,
                isSelected = selectedTab == NavigationTabs.HOME,
                onClick = { onTabSelected(NavigationTabs.HOME) }
            )
            NavBarItem(
                icon = Icons.Default.BarChart,
                label = NavigationTabs.STATS,
                isSelected = selectedTab == NavigationTabs.STATS,
                onClick = { onTabSelected(NavigationTabs.STATS) }
            )
            
            // Spacer for the center button
            Spacer(modifier = Modifier.width(60.dp))
            
            NavBarItem(
                icon = Icons.Default.AccountBalanceWallet,
                label = NavigationTabs.WALLET,
                isSelected = selectedTab == NavigationTabs.WALLET,
                onClick = { onTabSelected(NavigationTabs.WALLET) }
            )
            NavBarItem(
                icon = Icons.Default.Person,
                label = NavigationTabs.PROFILE,
                isSelected = selectedTab == NavigationTabs.PROFILE,
                onClick = { onTabSelected(NavigationTabs.PROFILE) }
            )
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
                .clickable(onClick = onAddClick),
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
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable(onClick = onClick)
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
