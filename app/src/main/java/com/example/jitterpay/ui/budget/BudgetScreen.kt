package com.example.jitterpay.ui.budget

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jitterpay.constants.NavigationRoutes
import com.example.jitterpay.navigation.LocalNavController
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.components.budget.BudgetCard
import com.example.jitterpay.ui.components.budget.BudgetHeader
import com.example.jitterpay.ui.components.budget.BudgetSummaryCard

@Composable
fun BudgetScreen(
    modifier: Modifier = Modifier,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val uiState by viewModel.uiState.collectAsState()

    val hasBudgets = uiState.budgets.isNotEmpty()
    val totalBudget = uiState.budgets.sumOf { it.amount }
    val totalSpent = uiState.budgets.sumOf { it.spentAmount }
    val activeBudgets = uiState.budgets.count { it.isActive }

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            BudgetHeader(
                onAddClick = {
                    navController.navigate(NavigationRoutes.ADD_BUDGET)
                }
            )

            if (hasBudgets) {
                Spacer(modifier = Modifier.height(8.dp))
                BudgetSummaryCard(
                    totalBudget = totalBudget,
                    totalSpent = totalSpent,
                    activeBudgets = activeBudgets
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (hasBudgets) {
                Text(
                    text = "YOUR BUDGETS",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                uiState.budgets.forEachIndexed { index, budget ->
                    var itemVisible by remember { mutableStateOf(false) }
                    
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay((index * 50).toLong())
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
                            BudgetCard(
                                budget = budget,
                                onCardClick = {
                                    navController.navigate(NavigationRoutes.editBudget(budget.id))
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            } else {
                EmptyBudgetState(
                    onCreateBudget = {
                        navController.navigate(NavigationRoutes.ADD_BUDGET)
                    }
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun EmptyBudgetState(
    onCreateBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(600)) + scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.4f),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No Budgets Yet",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Set spending limits to track your\nexpenses and stay on budget",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onCreateBudget,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Create Your First Budget",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }
    }
}
