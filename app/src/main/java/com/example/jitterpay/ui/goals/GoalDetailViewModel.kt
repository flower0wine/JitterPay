package com.example.jitterpay.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.local.entity.GoalEntity
import com.example.jitterpay.data.local.entity.GoalIconType as DomainGoalIconType
import com.example.jitterpay.data.local.entity.GoalTransactionEntity
import com.example.jitterpay.data.repository.GoalRepository
import com.example.jitterpay.data.repository.UserPreferencesRepository
import com.example.jitterpay.ui.goals.GoalIconType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalDetailUiState())
    val uiState: StateFlow<GoalDetailUiState> = _uiState.asStateFlow()

    val quickAddAmount: StateFlow<Int> = userPreferencesRepository.getQuickAddAmount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 100
        )

    private var currentGoalId: Long = 0

    fun loadGoal(goalId: Long) {
        currentGoalId = goalId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // 并行加载目标信息和交易记录
                combine(
                    goalRepository.getGoalByIdFlow(goalId),
                    goalRepository.getTransactionsByGoalId(goalId)
                ) { goal, transactions ->
                    if (goal != null) {
                        GoalDetailData(
                            goal = goal.toGoalData(),
                            transactions = transactions
                        )
                    } else {
                        null
                    }
                }.catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }.collect { detailData ->
                    if (detailData != null) {
                        _uiState.value = _uiState.value.copy(
                            goalDetail = detailData,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Goal not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun addFunds(amount: Double, description: String = "") {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                goalRepository.addFundsToGoal(
                    goalId = currentGoalId,
                    amountCents = GoalEntity.parseAmountToCents(amount),
                    description = description.ifBlank { "Added funds" }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun withdrawFunds(amount: Double, description: String = "") {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                goalRepository.withdrawFromGoal(
                    goalId = currentGoalId,
                    amountCents = GoalEntity.parseAmountToCents(amount),
                    description = description.ifBlank { "Withdrew funds" }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun deleteGoal() {
        viewModelScope.launch {
            try {
                goalRepository.deleteGoalById(currentGoalId)
                _uiState.value = _uiState.value.copy(goalDeleted = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteTransaction(transaction: GoalTransactionEntity) {
        viewModelScope.launch {
            try {
                goalRepository.deleteGoalTransaction(transaction)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateGoal(title: String, targetAmount: Double, iconType: GoalIconType) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val currentGoal = _uiState.value.goalDetail?.goal
                if (currentGoal != null) {
                    val updatedEntity = com.example.jitterpay.data.local.entity.GoalEntity(
                        id = currentGoal.id,
                        title = title,
                        targetAmountCents = com.example.jitterpay.data.local.entity.GoalEntity.parseAmountToCents(targetAmount),
                        currentAmountCents = com.example.jitterpay.data.local.entity.GoalEntity.parseAmountToCents(currentGoal.currentAmount),
                        iconType = iconType.name,
                        isCompleted = currentGoal.isCompleted,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    goalRepository.updateGoal(updatedEntity)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun updateQuickAddAmount(amount: Int) {
        viewModelScope.launch {
            try {
                userPreferencesRepository.saveQuickAddAmount(amount)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

data class GoalDetailUiState(
    val goalDetail: GoalDetailData? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val goalDeleted: Boolean = false
)

data class GoalDetailData(
    val goal: GoalData,
    val transactions: List<GoalTransactionEntity>
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
