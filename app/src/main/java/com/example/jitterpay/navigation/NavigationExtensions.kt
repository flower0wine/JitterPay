package com.example.jitterpay.navigation

import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry
import com.example.jitterpay.constants.NavigationRoutes
import com.example.jitterpay.ui.animation.SlideTransitions

/**
 * Order of bottom navigation tabs for determining slide direction
 */
private val BOTTOM_NAV_ORDER = listOf(
    NavigationRoutes.HOME,
    NavigationRoutes.STATS,
    NavigationRoutes.GOALS,
    NavigationRoutes.PROFILE
)

/**
 * Get index of a bottom navigation route
 * Returns -1 if route is not a bottom nav item
 */
fun getBottomNavIndex(route: String?): Int {
    val index = if (route == null) -1 else BOTTOM_NAV_ORDER.indexOf(route)
    return index
}

/**
 * Determine slide direction between two bottom navigation routes
 * @return true if should slide in from right, false if from left
 */
fun shouldSlideInRight(currentRoute: String?, targetRoute: String?): Boolean {
    val currentIndex = getBottomNavIndex(currentRoute)
    val targetIndex = getBottomNavIndex(targetRoute)

    // If either route is not a bottom nav item, default to slide from right
    if (currentIndex == -1 || targetIndex == -1) {
        return true
    }

    // Target is to the right of current -> slide from right
    // Target is to the left of current -> slide from left
    return targetIndex > currentIndex
}

/**
 * Get enter transition for bottom navigation based on route order
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.getBottomNavEnterTransition(): EnterTransition {
    val currentRoute = initialState.destination.route
    val targetRoute = targetState.destination.route

    // If current route is null (initial navigation), no animation needed
    if (currentRoute == null) {
        return EnterTransition.None
    }

    val transition = if (shouldSlideInRight(currentRoute, targetRoute)) {
        SlideTransitions.slideInRight()
    } else {
        SlideTransitions.slideInLeft()
    }
    return transition
}

/**
 * Get exit transition for bottom navigation based on route order
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.getBottomNavExitTransition(): ExitTransition {
    val currentRoute = initialState.destination.route
    val targetRoute = targetState.destination.route

    // If current route is null (initial navigation), no animation needed
    if (currentRoute == null) {
        return ExitTransition.None
    }

    // If sliding in from right, current should exit to left
    // If sliding in from left, current should exit to right
    val transition = if (shouldSlideInRight(currentRoute, targetRoute)) {
        SlideTransitions.slideOutLeft()
    } else {
        SlideTransitions.slideOutRight()
    }
    return transition
}

/**
 * Get pop enter transition for bottom navigation
 * This is reverse of enter transition (back navigation)
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.getBottomNavPopEnterTransition(): EnterTransition {
    val currentRoute = initialState.destination.route
    val targetRoute = targetState.destination.route

    val transition = if (shouldSlideInRight(currentRoute, targetRoute)) {
        SlideTransitions.slideInLeft()
    } else {
        SlideTransitions.slideInRight()
    }
    return transition
}

/**
 * Get pop exit transition for bottom navigation
 * This is the reverse of exit transition (back navigation)
 */
fun AnimatedContentTransitionScope<NavBackStackEntry>.getBottomNavPopExitTransition(): ExitTransition {
    val currentRoute = initialState.destination.route
    val targetRoute = targetState.destination.route

    val transition = if (shouldSlideInRight(currentRoute, targetRoute)) {
        SlideTransitions.slideOutRight()
    } else {
        SlideTransitions.slideOutLeft()
    }
    return transition
}
