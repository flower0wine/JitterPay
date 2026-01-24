package com.example.jitterpay.ui.components.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants

@Composable
fun QuickActions(
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionButton(
            icon = Icons.Default.Send,
            label = "SEND",
            isVisible = isVisible,
            delayMillis = AnimationConstants.Stagger.QUICK_ACTIONS[0]
        )
        QuickActionButton(
            icon = Icons.Default.AccountBalance,
            label = "BANK",
            isVisible = isVisible,
            delayMillis = AnimationConstants.Stagger.QUICK_ACTIONS[1]
        )
        QuickActionButton(
            icon = Icons.Default.Receipt,
            label = "BILLS",
            isVisible = isVisible,
            delayMillis = AnimationConstants.Stagger.QUICK_ACTIONS[2]
        )
        QuickActionButton(
            icon = Icons.Default.MoreHoriz,
            label = "MORE",
            isVisible = isVisible,
            delayMillis = AnimationConstants.Stagger.QUICK_ACTIONS[3]
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    isVisible: Boolean,
    delayMillis: Int
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM,
                delayMillis = delayMillis,
                easing = AnimationConstants.Easing.Entrance
            ),
            initialScale = 0.8f
        ) + slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM,
                delayMillis = delayMillis,
                easing = AnimationConstants.Easing.Entrance
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM,
                delayMillis = delayMillis
            )
        ),
        label = "quickAction_$label"
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Surface(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape),
            color = Color(0xFF1C1C1E)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (label == "SEND") MaterialTheme.colorScheme.primary else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
    }
}

