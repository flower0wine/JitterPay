package com.example.jitterpay.ui.components.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants

/**
 * Settings item with toggle switch - pure UI component without entrance animation
 * Entrance animation is handled by parent container (SettingsSection)
 *
 * @param icon Item icon
 * @param iconTint Icon color
 * @param iconBackground Icon background color
 * @param title Item title
 * @param isEnabled Toggle state
 * @param onToggleChanged Toggle change handler
 * @param modifier Modifier for the surface
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
        modifier = modifier.fillMaxWidth(),
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
                onCheckedChange = null,
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
 * Warning banner has expand/collapse animation
 *
 * @param icon Item icon
 * @param iconTint Icon color
 * @param iconBackground Icon background color
 * @param title Item title
 * @param isEnabled Toggle state
 * @param onToggleChanged Toggle change handler
 * @param warningMessage Warning text to show when enabled
 * @param warningActionLabel Action button label
 * @param onWarningActionClick Action button click handler
 * @param modifier Modifier for the column
 */
@Composable
fun SettingsItemWithToggleAndWarning(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    isEnabled: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    warningMessage: String? = null,
    warningActionLabel: String? = null,
    onWarningActionClick: (() -> Unit)? = null
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

        // Warning banner with expand/collapse animation
        AnimatedVisibility(
            visible = isEnabled && warningMessage != null,
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
            label = "warningBanner"
        ) {
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
                    val message = warningMessage!!
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )

                    if (warningActionLabel != null && onWarningActionClick != null) {
                        TextButton(
                            onClick = onWarningActionClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
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
