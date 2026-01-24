package com.example.jitterpay.ui.components.statistics

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants

enum class TimePeriod {
    WEEKLY, MONTHLY, YEARLY
}

@Composable
fun PeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.SHORT,
                easing = AnimationConstants.Easing.Entrance
            )
        ) + slideInHorizontally(
            initialOffsetX = { it / 4 },
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.SHORT,
                delayMillis = 50,
                easing = AnimationConstants.Easing.Entrance
            )
        ),
        label = "periodSelector"
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PeriodButton(
                text = "Weekly",
                isSelected = selectedPeriod == TimePeriod.WEEKLY,
                onClick = { onPeriodSelected(TimePeriod.WEEKLY) },
                modifier = Modifier.weight(1f)
            )

            PeriodButton(
                text = "MONTHLY",
                isSelected = selectedPeriod == TimePeriod.MONTHLY,
                onClick = { onPeriodSelected(TimePeriod.MONTHLY) },
                modifier = Modifier.weight(1f)
            )

            PeriodButton(
                text = "Yearly",
                isSelected = selectedPeriod == TimePeriod.YEARLY,
                onClick = { onPeriodSelected(TimePeriod.YEARLY) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PeriodButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.Black else Color.Gray
        )
    }
}
