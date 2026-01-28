package com.example.jitterpay.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.local.entity.BudgetEntity
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.data.repository.BudgetRepository
import com.example.jitterpay.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudgets()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                budgetRepository.getAllBudgets().collect { budgets ->
                    val budgetsWithSpent = mutableListOf<BudgetData>()
                    for (budget in budgets) {
                        val (startDate, endDate) = budget.getCurrentPeriodRange()
                        var spent = 0.0
                        transactionRepository.getTransactionsByDateRange(startDate, endDate)
                            .collect { transactions ->
                                val spentCents = transactions
                                    .filter { it.type == TransactionType.EXPENSE.name }
                                    .sumOf { it.amountCents }
                                spent = spentCents / 100.0
                            }
                        budgetsWithSpent.add(budget.toBudgetData(spent))
                    }
                    _uiState.value = BudgetUiState(
                        budgets = budgetsWithSpent,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = BudgetUiState(
                    budgets = emptyList(),
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun createBudget(budgetData: BudgetData) {
        viewModelScope.launch {
            try {
                budgetRepository.createBudget(
                    title = budgetData.title,
                    amountCents = BudgetEntity.parseAmountToCents(budgetData.amount),
                    periodType = budgetData.periodType.name,
                    startDate = budgetData.startDate,
                    endDate = budgetData.endDate,
                    notifyAt80 = budgetData.notifyAt80,
                    notifyAt90 = budgetData.notifyAt90,
                    notifyAt100 = budgetData.notifyAt100
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateBudget(budgetData: BudgetData) {
        viewModelScope.launch {
            try {
                val entity = BudgetEntity(
                    id = budgetData.id,
                    title = budgetData.title,
                    amountCents = BudgetEntity.parseAmountToCents(budgetData.amount),
                    periodType = budgetData.periodType.name,
                    startDate = budgetData.startDate,
                    endDate = budgetData.endDate,
                    notifyAt80 = budgetData.notifyAt80,
                    notifyAt90 = budgetData.notifyAt90,
                    notifyAt100 = budgetData.notifyAt100,
                    isActive = budgetData.isActive
                )
                budgetRepository.updateBudget(entity)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            try {
                budgetRepository.deleteBudgetById(budgetId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class BudgetUiState(
    val budgets: List<BudgetData> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
