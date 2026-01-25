package com.example.jitterpay.ui.goals

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Flag
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

    val hasGoals = uiState.goals.isNotEmpty()
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

            if (hasGoals) {
                Spacer(modifier = Modifier.height(20.dp))
                TotalProgressCard(
                    totalTarget = totalTarget,
                    totalCurrent = totalCurrent,
                    completedGoals = completedGoals,
                    totalGoals = uiState.goals.size
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (hasGoals) {
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
            }

            if (hasGoals) {
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
            } else {
                EmptyGoalsState(
                    onCreateGoal = {
                        navController.navigate(com.example.jitterpay.constants.NavigationRoutes.ADD_GOAL)
                    }
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun EmptyGoalsState(
    onCreateGoal: () -> Unit,
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
                imageVector = Icons.Outlined.Flag,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.4f),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "No Goals Yet",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Start your savings journey by creating\nyour first financial goal",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onCreateGoal,
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
                    text = "Create Your First Goal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

