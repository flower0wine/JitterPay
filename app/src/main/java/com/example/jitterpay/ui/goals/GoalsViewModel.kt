package com.example.jitterpay.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.local.entity.GoalEntity
import com.example.jitterpay.data.local.entity.GoalIconType as DomainGoalIconType
import com.example.jitterpay.data.repository.GoalRepository
import com.example.jitterpay.ui.goals.GoalIconType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                goalRepository.getAllGoals().collect { goals ->
                    _uiState.value = GoalsUiState(
                        goals = goals.map { it.toGoalData() },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = GoalsUiState(
                    goals = emptyList(),
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun addGoal(goalData: GoalData) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                goalRepository.createGoal(
                    title = goalData.title,
                    targetAmountCents = GoalEntity.parseAmountToCents(goalData.targetAmount),
                    iconType = goalData.iconType.name
                )
                // loadGoals()会被Flow自动触发
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateGoal(goalData: GoalData) {
        viewModelScope.launch {
            try {
                val entity = GoalEntity(
                    id = goalData.id,
                    title = goalData.title,
                    targetAmountCents = GoalEntity.parseAmountToCents(goalData.targetAmount),
                    currentAmountCents = GoalEntity.parseAmountToCents(goalData.currentAmount),
                    iconType = goalData.iconType.name,
                    isCompleted = goalData.isCompleted
                )
                goalRepository.updateGoal(entity)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            try {
                goalRepository.deleteGoalById(goalId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun addFundsToGoal(goalId: Long, amount: Double) {
        viewModelScope.launch {
            try {
                goalRepository.addFundsToGoal(
                    goalId = goalId,
                    amountCents = GoalEntity.parseAmountToCents(amount),
                    description = "Added funds"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun withdrawFromGoal(goalId: Long, amount: Double) {
        viewModelScope.launch {
            try {
                goalRepository.withdrawFromGoal(
                    goalId = goalId,
                    amountCents = GoalEntity.parseAmountToCents(amount),
                    description = "Withdrew funds"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class GoalsUiState(
    val goals: List<GoalData> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * 将GoalEntity转换为UI使用的GoalData
 */
private fun GoalEntity.toGoalData(): GoalData {
    return GoalData(
        id = id,
        title = title,
        targetAmount = targetAmountCents / 100.0,
        currentAmount = currentAmountCents / 100.0,
        iconType = GoalIconType.valueOf(iconType)
    )
}
