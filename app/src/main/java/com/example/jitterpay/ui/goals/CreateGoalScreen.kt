package com.example.jitterpay.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.jitterpay.ui.components.goals.addgoal.*

@Composable
fun CreateGoalScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    var goalTitle by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf<GoalIconType?>(GoalIconType.SHIELD) }

    val isFormValid = goalTitle.isNotBlank() &&
                     targetAmount.isNotBlank() &&
                     selectedIcon != null

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            AddGoalHeader(
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

            CreateGoalButton(
                isEnabled = isFormValid,
                onClick = {
                    val newGoal = GoalData(
                        id = 0,
                        title = goalTitle,
                        targetAmount = targetAmount.toDouble(),
                        currentAmount = 0.0,
                        iconType = selectedIcon!!
                    )
                    viewModel.addGoal(newGoal)
                    navController.navigateUp()
                }
            )

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
