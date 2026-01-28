package com.example.jitterpay.ui.budget

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun AddBudgetScreen(
    onClose: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf(BudgetPeriodType.MONTHLY) }
    var notifyAt80 by remember { mutableStateOf(true) }
    var notifyAt90 by remember { mutableStateOf(true) }
    var notifyAt100 by remember { mutableStateOf(true) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val isValid = title.isNotBlank() && amount.isNotBlank() && amount.toDoubleOrNull() != null

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Budget",
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
                label = "createButton"
            ) {
                Button(
                    onClick = {
                        val budgetData = BudgetData(
                            title = title,
                            amount = amount.toDouble(),
                            periodType = selectedPeriod,
                            startDate = System.currentTimeMillis(),
                            notifyAt80 = notifyAt80,
                            notifyAt90 = notifyAt90,
                            notifyAt100 = notifyAt100
                        )
                        viewModel.createBudget(budgetData)
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
                        text = "Create Budget",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isValid) Color.Black else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
