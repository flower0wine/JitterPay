package com.example.jitterpay.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    // TODO: Inject repository when implemented
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            // TODO: Load goals from repository
            // For now, using sample data
            _uiState.value = GoalsUiState(
                goals = listOf(
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
                ),
                isLoading = false
            )
        }
    }

    fun addGoal(goal: GoalData) {
        viewModelScope.launch {
            // TODO: Add goal to repository
            val currentGoals = _uiState.value.goals.toMutableList()
            currentGoals.add(goal)
            _uiState.value = _uiState.value.copy(goals = currentGoals)
        }
    }

    fun updateGoal(goal: GoalData) {
        viewModelScope.launch {
            // TODO: Update goal in repository
            val currentGoals = _uiState.value.goals.toMutableList()
            val index = currentGoals.indexOfFirst { it.id == goal.id }
            if (index != -1) {
                currentGoals[index] = goal
                _uiState.value = _uiState.value.copy(goals = currentGoals)
            }
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            // TODO: Delete goal from repository
            val currentGoals = _uiState.value.goals.toMutableList()
            currentGoals.removeAll { it.id == goalId }
            _uiState.value = _uiState.value.copy(goals = currentGoals)
        }
    }

    fun addFundsToGoal(goalId: Long, amount: Double) {
        viewModelScope.launch {
            // TODO: Add funds to goal in repository
            val currentGoals = _uiState.value.goals.toMutableList()
            val index = currentGoals.indexOfFirst { it.id == goalId }
            if (index != -1) {
                val goal = currentGoals[index]
                currentGoals[index] = goal.copy(
                    currentAmount = (goal.currentAmount + amount).coerceAtMost(goal.targetAmount)
                )
                _uiState.value = _uiState.value.copy(goals = currentGoals)
            }
        }
    }
}

data class GoalsUiState(
    val goals: List<GoalData> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
