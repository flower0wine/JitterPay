package com.example.jitterpay.ui.recurring

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jitterpay.ui.components.recurring.addrecurring.*
import com.example.jitterpay.ui.components.recurring.detail.RecurringDetailHeader
import com.example.jitterpay.ui.components.recurring.detail.RecurringReminderSelector
import com.example.jitterpay.ui.theme.ErrorRed

@Composable
fun RecurringDetailScreen(
    navController: NavController,
    viewModel: RecurringDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navController.navigateUp()
        }
    }

    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            navController.navigateUp()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recurring Transaction?", color = Color.White) },
            text = { Text("This action cannot be undone.", color = Color.Gray) },
            containerColor = MaterialTheme.colorScheme.background,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecurring()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            RecurringDetailHeader(
                onBackClick = { navController.navigateUp() },
                onDoneClick = { viewModel.saveRecurring() }
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Content with animations
                AnimatedContentWrapper(delay = 50) {
                    RecurringTitleInput(
                        title = uiState.title,
                        onTitleChange = { viewModel.setTitle(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContentWrapper(delay = 100) {
                    RecurringAmountInput(
                        amount = uiState.amount,
                        type = uiState.type,
                        onAmountChange = { viewModel.setAmount(it) },
                        onTypeChange = { viewModel.setType(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContentWrapper(delay = 150) {
                    RecurringCategorySelector(
                        selectedCategory = uiState.category,
                        onCategorySelected = { viewModel.setCategory(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContentWrapper(delay = 200) {
                    RecurringFrequencySelector(
                        selectedFrequency = uiState.frequency,
                        onFrequencySelected = { viewModel.setFrequency(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContentWrapper(delay = 250) {
                    RecurringStartDateSelector(
                        startDate = uiState.startDate,
                        onDateSelected = { viewModel.setStartDate(it) }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                AnimatedContentWrapper(delay = 300) {
                    RecurringReminderSelector(
                        enabled = uiState.reminderEnabled,
                        daysBefore = uiState.reminderDaysBefore,
                        onReminderChange = { enabled, days -> viewModel.setReminder(enabled, days) }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Delete Button at bottom
                AnimatedContentWrapper(delay = 350) {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ErrorRed
                        ),
                        border = BorderStroke(1.dp, ErrorRed),
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Delete Recurring Transaction",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ErrorRed
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun AnimatedContentWrapper(
    delay: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 500, delayMillis = delay)
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(durationMillis = 500, delayMillis = delay)
        )
    ) {
        content()
    }
}
