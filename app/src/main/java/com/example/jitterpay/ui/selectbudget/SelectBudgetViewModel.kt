package com.example.jitterpay.ui.selectbudget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.data.repository.BudgetRepository
import com.example.jitterpay.data.repository.TransactionRepository
import com.example.jitterpay.ui.budget.BudgetData
import com.example.jitterpay.ui.budget.toBudgetData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectBudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectBudgetUiState())
    val uiState: StateFlow<SelectBudgetUiState> = _uiState.asStateFlow()

    init {
        loadBudgets()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                combine(
                    budgetRepository.getAllBudgets(),
                    transactionRepository.getAllTransactions()
                ) { budgets, allTransactions ->
                    val activeBudgets = budgets
                        .filter { it.isActive }
                        .map { budget ->
                            val (startDate, endDate) = budget.getCurrentPeriodRange()
                            val spentCents = allTransactions
                                .filter { transaction ->
                                    transaction.dateMillis in (startDate..endDate) &&
                                    transaction.type == TransactionType.EXPENSE.name
                                }
                                .sumOf { it.amountCents }
                            budget.toBudgetData(spentCents / 100.0)
                        }
                    
                    SelectBudgetUiState(
                        budgets = activeBudgets,
                        isLoading = false
                    )
                }.collect { newState ->
                    _uiState.value = newState
                }
            } catch (e: Exception) {
                _uiState.value = SelectBudgetUiState(
                    budgets = emptyList(),
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun selectBudget(budgetId: Long?) {
        _uiState.value = _uiState.value.copy(selectedBudgetId = budgetId)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class SelectBudgetUiState(
    val budgets: List<BudgetData> = emptyList(),
    val selectedBudgetId: Long? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
