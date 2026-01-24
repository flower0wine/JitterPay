package com.example.jitterpay.ui.components.addtransaction

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun NumberPad(
    inputDisplay: String? = null,
    onNumberClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.MEDIUM,
                delayMillis = 100,
                easing = AnimationConstants.Easing.Entrance
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.SHORT,
                delayMillis = 100,
                easing = AnimationConstants.Easing.Entrance
            )
        ),
        label = "numberPad"
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 显示用户输入
            if (!inputDisplay.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = inputDisplay,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            // Row 1: 1, 2, 3, backspace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NumberButton("1", onNumberClick, Modifier.weight(1f))
                NumberButton("2", onNumberClick, Modifier.weight(1f))
                NumberButton("3", onNumberClick, Modifier.weight(1f))
                IconButton(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    onClick = onBackspace,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: 4, 5, 6, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NumberButton("4", onNumberClick, Modifier.weight(1f))
                NumberButton("5", onNumberClick, Modifier.weight(1f))
                NumberButton("6", onNumberClick, Modifier.weight(1f))
                OperatorButton("+", { onOperatorClick("+") }, Modifier.weight(1f))
            }

            // Row 3: 7, 8, 9, -
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NumberButton("7", onNumberClick, Modifier.weight(1f))
                NumberButton("8", onNumberClick, Modifier.weight(1f))
                NumberButton("9", onNumberClick, Modifier.weight(1f))
                OperatorButton("-", { onOperatorClick("-") }, Modifier.weight(1f))
            }

            // Row 4: ., 0, confirm button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                NumberButton(".", onNumberClick, Modifier.weight(1f))
                NumberButton("0", onNumberClick, Modifier.weight(1f))
                ConfirmButton(
                    onClick = onConfirm,
                    modifier = Modifier.weight(2f)
                )
            }
        }
    }
}

@Composable
private fun NumberButton(
    number: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1.4f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(number) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun OperatorButton(
    operator: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1.4f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(operator) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = operator,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun IconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1.4f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ConfirmButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(2.5f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Confirm",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
    }
}
