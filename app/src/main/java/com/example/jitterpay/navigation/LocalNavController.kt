package com.example.jitterpay.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * CompositionLocal key for NavController
 * Provides implicit access to NavController throughout the composition
 */
val LocalNavController = compositionLocalOf<NavController> {
    error("No NavController provided. Make sure to wrap your NavHost with ProvideNavController")
}

/**
 * Provides NavController to all children in the composition
 *
 * This allows any descendant composable to access the NavController without
 * explicitly passing it as a parameter.
 *
 * @param navController The NavController instance to provide
 * @param content The composable content that will have access to the NavController
 */
@Composable
fun ProvideNavController(
    navController: NavController,
    content: @Composable (Modifier) -> Unit
) {
    CompositionLocalProvider(LocalNavController provides navController) {
        content(Modifier)
    }
}
