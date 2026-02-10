package com.example.jitterpay.ui.selectbudget

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.components.selectbudget.BudgetSelectionCard
import com.example.jitterpay.ui.components.selectbudget.EmptyBudgetState
import com.example.jitterpay.ui.components.selectbudget.SelectBudgetHeader

@Composable
fun SelectBudgetScreen(
    transactionId: Long? = null,
    originalBudgetId: Long? = null,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    viewModel: SelectBudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadBudgets(transactionId, originalBudgetId)
        isVisible = true
    }

    // 处理完成事件
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onComplete()
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
            SelectBudgetHeader(
                onBack = onBack,
                onConfirm = {
                    viewModel.confirmSelection()
                }
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                    label = "description"
                ) {
                    Column {
                        Text(
                            text = "Link to Budget",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Choose a budget to track this expense against, or skip to add without linking.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = AnimationConstants.Duration.MEDIUM,
                            delayMillis = 100,
                            easing = AnimationConstants.Easing.Entrance
                        )
                    ) + slideInHorizontally(
                        initialOffsetX = { it / 3 },
                        animationSpec = tween(
                            durationMillis = AnimationConstants.Duration.MEDIUM,
                            delayMillis = 100,
                            easing = AnimationConstants.Easing.Entrance
                        )
                    ),
                    label = "noBudgetOption"
                ) {
                    Column {
                        BudgetSelectionCard(
                            budget = null,
                            isSelected = uiState.selectedBudgetId == null,
                            onSelect = { viewModel.selectBudget(null) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                if (uiState.budgets.isNotEmpty()) {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = AnimationConstants.Duration.MEDIUM,
                                delayMillis = 150,
                                easing = AnimationConstants.Easing.Entrance
                            )
                        ),
                        label = "budgetsLabel"
                    ) {
                        Column {
                            Text(
                                text = "YOUR BUDGETS",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    uiState.budgets.forEachIndexed { index, budget ->
                        var itemVisible by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay((200 + index * 50).toLong())
                            itemVisible = true
                        }

                        AnimatedVisibility(
                            visible = itemVisible,
                            enter = fadeIn(
                                animationSpec = tween(
                                    durationMillis = AnimationConstants.Duration.MEDIUM,
                                    easing = AnimationConstants.Easing.Entrance
                                )
                            ) + slideInHorizontally(
                                initialOffsetX = { it / 3 },
                                animationSpec = tween(
                                    durationMillis = AnimationConstants.Duration.MEDIUM,
                                    easing = AnimationConstants.Easing.Entrance
                                )
                            ),
                            label = "budgetItem_$index"
                        ) {
                            Column {
                                BudgetSelectionCard(
                                    budget = budget,
                                    isSelected = uiState.selectedBudgetId == budget.id,
                                    onSelect = { viewModel.selectBudget(budget.id) }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                } else {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = AnimationConstants.Duration.MEDIUM,
                                delayMillis = 200,
                                easing = AnimationConstants.Easing.Entrance
                            )
                        ) + scaleIn(
                            initialScale = 0.9f,
                            animationSpec = tween(
                                durationMillis = AnimationConstants.Duration.MEDIUM,
                                delayMillis = 200,
                                easing = AnimationConstants.Easing.Entrance
                            )
                        ),
                        label = "emptyState"
                    ) {
                        EmptyBudgetState()
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
