package com.example.jitterpay.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.repository.TransactionRepository
import com.example.jitterpay.ui.components.statistics.TimePeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 统计页面ViewModel - 管理统计数据状态
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(TimePeriod.MONTHLY)

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _selectedPeriod.collect { period ->
                combine(
                    transactionRepository.getCurrentPeriodIncome(period.name),
                    transactionRepository.getCurrentPeriodExpense(period.name),
                    transactionRepository.getCurrentPeriodExpenseByCategory(period.name)
                ) { income, expense, categoryTotals ->
                    val totalSpent = expense
                    val totalIncome = income
                    val balance = income - expense

                    // 计算分类百分比
                    val categories = if (totalSpent > 0) {
                        categoryTotals.map { categoryTotal ->
                            CategorySpending(
                                name = categoryTotal.category,
                                amount = categoryTotal.totalAmount.toDouble() / 100.0,
                                percentage = (categoryTotal.totalAmount.toDouble() / totalSpent * 100)
                            )
                        }
                    } else {
                        emptyList()
                    }

                    StatisticsUiState(
                        totalSpent = totalSpent.toDouble() / 100.0,
                        totalIncome = totalIncome.toDouble() / 100.0,
                        balance = balance.toDouble() / 100.0,
                        categories = categories,
                        selectedPeriod = period,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            }
        }
    }

    fun selectPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
    }
}

/**
 * 统计页面UI状态
 */
data class StatisticsUiState(
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val balance: Double = 0.0,
    val categories: List<CategorySpending> = emptyList(),
    val selectedPeriod: TimePeriod = TimePeriod.MONTHLY,
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * 分类支出数据
 */
data class CategorySpending(
    val name: String,
    val amount: Double,
    val percentage: Double
)
