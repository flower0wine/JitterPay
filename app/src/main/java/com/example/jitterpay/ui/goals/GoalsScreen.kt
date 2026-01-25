package com.example.jitterpay.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jitterpay.ui.components.goals.GoalCard
import com.example.jitterpay.ui.components.goals.GoalsHeader
import com.example.jitterpay.ui.components.goals.TotalProgressCard

@Composable
fun GoalsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // TODO: Replace with ViewModel data
    val sampleGoals = remember {
        listOf(
            GoalData(
                id = 1,
                title = "Emergency Fund",
                targetAmount = 10000.0,
                currentAmount = 7500.0,
                category = GoalCategory.SAVINGS,
                iconType = GoalIconType.SHIELD
            ),
            GoalData(
                id = 2,
                title = "Dream Vacation",
                targetAmount = 5000.0,
                currentAmount = 3200.0,
                category = GoalCategory.TRAVEL,
                iconType = GoalIconType.FLIGHT
            ),
            GoalData(
                id = 3,
                title = "New Laptop",
                targetAmount = 2000.0,
                currentAmount = 2000.0,
                category = GoalCategory.PURCHASE,
                iconType = GoalIconType.LAPTOP
            )
        )
    }

    val totalTarget = sampleGoals.sumOf { it.targetAmount }
    val totalCurrent = sampleGoals.sumOf { it.currentAmount }
    val completedGoals = sampleGoals.count { it.isCompleted }

    Scaffold(
        containerColor = Color.Black,
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            GoalsHeader()

            Spacer(modifier = Modifier.height(20.dp))

            TotalProgressCard(
                totalTarget = totalTarget,
                totalCurrent = totalCurrent,
                completedGoals = completedGoals,
                totalGoals = sampleGoals.size
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "YOUR GOALS",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            sampleGoals.forEach { goal ->
                GoalCard(
                    goal = goal,
                    onCardClick = {
                        navController.navigate(
                            com.example.jitterpay.constants.NavigationRoutes.goalDetail(goal.id)
                        )
                    },
                    onAddFunds = { /* TODO: Add funds to goal */ }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

data class GoalData(
    val id: Long,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val category: GoalCategory,
    val iconType: GoalIconType
) {
    val progress: Float
        get() = (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f)
    
    val isCompleted: Boolean
        get() = currentAmount >= targetAmount
    
    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)
}

enum class GoalCategory {
    SAVINGS,
    TRAVEL,
    PURCHASE,
    INVESTMENT,
    OTHER
}

enum class GoalIconType {
    SHIELD,
    FLIGHT,
    LAPTOP,
    HOME,
    CAR,
    EDUCATION,
    HEALTH,
    GIFT
}
