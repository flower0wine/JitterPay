package com.example.jitterpay.ui.components.addtransaction

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants

@Composable
fun NumberPad(
    modifier: Modifier = Modifier,
    inputDisplay: String? = null,
    onNumberClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit,
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
            // 显示用户输入 - 带动画效果
            AnimatedVisibility(
                visible = !inputDisplay.isNullOrEmpty(),
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.SHORT,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ) + expandVertically(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.SHORT,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ),
                exit = fadeOut(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.SHORT,
                        easing = AnimationConstants.Easing.Exit
                    )
                ) + shrinkVertically(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.SHORT,
                        easing = AnimationConstants.Easing.Exit
                    )
                ),
                label = "inputDisplayVisibility"
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    AnimatedContent(
                        targetState = inputDisplay ?: "",
                        transitionSpec = {
                            // 新内容从右侧滑入并放大，旧内容淡出
                            (slideInHorizontally(
                                initialOffsetX = { it / 4 },
                                animationSpec = tween(
                                    durationMillis = AnimationConstants.Duration.MICRO,
                                    easing = AnimationConstants.Easing.Entrance
                                )
                            ) + fadeIn(
                                animationSpec = tween(
                                    durationMillis = AnimationConstants.Duration.MICRO,
                                    easing = AnimationConstants.Easing.Entrance
                                )
                            ) + scaleIn(
                                initialScale = 1.1f,
                                animationSpec = tween(
                                    durationMillis = AnimationConstants.Duration.MICRO,
                                    easing = AnimationConstants.Easing.Entrance
                                )
                            )).togetherWith(
                                fadeOut(
                                    animationSpec = tween(
                                        durationMillis = AnimationConstants.Duration.MICRO,
                                        easing = AnimationConstants.Easing.Exit
                                    )
                                )
                            )
                        },
                        label = "inputDisplayContent"
                    ) { displayText ->
                        Text(
                            text = displayText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
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
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.MICRO,
            easing = AnimationConstants.Easing.Standard
        ),
        label = "numberButtonScale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1.4f)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick(number)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun OperatorButton(
    operator: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.MICRO,
            easing = AnimationConstants.Easing.Standard
        ),
        label = "operatorButtonScale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1.4f)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick(operator)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = operator,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun IconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.MICRO,
            easing = AnimationConstants.Easing.Standard
        ),
        label = "iconButtonScale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1.4f)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun ConfirmButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.MICRO,
            easing = AnimationConstants.Easing.Standard
        ),
        label = "confirmButtonScale"
    )

    Box(
        modifier = modifier
            .aspectRatio(2.5f)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Confirm",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}
