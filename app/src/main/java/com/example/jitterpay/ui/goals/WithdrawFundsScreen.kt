package com.example.jitterpay.ui.goals

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jitterpay.domain.model.Money
import com.example.jitterpay.domain.usecase.AmountCalculator
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.components.addtransaction.AmountDisplay
import com.example.jitterpay.ui.components.addtransaction.NumberPad
import java.text.DecimalFormat

@Composable
fun WithdrawFundsScreen(
    modifier: Modifier = Modifier,
    goalId: Long,
    navController: NavController,
    viewModel: GoalDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val goal = uiState.goalDetail?.goal
    
    var isVisible by remember { mutableStateOf(false) }
    val amountCalculator = remember { AmountCalculator() }
    var currentAmount by remember { mutableStateOf(Money.ZERO) }
    var displayAmount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(goalId) {
        viewModel.loadGoal(goalId)
        isVisible = true
    }

    // Track if we've initiated a save operation
    var hasSaved by remember { mutableStateOf(false) }

    // Handle save success - navigate back when loading completes after save
    LaunchedEffect(uiState.isLoading, hasSaved) {
        if (hasSaved && !uiState.isLoading && uiState.error == null) {
            navController.popBackStack()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
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
            WithdrawFundsHeader(
                goalTitle = goal?.title ?: "",
                currentAmount = goal?.currentAmount ?: 0.0,
                onClose = { navController.popBackStack() },
                onDone = {
                    val goalCurrentAmount = goal?.currentAmount ?: 0.0
                    val withdrawAmount = currentAmount.toBigDecimal().toDouble()
                    
                    when {
                        withdrawAmount <= 0 -> {
                            errorMessage = "Amount must be greater than 0"
                        }
                        withdrawAmount > goalCurrentAmount -> {
                            errorMessage = "Insufficient funds (max: ${DecimalFormat("#,##0.00").format(goalCurrentAmount)})"
                        }
                        else -> {
                            errorMessage = null
                            hasSaved = true
                            viewModel.withdrawFunds(
                                amount = withdrawAmount,
                                description = description.ifBlank { "Withdrew funds" }
                            )
                        }
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Amount Display
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
                AmountDisplay(amount = currentAmount.toBigDecimal())
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.SHORT,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ) + expandVertically(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ) + slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
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
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        easing = AnimationConstants.Easing.Exit
                    )
                ) + slideOutVertically(
                    targetOffsetY = { -it / 2 },
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        easing = AnimationConstants.Easing.Exit
                    )
                )
            ) {
                errorMessage?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF5252).copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                color = Color(0xFFFF5252),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Remaining Balance Preview - only show when no error
            AnimatedVisibility(
                visible = errorMessage == null && !currentAmount.isZero() && goal != null && 
                         (goal.currentAmount - currentAmount.toBigDecimal().toDouble()) >= 0,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.SHORT,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ) + expandVertically(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ) + slideInVertically(
                    initialOffsetY = { -it / 2 },
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
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
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        easing = AnimationConstants.Easing.Exit
                    )
                ) + slideOutVertically(
                    targetOffsetY = { -it / 2 },
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        easing = AnimationConstants.Easing.Exit
                    )
                )
            ) {
                val remainingBalance = goal?.currentAmount?.minus(currentAmount.toBigDecimal().toDouble()) ?: 0.0
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1C1C1E)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Remaining Balance",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${DecimalFormat("#,##0.00").format(remainingBalance)}",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description Input (Optional)
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = AnimationConstants.Duration.MEDIUM,
                        delayMillis = 150,
                        easing = AnimationConstants.Easing.Entrance
                    )
                ),
                label = "description"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = "REASON (OPTIONAL)",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("e.g., Emergency expense") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Number Pad
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.MEDIUM,
                    delayMillis = 200,
                    easing = AnimationConstants.Easing.Entrance
                )
            ) + slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.MEDIUM,
                    delayMillis = 200,
                    easing = AnimationConstants.Easing.Entrance
                )
            ),
            label = "numberPad"
        ) {
            NumberPad(
                inputDisplay = displayAmount,
                onNumberClick = { digit ->
                    errorMessage = null // Clear error on new input
                    if (digit in "0".."9") {
                        val newState = amountCalculator.process(AmountCalculator.Input.Digit(digit))
                        currentAmount = newState.currentAmount
                        displayAmount = newState.displayValue
                    } else if (digit == ".") {
                        val newState = amountCalculator.process(AmountCalculator.Input.Decimal)
                        currentAmount = newState.currentAmount
                        displayAmount = newState.displayValue
                    }
                },
                onOperatorClick = { operator ->
                    errorMessage = null
                    val newState = amountCalculator.process(AmountCalculator.Input.Operator(operator))
                    currentAmount = newState.currentAmount
                    displayAmount = newState.displayValue
                },
                onBackspace = {
                    errorMessage = null
                    val newState = amountCalculator.process(AmountCalculator.Input.Backspace)
                    currentAmount = newState.currentAmount
                    displayAmount = newState.displayValue
                },
                onConfirm = {
                    val goalCurrentAmount = goal?.currentAmount ?: 0.0
                    val withdrawAmount = currentAmount.toBigDecimal().toDouble()
                    
                    when {
                        withdrawAmount <= 0 -> {
                            errorMessage = "Amount must be greater than 0"
                        }
                        withdrawAmount > goalCurrentAmount -> {
                            errorMessage = "Insufficient funds (max: ${DecimalFormat("#,##0.00").format(goalCurrentAmount)})"
                        }
                        else -> {
                            errorMessage = null
                            hasSaved = true
                            viewModel.withdrawFunds(
                                amount = withdrawAmount,
                                description = description.ifBlank { "Withdrew funds" }
                            )
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WithdrawFundsHeader(
    goalTitle: String,
    currentAmount: Double,
    onClose: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Withdraw Funds",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = goalTitle,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            },
            actions = {
                TextButton(onClick = onDone) {
                    Text(
                        text = "Done",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black
            )
        )
        
        // Current Balance Info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Available Balance",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Text(
                text = "${DecimalFormat("#,##0.00").format(currentAmount)}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
    }
}
