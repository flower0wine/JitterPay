package com.example.jitterpay.ui.components.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants

/**
 * Settings section container with optional entrance animation for title
 *
 * @param title Section title displayed above items
 * @param modifier Modifier for the section container
 * @param animationDelayMs Delay in milliseconds before title animation starts (for staggered effects)
 * @param content Settings items as column content
 */
@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    animationDelayMs: Int = 0,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.SHORT,
                    delayMillis = animationDelayMs,
                    easing = AnimationConstants.Easing.Entrance
                )
            ) + slideInVertically(
                initialOffsetY = { -20 },
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.MEDIUM,
                    delayMillis = animationDelayMs,
                    easing = AnimationConstants.Easing.Entrance
                )
            ),
            label = "settingsSectionTitle"
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )
        }

        content()
    }
}

