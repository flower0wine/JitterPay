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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jitterpay.ui.components.goals.GoalCard
import com.example.jitterpay.ui.components.goals.GoalsHeader
import com.example.jitterpay.ui.components.goals.TotalProgressCard

@Composable
fun GoalsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val totalTarget = uiState.goals.sumOf { it.targetAmount }
    val totalCurrent = uiState.goals.sumOf { it.currentAmount }
    val completedGoals = uiState.goals.count { it.isCompleted }

    Scaffold(
        containerColor = Color.Black
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
                totalGoals = uiState.goals.size
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR GOALS",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = {
                        navController.navigate(com.example.jitterpay.constants.NavigationRoutes.ADD_GOAL)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Goal",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New Goal",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            uiState.goals.forEach { goal ->
                GoalCard(
                    goal = goal,
                    onCardClick = {
                        navController.navigate(
                            com.example.jitterpay.constants.NavigationRoutes.goalDetail(goal.id)
                        )
                    },
                    onAddFunds = { viewModel.addFundsToGoal(goal.id, 100.0) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

