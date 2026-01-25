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
import com.example.jitterpay.ui.components.goals.detail.*

@Composable
fun GoalDetailScreen(
    goalId: Long,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // TODO: Replace with ViewModel data
    val goal = remember {
        GoalData(
            id = goalId,
            title = "Emergency Fund",
            targetAmount = 10000.0,
            currentAmount = 7500.0,
            category = GoalCategory.SAVINGS,
            iconType = GoalIconType.SHIELD
        )
    }

    val milestones = remember {
        calculateMilestones(goal)
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

            GoalDetailHeader(goal = goal)

            Spacer(modifier = Modifier.height(24.dp))

            GoalProgressSection(goal = goal)

            Spacer(modifier = Modifier.height(24.dp))

            MilestonesSection(
                milestones = milestones,
                currentAmount = goal.currentAmount
            )

            Spacer(modifier = Modifier.height(24.dp))

            GoalActionsSection(
                goal = goal,
                onAddFunds = { /* TODO: Add funds */ },
                onWithdraw = { /* TODO: Withdraw */ },
                onEditGoal = { /* TODO: Edit goal */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            TransactionHistorySection(
                goalId = goal.id
            )

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
        title = { },
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
