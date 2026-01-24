package com.example.jitterpay.ui.components.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
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
import com.example.jitterpay.ui.animation.AnimationConstants

/**
 * Settings item with toggle switch
 * The entire item is clickable for better usability
 *
 * @param icon Item icon
 * @param iconTint Icon color
 * @param iconBackground Icon background color
 * @param title Item title
 * @param isEnabled Toggle state
 * @param onToggleChanged Toggle change handler
 * @param modifier Modifier for the surface
 * @param animationDelayMs Delay for entrance animation (staggered list effect)
 */
@Composable
fun SettingsItemWithToggle(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    isEnabled: Boolean,
    onToggleChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    animationDelayMs: Int = 0
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.SHORT,
                delayMillis = animationDelayMs,
                easing = AnimationConstants.Easing.Entrance
            )
        ) + slideInHorizontally(
            initialOffsetX = { it / 3 },
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM,
                delayMillis = animationDelayMs,
                easing = AnimationConstants.Easing.Entrance
            )
        ),
        label = "settingsItemWithToggle"
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

                // Toggle switch with spring animation for smooth color transition
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
}

/**
 * Settings item with toggle and optional warning banner with expand/collapse animation
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
 * @param animationDelayMs Delay for entrance animation (staggered list effect)
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
    modifier: Modifier = Modifier,
    animationDelayMs: Int = 0
) {
    Column(modifier = modifier) {
        SettingsItemWithToggle(
            icon = icon,
            iconTint = iconTint,
            iconBackground = iconBackground,
            title = title,
            isEnabled = isEnabled,
            onToggleChanged = onToggleChanged,
            animationDelayMs = animationDelayMs
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
                    // warningMessage is guaranteed non-null in this AnimatedVisibility scope
                    val message = warningMessage!!
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )

                    // Show action button - use requireNotNull for safety
                    if (warningActionLabel != null && onWarningActionClick != null) {
                        val label = warningActionLabel!!
                        val click = onWarningActionClick!!

                        TextButton(
                            onClick = click,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = label,
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
