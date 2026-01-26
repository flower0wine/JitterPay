package com.example.jitterpay.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jitterpay.ui.components.goals.addgoal.*

@Composable
fun EditGoalScreen(
    modifier: Modifier = Modifier,
    goalId: Long,
    navController: NavController,
    viewModel: GoalDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val goal = uiState.goalDetail?.goal

    var goalTitle by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf<GoalIconType?>(null) }
    var isInitialized by remember { mutableStateOf(false) }

    // Load goal data
    LaunchedEffect(goalId) {
        viewModel.loadGoal(goalId)
    }

    // Initialize form with goal data
    LaunchedEffect(goal) {
        if (goal != null && !isInitialized) {
            goalTitle = goal.title
            targetAmount = goal.targetAmount.toString()
            selectedIcon = goal.iconType
            isInitialized = true
        }
    }

    val isFormValid = goalTitle.isNotBlank() &&
                     targetAmount.isNotBlank() &&
                     selectedIcon != null

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            EditGoalHeader(
                onBackClick = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            GoalTitleInput(
                title = goalTitle,
                onTitleChange = { goalTitle = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            TargetAmountInput(
                amount = targetAmount,
                onAmountChange = { targetAmount = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            GoalIconSelector(
                selectedIcon = selectedIcon,
                onIconSelected = { selectedIcon = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            SaveGoalButton(
                isEnabled = isFormValid,
                onClick = {
                    if (isFormValid) {
                        viewModel.updateGoal(
                            title = goalTitle,
                            targetAmount = targetAmount.toDouble(),
                            iconType = selectedIcon!!
                        )
                        navController.navigateUp()
                    }
                }
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditGoalHeader(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Edit Goal",
                color = Color.White
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

@Composable
private fun SaveGoalButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = Color.Gray
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "Save Changes",
            color = if (isEnabled) Color.Black else Color.DarkGray,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
