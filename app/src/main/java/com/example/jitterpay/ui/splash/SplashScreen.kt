package com.example.jitterpay.ui.splash

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.example.jitterpay.ui.animation.AnimationConstants
import kotlinx.coroutines.delay

private const val TAG = "SplashScreen"

/**
 * Splash screen composable that displays a Lottie animation
 * @param preloadedComposition Pre-loaded Lottie composition for immediate display
 */
@Composable
fun SplashScreen(
    preloadedComposition: LottieComposition?,
    timeoutMillis: Long = AnimationConstants.Duration.EXTRA_LONG * 4L,
    onAnimationComplete: () -> Unit
) {
    var hasTimedOut by remember { mutableStateOf(false) }

    // 使用预加载的 composition
    val composition = preloadedComposition

    Log.d(TAG, "Displaying animation with composition: ${composition != null}")

    // 跟踪动画进度
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1 // 只播放一次
    )

    // 完成时回调
    LaunchedEffect(progress, hasTimedOut) {
        if ((progress >= 1f) || hasTimedOut) {
            Log.d(TAG, "Animation complete or timeout, hiding splash")
            onAnimationComplete()
        }
    }

    // 超时处理
    LaunchedEffect(Unit) {
        delay(timeoutMillis)
        Log.d(TAG, "Timeout reached, hiding splash")
        hasTimedOut = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
            )
        }
    }
}
