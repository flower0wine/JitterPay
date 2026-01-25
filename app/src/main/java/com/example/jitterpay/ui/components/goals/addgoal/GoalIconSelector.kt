package com.example.jitterpay.ui.components.goals.addgoal

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.jitterpay.ui.goals.GoalIconType
import com.example.jitterpay.ui.theme.GrayText
import com.example.jitterpay.ui.theme.SurfaceDark

@Composable
fun GoalIconSelector(
    selectedIcon: GoalIconType?,
    onIconSelected: (GoalIconType) -> Unit,
    modifier: Modifier = Modifier
) {
    val icons = listOf(
        GoalIconType.SHIELD to Icons.Default.Shield,
        GoalIconType.FLIGHT to Icons.Default.Flight,
        GoalIconType.LAPTOP to Icons.Default.Laptop,
        GoalIconType.HOME to Icons.Default.Home,
        GoalIconType.CAR to Icons.Default.DirectionsCar,
        GoalIconType.EDUCATION to Icons.Default.School,
        GoalIconType.HEALTH to Icons.Default.FavoriteBorder,
        GoalIconType.GIFT to Icons.Default.CardGiftcard
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "CHOOSE ICON",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = GrayText,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icons.chunked(4).forEachIndexed { rowIndex, rowIcons ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowIcons.forEachIndexed { colIndex, (iconType, icon) ->
                        val itemIndex = rowIndex * 4 + colIndex
                        Box(modifier = Modifier.weight(1f)) {
                            IconItem(
                                icon = icon,
                                iconType = iconType,
                                isSelected = selectedIcon == iconType,
                                onClick = { onIconSelected(iconType) },
                                itemIndex = itemIndex
                            )
                        }
                    }

                    // Fill remaining space if row is not complete
                    repeat(4 - rowIcons.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun IconItem(
    icon: ImageVector,
    iconType: GoalIconType,
    isSelected: Boolean,
    onClick: () -> Unit,
    itemIndex: Int
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val staggerDelay = AnimationConstants.Stagger.gridItemDelay(itemIndex / 4, itemIndex % 4)

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.SHORT,
                delayMillis = 250 + staggerDelay,
                easing = AnimationConstants.Easing.Entrance
            )
        ) + scaleIn(
            initialScale = 0.7f,
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM,
                delayMillis = 250 + staggerDelay,
                easing = AnimationConstants.Easing.Entrance
            )
        ),
        label = "iconItem_$itemIndex"
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else SurfaceDark)
                .border(
                    width = if (isSelected) 0.dp else 1.dp,
                    color = if (isSelected) Color.Transparent else Color(0xFF2C2C2E),
                    shape = CircleShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconType.name,
                tint = if (isSelected) Color.Black else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
