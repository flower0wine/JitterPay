package com.example.jitterpay.ui.selectbudget

import android.util.Log
import androidx.lifecycle.SavedStateHandle
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

/**
 * 选择预算页面的 ViewModel
 *
 * 支持两种模式：
 * 1. 编辑模式：transactionId != null，更新现有交易的预算
 * 2. 新增模式：transactionId == null，只显示预算列表供选择
 */
@HiltViewModel
class SelectBudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "SelectBudgetViewModel"
    }

    private val _uiState = MutableStateFlow(SelectBudgetUiState())
    val uiState: StateFlow<SelectBudgetUiState> = _uiState.asStateFlow()

    private var currentTransactionId: Long? = null

    fun loadBudgets(transactionId: Long?) {
        currentTransactionId = transactionId
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
                            val spentAmount = allTransactions
                                .filter { tx ->
                                    tx.dateMillis in (startDate..endDate) &&
                                    tx.type == TransactionType.EXPENSE.name
                                }
                                .sumOf { it.amountCents }
                            budget.toBudgetData(spentAmount / 100.0)
                        }

                    // 获取当前交易的预算ID（如果有的话）
                    val currentTransactionBudgetId = transactionId?.let { txId ->
                        transactionRepository.getTransactionById(txId)?.budgetId
                    }

                    // 排序：将当前交易关联的预算排在第一个
                    val sortedBudgets = if (currentTransactionBudgetId != null) {
                        activeBudgets.sortedBy { budget ->
                            if (budget.id == currentTransactionBudgetId) 0 else 1
                        }
                    } else {
                        activeBudgets
                    }

                    // 默认选中当前交易的预算（如果有），否则选中第一个预算
                    val defaultSelectedBudgetId = currentTransactionBudgetId
                        ?: sortedBudgets.firstOrNull()?.id

                    SelectBudgetUiState(
                        budgets = sortedBudgets,
                        selectedBudgetId = defaultSelectedBudgetId,
                        isLoading = false
                    )
                }.collect { newState ->
                    _uiState.value = newState.copy(
                        saveSuccess = _uiState.value.saveSuccess
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load budgets", e)
                _uiState.value = SelectBudgetUiState(
                    budgets = emptyList(),
                    selectedBudgetId = null,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * 确认选择并更新交易的预算ID
     */
    fun confirmSelection() {
        val state = _uiState.value
        val transactionId = currentTransactionId ?: return
        val selectedBudgetId = state.selectedBudgetId

        viewModelScope.launch {
            try {
                // 更新交易的预算ID
                transactionRepository.updateTransactionBudgetId(transactionId, selectedBudgetId)
                Log.d(TAG, "Transaction $transactionId budget updated to $selectedBudgetId")
                _uiState.value = _uiState.value.copy(saveSuccess = true)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update transaction budget", e)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to update budget"
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

/**
 * 选择预算页面的 UI 状态
 */
data class SelectBudgetUiState(
    val budgets: List<BudgetData> = emptyList(),
    val selectedBudgetId: Long? = null,
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false,
    val error: String? = null
)
