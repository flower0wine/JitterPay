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
 * Settings section container that manages entrance animations for its children
 * 
 * This component handles all animation logic, allowing child components to be pure UI.
 * Children are automatically animated with staggered delays based on their index.
 *
 * @param title Section title displayed above items
 * @param modifier Modifier for the section container
 * @param isVisible Controls visibility for entrance animation
 * @param baseDelayMs Base delay before section starts animating
 * @param content Settings items as column content - will be wrapped with AnimatedVisibility
 */
@Composable
fun SettingsSection(
    modifier: Modifier = Modifier,
    title: String,
    isVisible: Boolean = true,
    baseDelayMs: Int = 0,
    content: @Composable AnimatedSectionScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Section title animation
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.SHORT,
                    delayMillis = baseDelayMs,
                    easing = AnimationConstants.Easing.Entrance
                )
            ) + slideInVertically(
                initialOffsetY = { -20 },
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.MEDIUM,
                    delayMillis = baseDelayMs,
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

        // Content with animation scope
        val scope = AnimatedSectionScopeImpl(
            isVisible = isVisible,
            baseDelayMs = baseDelayMs + 100 // Items start 100ms after title
        )
        scope.content()
    }
}

/**
 * Scope for animated section content
 * Provides helper functions to wrap children with animations
 */
interface AnimatedSectionScope {
    /**
     * Wrap a composable with entrance animation
     * Automatically calculates staggered delay based on item index
     */
    @Composable
    fun AnimatedItem(
        index: Int,
        content: @Composable () -> Unit
    )

    /**
     * Wrap a composable with custom entrance animation delay
     */
    @Composable
    fun AnimatedItemWithDelay(
        delayMs: Int,
        content: @Composable () -> Unit
    )
}

/**
 * Implementation of AnimatedSectionScope
 */
private class AnimatedSectionScopeImpl(
    private val isVisible: Boolean,
    private val baseDelayMs: Int
) : AnimatedSectionScope {

    @Composable
    override fun AnimatedItem(
        index: Int,
        content: @Composable () -> Unit
    ) {
        val itemDelay = baseDelayMs + (index * AnimationConstants.Stagger.LIST_ITEM_BASE)
        AnimatedItemWithDelay(delayMs = itemDelay, content = content)
    }

    @Composable
    override fun AnimatedItemWithDelay(
        delayMs: Int,
        content: @Composable () -> Unit
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.SHORT,
                    delayMillis = delayMs,
                    easing = AnimationConstants.Easing.Entrance
                )
            ) + slideInHorizontally(
                initialOffsetX = { it / 3 },
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.MEDIUM,
                    delayMillis = delayMs,
                    easing = AnimationConstants.Easing.Entrance
                )
            ),
            label = "settingsItem"
        ) {
            content()
        }
    }
}

