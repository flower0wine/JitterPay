package com.example.jitterpay.ui.addtransaction

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.components.addtransaction.AddTransactionHeader
import com.example.jitterpay.ui.components.addtransaction.AmountDisplay
import com.example.jitterpay.ui.components.addtransaction.CategoryGrid
import com.example.jitterpay.ui.components.addtransaction.DateSelector
import com.example.jitterpay.ui.components.addtransaction.NumberPad
import com.example.jitterpay.ui.components.addtransaction.TypeSelector
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddTransactionScreen(
    onClose: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // 处理错误提示
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // 处理保存成功
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onClose()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.SHORT,
                    easing = AnimationConstants.Easing.Entrance
                )
            ) + slideInVertically(
                initialOffsetY = { -it / 4 },
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.MEDIUM,
                    easing = AnimationConstants.Easing.Entrance
                )
            ),
            label = "header"
        ) {
            AddTransactionHeader(
                onClose = onClose,
                onDone = { viewModel.saveTransaction() }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 50,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 50,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ),
                label = "typeSelector"
            ) {
                TypeSelector(
                    selectedType = uiState.selectedType,
                    onTypeSelected = { viewModel.setType(it) }
                )
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 100,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 100,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ),
                label = "amountDisplay"
            ) {
                // 使用格式化后的金额显示
                AmountDisplay(amount = uiState.amount.toBigDecimal())
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 150,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 150,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ),
                label = "dateSelector"
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val dateFormatter = remember { SimpleDateFormat("'Today', MMM dd", Locale.getDefault()) }
                    val displayDate = uiState.selectedDateMillis?.let {
                        dateFormatter.format(Date(it))
                    } ?: dateFormatter.format(Date())

                    DateSelector(
                        date = displayDate,
                        onDateClick = { /* TODO: Implement date picker */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 200,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 200,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ),
                label = "categoryGrid"
            ) {
                CategoryGrid(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.setCategory(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.MEDIUM,
                    delayMillis = 250,
                    easing = AnimationConstants.Easing.Entrance
                )
            ) + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.MEDIUM,
                    delayMillis = 250,
                    easing = AnimationConstants.Easing.Entrance
                )
            ),
            label = "numberPad"
        ) {
            NumberPad(
                inputDisplay = uiState.displayAmount,
                onNumberClick = { digit ->
                    if (digit in "0".."9") {
                        viewModel.onDigitClick(digit)
                    } else if (digit == ".") {
                        viewModel.onDecimalClick()
                    }
                },
                onOperatorClick = { operator ->
                    viewModel.onOperatorClick(operator)
                },
                onBackspace = { viewModel.onBackspaceClick() },
                onConfirm = { viewModel.saveTransaction() }
            )
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}
