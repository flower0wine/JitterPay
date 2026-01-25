package com.example.jitterpay.ui.components.goals.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.goals.GoalData
import com.example.jitterpay.ui.goals.GoalIconType
import java.text.NumberFormat
import java.util.Locale

@Composable
fun GoalDetailHeader(
    goal: GoalData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            color = if (goal.isCompleted)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = getGoalIcon(goal.iconType),
                    contentDescription = goal.title,
                    tint = if (goal.isCompleted) Color.Black else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = goal.title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun getGoalIcon(iconType: GoalIconType): ImageVector {
    return when (iconType) {
        GoalIconType.SHIELD -> Icons.Default.Shield
        GoalIconType.FLIGHT -> Icons.Default.Flight
        GoalIconType.LAPTOP -> Icons.Default.Laptop
        GoalIconType.HOME -> Icons.Default.Home
        GoalIconType.CAR -> Icons.Default.DirectionsCar
        GoalIconType.EDUCATION -> Icons.Default.School
        GoalIconType.HEALTH -> Icons.Default.FavoriteBorder
        GoalIconType.GIFT -> Icons.Default.CardGiftcard
    }
}

