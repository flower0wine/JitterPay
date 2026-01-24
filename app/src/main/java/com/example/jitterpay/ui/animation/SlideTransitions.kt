package com.example.jitterpay.ui.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * Slide transition animations for page navigation
 */
object SlideTransitions {

    /**
     * Slide in from the right side (standard push animation)
     * Used when navigating forward or opening a new screen
     */
    fun slideInRight(
        durationMillis: Int = AnimationConstants.Duration.MEDIUM
    ): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { it }, // Start from the right edge
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = AnimationConstants.Easing.Entrance
            )
        )
    }

    /**
     * Slide out to the left side (standard push animation)
     * Used when navigating forward (current page exits)
     */
    fun slideOutLeft(
        durationMillis: Int = AnimationConstants.Duration.MEDIUM
    ): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { -it }, // Exit to the left edge
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = AnimationConstants.Easing.Exit
            )
        )
    }

    /**
     * Slide in from the left side (back navigation animation)
     * Used when navigating back or going to a "previous" tab
     */
    fun slideInLeft(
        durationMillis: Int = AnimationConstants.Duration.MEDIUM
    ): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { -it }, // Start from the left edge
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = AnimationConstants.Easing.Entrance
            )
        )
    }

    /**
     * Slide out to the right side (back navigation animation)
     * Used when navigating back (current page exits to the right)
     */
    fun slideOutRight(
        durationMillis: Int = AnimationConstants.Duration.MEDIUM
    ): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { it }, // Exit to the right edge
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = AnimationConstants.Easing.Exit
            )
        )
    }
}
