package com.example.jitterpay.ui.goals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.jitterpay.constants.NavigationRoutes
import com.example.jitterpay.ui.components.goals.detail.*
import com.example.jitterpay.ui.theme.ErrorRed

@Composable
fun GoalDetailScreen(
    modifier: Modifier = Modifier,
    goalId: Long,
    navController: NavController,
    viewModel: GoalDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(goalId) {
        viewModel.loadGoal(goalId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val quickAddAmount by viewModel.quickAddAmount.collectAsState()

    val goal = uiState.goalDetail?.goal
    val milestones = goal?.let { calculateMilestones(it) } ?: emptyList()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.goalDeleted) {
        if (uiState.goalDeleted) {
            navController.popBackStack()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Goal?", color = Color.White) },
            text = { 
                Text(
                    "This will permanently delete this goal and all its transaction history. This action cannot be undone.",
                    color = Color.Gray
                ) 
            },
            containerColor = MaterialTheme.colorScheme.background,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGoal()
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
        containerColor = Color.Black,
        topBar = {
            GoalDetailTopBar(
                onNavigateBack = { navController.popBackStack() }
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

            goal?.let {
                GoalDetailHeader(goal = it)

                Spacer(modifier = Modifier.height(24.dp))

                GoalProgressSection(goal = it)

                Spacer(modifier = Modifier.height(24.dp))

                MilestonesSection(
                    milestones = milestones,
                    currentAmount = it.currentAmount
                )

                Spacer(modifier = Modifier.height(24.dp))

                GoalActionsSection(
                    goal = it,
                    onAddFunds = { navController.navigate(NavigationRoutes.addFunds(goalId)) },
                    onWithdraw = { navController.navigate(NavigationRoutes.withdrawFunds(goalId)) },
                    onEditGoal = { navController.navigate(NavigationRoutes.editGoal(goalId)) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                QuickAddAmountSection(
                    currentAmount = quickAddAmount,
                    onAmountChange = { newAmount ->
                        viewModel.updateQuickAddAmount(newAmount)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                TransactionHistorySection(
                    goalId = goalId,
                    transactions = uiState.goalDetail?.transactions ?: emptyList()
                )

                Spacer(modifier = Modifier.height(40.dp))

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ErrorRed
                    ),
                    border = BorderStroke(1.dp, ErrorRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Delete Goal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalDetailTopBar(
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = { 
            Text(
                text = "Goal Details",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
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

private fun calculateMilestones(goal: GoalData): List<Milestone> {
    val target = goal.targetAmount
    return listOf(
        Milestone(
            percentage = 25,
            amount = target * 0.25,
            title = "First Quarter",
            description = "Great start!",
            iconType = MilestoneIconType.STAR
        ),
        Milestone(
            percentage = 50,
            amount = target * 0.50,
            title = "Halfway There",
            description = "You're doing amazing!",
            iconType = MilestoneIconType.TROPHY
        ),
        Milestone(
            percentage = 75,
            amount = target * 0.75,
            title = "Almost Done",
            description = "Keep pushing!",
            iconType = MilestoneIconType.MEDAL
        ),
        Milestone(
            percentage = 100,
            amount = target,
            title = "Goal Achieved",
            description = "Congratulations!",
            iconType = MilestoneIconType.CROWN
        )
    )
}

data class Milestone(
    val percentage: Int,
    val amount: Double,
    val title: String,
    val description: String,
    val iconType: MilestoneIconType
) {
    fun isAchieved(currentAmount: Double): Boolean {
        return currentAmount >= amount
    }
}

enum class MilestoneIconType {
    STAR,
    TROPHY,
    MEDAL,
    CROWN
}

