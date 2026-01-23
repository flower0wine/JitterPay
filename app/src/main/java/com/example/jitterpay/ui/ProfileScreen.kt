package com.example.jitterpay.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.jitterpay.ui.components.profile.*

@Composable
fun ProfileScreen(
    onAddTransactionClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AutoTrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Check permissions when screen recomposes
    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    // Handle intents for permission results
    LaunchedEffect(Unit) {
        // This would be triggered when user returns from settings
        // In a real implementation, you'd use ActivityResultLauncher
    }

    Scaffold(
        bottomBar = {
            // Navigation is now handled by BottomNavBar internally via NavController
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            if (uiState.lastSavedTransaction != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearLastSavedTransaction() }) {
                            Text("Dismiss")
                        }
                    },
                    dismissAction = {
                        IconButton(onClick = { viewModel.clearLastSavedTransaction() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                ) {
                    Text(uiState.lastSavedTransaction ?: "")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Simplified header without back button since it's now a main destination
            ProfileTopBar(
                onBackClick = { /* Back navigation handled by system */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            ProfileHeader(
                userName = "Alex Morgan",
                userEmail = "alex.morgan@flowpay.io",
                isPro = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Auto-tracking section
            SettingsSection(title = "AUTO-TRACKING") {
                SettingsItemWithToggleAndWarning(
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = Color.Black,
                    iconBackground = MaterialTheme.colorScheme.primary,
                    title = "Auto-tracking",
                    isEnabled = uiState.isAutoTrackingEnabled,
                    onToggleChanged = { enabled ->
                        // Allow toggling regardless of permission status
                        viewModel.toggleAutoTracking(enabled)
                    },
                    warningMessage = if (uiState.isAutoTrackingEnabled) {
                        when {
                            !uiState.isAccessibilityEnabled -> "Accessibility service not enabled"
                            !uiState.isOverlayGranted -> "Display over other apps permission not granted"
                            else -> null
                        }
                    } else {
                        null
                    },
                    warningActionLabel = if (uiState.isAutoTrackingEnabled) {
                        when {
                            !uiState.isAccessibilityEnabled -> "Enable"
                            !uiState.isOverlayGranted -> "Grant"
                            else -> null
                        }
                    } else {
                        null
                    },
                    onWarningActionClick = if (uiState.isAutoTrackingEnabled) {
                        when {
                            !uiState.isAccessibilityEnabled -> {
                                {
                                    context.startActivity(
                                        viewModel.openAccessibilitySettings(context)
                                    )
                                }
                            }
                            !uiState.isOverlayGranted -> {
                                {
                                    context.startActivity(
                                        viewModel.openOverlaySettings(context)
                                    )
                                }
                            }
                            else -> null
                        }
                    } else {
                        null
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Permission status indicators
                if (uiState.isAutoTrackingEnabled) {
                    PermissionStatusIndicator(
                        title = "Accessibility Service",
                        isGranted = uiState.isAccessibilityEnabled,
                        onSettingsClick = {
                            context.startActivity(
                                viewModel.openAccessibilitySettings(context)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PermissionStatusIndicator(
                        title = "Display Over Other Apps",
                        isGranted = uiState.isOverlayGranted,
                        onSettingsClick = {
                            context.startActivity(
                                viewModel.openOverlaySettings(context)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

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

/**
 * Permission status indicator component
 */
@Composable
private fun PermissionStatusIndicator(
    title: String,
    isGranted: Boolean,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(16.dp)
            )

            Text(
                text = title,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (!isGranted) {
            TextButton(
                onClick = onSettingsClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Enable",
                    fontSize = 12.sp
                )
            }
        }
    }
}
