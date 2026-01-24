package com.example.jitterpay.ui.animation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/**
 * Animation constants for consistent motion design across the JitterPay app
 * Based on Material Design 3 motion principles
 */
object AnimationConstants {

    // Animation durations in milliseconds
    object Duration {
        // Micro-interactions (button clicks, toggles)
        const val MICRO = 150

        // Quick component animations (small elements)
        const val SHORT = 250

        // Standard component animations (cards, buttons)
        const val MEDIUM = 300

        // Large component animations (page sections)
        const val LONG = 400

        // Complex transitions (modals, bottom sheets)
        const val EXTRA_LONG = 500

        // Value counting animations (numbers, charts)
        const val COUNTING = 1000
    }

    // Easing curves for natural motion
    object Easing {
        // Fast out, slow in - ideal for entrances
        // Element starts quickly and slows to a smooth stop
        val Entrance = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)

        // Slow out, fast in - ideal for exits
        // Element accelerates away smoothly
        val Exit = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)

        // Standard motion curve - balanced and predictable
        val Standard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

        // Emphasized - for important or prominent elements
        val Emphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

        // Emelerated - slow start, gentle finish
        val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

        // Accelerated - quick start, dramatic finish
        val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

        // Linear - for micro-interactions where timing matters more than feel
        val Linear = CubicBezierEasing(0.0f, 0.0f, 1.0f, 1.0f)
    }

    // Stagger timing patterns for sequential animations
    object Stagger {
        // Quick action button delays (4 buttons: Send, Bank, Bills, More)
        val QUICK_ACTIONS = intArrayOf(0, 50, 100, 150)

        // List item delay calculation (capped to prevent excessive wait times)
        const val LIST_ITEM_BASE = 30
        const val LIST_ITEM_MAX = 300

        /**
         * Calculate stagger delay for list items based on index
         * @param index Item position in the list
         * @return Delay in milliseconds, capped at LIST_ITEM_MAX
         */
        fun listItemDelay(index: Int): Int {
            return (index * LIST_ITEM_BASE).coerceAtMost(LIST_ITEM_MAX)
        }

        // Grid item delay calculation for 2D layouts
        /**
         * Calculate stagger delay for grid items
         * @param row Row index
         * @param col Column index
         * @return Delay in milliseconds
         */
        fun gridItemDelay(row: Int, col: Int): Int {
            return (row * 100) + (col * 40)
        }

        // Sequential animation delays
        val SEQUENCE = intArrayOf(0, 100, 200, 350, 500)
    }
}
