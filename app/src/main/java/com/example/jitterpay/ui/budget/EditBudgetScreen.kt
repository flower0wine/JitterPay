package com.example.jitterpay.ui.budget

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jitterpay.data.local.entity.BudgetPeriodType
import com.example.jitterpay.navigation.LocalNavController
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.components.budget.BudgetAmountInput
import com.example.jitterpay.ui.components.budget.BudgetNotificationSettings
import com.example.jitterpay.ui.components.budget.BudgetPeriodSelector
import com.example.jitterpay.ui.components.budget.BudgetTitleInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBudgetScreen(
    budgetId: Long,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val uiState by viewModel.uiState.collectAsState()

    val budget = uiState.budgets.find { it.id == budgetId }

    var title by remember { mutableStateOf(budget?.title ?: "") }
    var amount by remember { mutableStateOf(budget?.amount?.toString() ?: "") }
    var selectedPeriod by remember { mutableStateOf(budget?.periodType ?: BudgetPeriodType.MONTHLY) }
    var notifyAt80 by remember { mutableStateOf(budget?.notifyAt80 ?: true) }
    var notifyAt90 by remember { mutableStateOf(budget?.notifyAt90 ?: true) }
    var notifyAt100 by remember { mutableStateOf(budget?.notifyAt100 ?: true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val isValid = title.isNotBlank() && amount.isNotBlank() && amount.toDoubleOrNull() != null

    if (budget == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Budget",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF3B30)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ),
                label = "titleInput"
            ) {
                BudgetTitleInput(
                    title = title,
                    onTitleChange = { title = it },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

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
                label = "amountInput"
            ) {
                BudgetAmountInput(
                    amount = amount,
                    onAmountChange = { amount = it },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
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
                ) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 100,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ),
                label = "periodSelector"
            ) {
                BudgetPeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodChange = { selectedPeriod = it },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                label = "notificationSettings"
            ) {
                BudgetNotificationSettings(
                    notifyAt80 = notifyAt80,
                    notifyAt90 = notifyAt90,
                    notifyAt100 = notifyAt100,
                    onNotifyAt80Change = { notifyAt80 = it },
                    onNotifyAt90Change = { notifyAt90 = it },
                    onNotifyAt100Change = { notifyAt100 = it },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
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
                ) + scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 200,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ),
                label = "saveButton"
            ) {
                Button(
                    onClick = {
                        val updatedBudget = budget.copy(
                            title = title,
                            amount = amount.toDouble(),
                            periodType = selectedPeriod,
                            notifyAt80 = notifyAt80,
                            notifyAt90 = notifyAt90,
                            notifyAt100 = notifyAt100
                        )
                        viewModel.updateBudget(updatedBudget)
                        navController.popBackStack()
                    },
                    enabled = isValid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color(0xFF2C2C2E)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(56.dp)
                ) {
                    Text(
                        text = "Save Changes",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isValid) Color.Black else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete Budget?",
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this budget? This action cannot be undone.",
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBudget(budgetId)
                        navController.popBackStack()
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = Color(0xFFFF3B30),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            containerColor = Color(0xFF1C1C1E)
        )
    }
}
