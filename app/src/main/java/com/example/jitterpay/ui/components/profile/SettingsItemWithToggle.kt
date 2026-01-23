package com.example.jitterpay.ui.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Settings item with toggle switch
 * The entire item is clickable for better usability
 */
@Composable
fun SettingsItemWithToggle(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    isEnabled: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        onClick = { onToggleChanged(!isEnabled) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBackground, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )

            // Toggle switch
            Switch(
                checked = isEnabled,
                onCheckedChange = null, // Handled by parent click
                colors = SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    checkedThumbColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

/**
 * Settings item with toggle and optional warning banner
 */
@Composable
fun SettingsItemWithToggleAndWarning(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    isEnabled: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    warningMessage: String? = null,
    warningActionLabel: String? = null,
    onWarningActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsItemWithToggle(
            icon = icon,
            iconTint = iconTint,
            iconBackground = iconBackground,
            title = title,
            isEnabled = isEnabled,
            onToggleChanged = onToggleChanged
        )

        // Warning banner if toggle is enabled and warning message is provided
        if (isEnabled && warningMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = warningMessage,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )

                    if (warningActionLabel != null && onWarningActionClick != null) {
                        TextButton(onClick = onWarningActionClick) {
                            Text(
                                text = warningActionLabel,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
