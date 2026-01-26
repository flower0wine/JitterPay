package com.example.jitterpay.ui.components.addtransaction

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.ui.animation.AnimationConstants

@Composable
fun TypeSelector(
    selectedType: TransactionType = TransactionType.EXPENSE,
    onTypeSelected: (TransactionType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.SHORT,
                delayMillis = 100,
                easing = AnimationConstants.Easing.Entrance
            )
        ) + scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.SHORT,
                delayMillis = 100,
                easing = AnimationConstants.Easing.Entrance
            )
        ),
        label = "typeSelector"
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1C1C1E)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TypeButton(
                text = "Expense",
                isSelected = selectedType == TransactionType.EXPENSE,
                onClick = { onTypeSelected(TransactionType.EXPENSE) },
                modifier = Modifier.weight(1f)
            )

            TypeButton(
                text = "Income",
                isSelected = selectedType == TransactionType.INCOME,
                onClick = { onTypeSelected(TransactionType.INCOME) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 选中状态的缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.95f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.SHORT,
            easing = AnimationConstants.Easing.Standard
        ),
        label = "typeButtonScale"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(4.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.SHORT,
                    easing = AnimationConstants.Easing.Standard
                )
            )
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
