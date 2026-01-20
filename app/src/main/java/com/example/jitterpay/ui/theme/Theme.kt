package com.example.jitterpay.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonLime,
    secondary = Color.White,
    tertiary = NeonLime,
    background = DarkBackground,
    surface = SurfaceDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NeonLime,
    secondary = Color.Black,
    tertiary = NeonLime,
    background = Color.White,
    surface = Color(0xFFF2F2F7),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun JitterPayTheme(
    darkTheme: Boolean = true, // Force dark theme for now as per the design
    dynamicColor: Boolean = false, // Disable dynamic color to maintain design consistency
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
