package com.example.jitterpay.ui

import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jitterpay.autotracking.AutoTrackingViewModel
import com.example.jitterpay.autotracking.util.AutoTrackingPermissions
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.components.profile.*

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: AutoTrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isVisible by remember { mutableStateOf(false) }

    // Check permissions when screen recomposes
    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    // Trigger entrance animations after composition
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Handle intents for permission results
    LaunchedEffect(Unit) {
        // This would be triggered when user returns from settings
        // In a real implementation, you'd use ActivityResultLauncher
    }

    Scaffold(
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

            // Auto-tracking section
            SettingsSection(
                title = "AUTO-TRACKING",
                isVisible = isVisible,
                baseDelayMs = 0
            ) {
                AnimatedItem(index = 0) {
                    SettingsItemWithToggle(
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = Color.Black,
                        iconBackground = MaterialTheme.colorScheme.primary,
                        title = "Auto-tracking",
                        isEnabled = uiState.isAutoTrackingEnabled,
                        onToggleChanged = { enabled ->
                            viewModel.toggleAutoTracking(enabled)
                        }
                    )
                }

                // Permission status indicators - shown as sub-items when auto-tracking is enabled
                AnimatedVisibility(
                    visible = uiState.isAutoTrackingEnabled,
                    enter = expandVertically(
                        animationSpec = tween(
                            durationMillis = AnimationConstants.Duration.MEDIUM,
                            easing = AnimationConstants.Easing.Entrance
                        ),
                        expandFrom = Alignment.Top
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = AnimationConstants.Duration.SHORT,
                            easing = AnimationConstants.Easing.Entrance
                        )
                    ),
                    exit = shrinkVertically(
                        animationSpec = tween(
                            durationMillis = AnimationConstants.Duration.SHORT,
                            easing = AnimationConstants.Easing.Exit
                        ),
                        shrinkTowards = Alignment.Top
                    ) + fadeOut(
                        animationSpec = tween(
                            durationMillis = AnimationConstants.Duration.SHORT,
                            easing = AnimationConstants.Easing.Exit
                        )
                    ),
                    label = "permissionIndicators"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // First sub-item with connector
                        SubSettingsItemContainer(
                            position = SubItemPosition.FIRST
                        ) {
                            PermissionSubItem(
                                title = "Accessibility Service",
                                isGranted = uiState.isAccessibilityEnabled,
                                onSettingsClick = {
                                    context.startActivity(
                                        viewModel.openAccessibilitySettings(context)
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Last sub-item with connector
                        SubSettingsItemContainer(
                            position = SubItemPosition.LAST
                        ) {
                            PermissionSubItem(
                                title = "Display Over Other Apps",
                                isGranted = uiState.isOverlayGranted,
                                onSettingsClick = {
                                    context.startActivity(
                                        viewModel.openOverlaySettings(context)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bookkeeping section - starts shortly after auto-tracking items
            SettingsSection(
                title = "BOOKKEEPING",
                isVisible = isVisible,
                baseDelayMs = 200
            ) {
                AnimatedItem(index = 0) {
                    SettingsItem(
                        icon = Icons.Default.AccountBalance,
                        iconTint = Color.Black,
                        iconBackground = MaterialTheme.colorScheme.primary,
                        title = "Budget Settings",
                        onClick = { /* TODO */ }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedItem(index = 1) {
                    SettingsItem(
                        icon = Icons.Default.CurrencyExchange,
                        iconTint = Color.Black,
                        iconBackground = MaterialTheme.colorScheme.primary,
                        title = "Multi-currency",
                        onClick = { /* TODO */ }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedItem(index = 2) {
                    SettingsItem(
                        icon = Icons.Default.Upload,
                        iconTint = Color.Black,
                        iconBackground = MaterialTheme.colorScheme.primary,
                        title = "Data Export",
                        onClick = { /* TODO */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Preferences section - continues the flow
            SettingsSection(
                title = "PREFERENCES",
                isVisible = isVisible,
                baseDelayMs = 350
            ) {
                AnimatedItem(index = 0) {
                    SettingsItem(
                        icon = Icons.Default.DarkMode,
                        iconTint = Color.Black,
                        iconBackground = MaterialTheme.colorScheme.primary,
                        title = "Appearance",
                        trailingText = "Dark",
                        onClick = { /* TODO */ }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedItem(index = 1) {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        iconTint = Color.Black,
                        iconBackground = MaterialTheme.colorScheme.primary,
                        title = "About",
                        onClick = { /* TODO */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            ProfileActions(
                onSwitchAccount = { /* TODO */ },
                onSignOut = { /* TODO */ },
                isVisible = isVisible
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}



