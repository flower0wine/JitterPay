package com.example.jitterpay.ui.recurring

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.components.recurring.addrecurring.*
import com.example.jitterpay.ui.components.recurring.detail.RecurringReminderSelector

@Composable
fun AddRecurringScreen(
    onClose: () -> Unit,
    viewModel: AddRecurringViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Handle save success
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
            AddRecurringHeader(
                onClose = onClose,
                onDone = { viewModel.saveRecurring() }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

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
                label = "titleInput"
            ) {
                RecurringTitleInput(
                    title = uiState.title,
                    onTitleChange = { viewModel.setTitle(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 100,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 100,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ),
                label = "amountInput"
            ) {
                RecurringAmountInput(
                    amount = uiState.amount,
                    type = uiState.type,
                    onAmountChange = { viewModel.setAmount(it) },
                    onTypeChange = { viewModel.setType(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                label = "categorySelector"
            ) {
                RecurringCategorySelector(
                    selectedCategory = uiState.category,
                    onCategorySelected = { viewModel.setCategory(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                label = "frequencySelector"
            ) {
                RecurringFrequencySelector(
                    selectedFrequency = uiState.frequency,
                    onFrequencySelected = { viewModel.setFrequency(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 250,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 250,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ),
                label = "startDateSelector"
            ) {
                RecurringStartDateSelector(
                    startDate = uiState.startDate,
                    onDateSelected = { viewModel.setStartDate(it) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 300,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 300,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ),
                label = "reminderSelector"
            ) {
                RecurringReminderSelector(
                    enabled = uiState.reminderEnabled,
                    daysBefore = uiState.reminderDaysBefore,
                    onReminderChange = { enabled, days -> viewModel.setReminder(enabled, days) }
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
