package com.example.jitterpay.ui.recurring

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jitterpay.ui.components.recurring.RecurringCard
import com.example.jitterpay.ui.components.recurring.RecurringHeader
import com.example.jitterpay.ui.components.recurring.RecurringSummaryCard

@Composable
fun RecurringScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: RecurringViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val hasRecurring = uiState.recurringTransactions.isNotEmpty()
    val activeCount = uiState.recurringTransactions.count { it.isActive }
    
    val activeTransactions = uiState.recurringTransactions.filter { it.isActive }
    val totalMonthlyIncome = activeTransactions
        .filter { it.type == "INCOME" }
        .sumOf { it.estimatedMonthlyAmount }
    val totalMonthlyExpense = activeTransactions
        .filter { it.type == "EXPENSE" }
        .sumOf { it.estimatedMonthlyAmount }
    val netMonthlyAmount = totalMonthlyIncome - totalMonthlyExpense

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            RecurringHeader(
                onBackClick = { navController.navigateUp() }
            )

            if (hasRecurring) {
                Spacer(modifier = Modifier.height(20.dp))
                RecurringSummaryCard(
                    activeCount = activeCount,
                    totalCount = uiState.recurringTransactions.size,
                    monthlyIncome = totalMonthlyIncome,
                    monthlyExpense = totalMonthlyExpense,
                    netMonthlyAmount = netMonthlyAmount
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (hasRecurring) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SCHEDULED TRANSACTIONS",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(
                        onClick = {
                            navController.navigate("add_recurring")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Recurring",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (hasRecurring) {
                uiState.recurringTransactions.forEach { recurring ->
                    RecurringCard(
                        recurring = recurring,
                        onCardClick = {
                            navController.navigate("recurring_detail/${recurring.id}")
                        },
                        onToggleActive = { viewModel.toggleRecurringActive(recurring.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                EmptyRecurringState(
                    onCreateRecurring = {
                        navController.navigate("add_recurring")
                    }
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun EmptyRecurringState(
    onCreateRecurring: () -> Unit,
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
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.4f),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No Recurring Transactions",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Automate your regular expenses like\ncommute, subscriptions, and bills",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onCreateRecurring,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create Recurring Transaction",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
