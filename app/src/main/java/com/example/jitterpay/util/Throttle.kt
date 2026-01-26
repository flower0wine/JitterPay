package com.example.jitterpay.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Remembers a throttled click handler that prevents rapid repeated clicks.
 *
 * @param onClick The original click handler to be throttled
 * @param throttleIntervalMs Minimum time interval between clicks in milliseconds (default: 500ms)
 * @return A throttled click handler
 */
@Composable
fun rememberThrottledClick(
    onClick: () -> Unit,
    throttleIntervalMs: Long = 500
): () -> Unit {
    var lastClickTime by remember { mutableLongStateOf(0L) }

    return {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= throttleIntervalMs) {
            lastClickTime = currentTime
            onClick()
        }
    }
}
