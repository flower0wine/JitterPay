package com.example.jitterpay.ui.components.goals

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants

@Composable
fun GoalsHeader(
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM
            )
        ) + slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM,
                easing = AnimationConstants.Easing.Entrance
            )
        ),
        label = "goalsHeader"
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Goals",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track your savings and achieve your dreams",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
