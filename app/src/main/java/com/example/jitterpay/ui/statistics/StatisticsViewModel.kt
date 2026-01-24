package com.example.jitterpay.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.local.entity.TransactionType
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
            combine(
                _selectedPeriod,
                transactionRepository.getAllTransactions()
            ) { period, allTransactions ->
                val (startMillis, endMillis) = calculatePeriodRange(period)

                // 过滤当前周期的交易
                val currentTransactions = allTransactions.filter { transaction ->
                    transaction.dateMillis >= startMillis && transaction.dateMillis < endMillis
                }

                // 计算收入总额
                val totalIncome = currentTransactions
                    .filter { it.type == TransactionType.INCOME.name }
                    .sumOf { it.amountCents }

                // 计算支出总额
                val totalSpent = currentTransactions
                    .filter { it.type == TransactionType.EXPENSE.name }
                    .sumOf { it.amountCents }

                val balance = totalIncome - totalSpent

                // 计算分类支出
                val categories = if (totalSpent > 0) {
                    currentTransactions
                        .filter { it.type == TransactionType.EXPENSE.name }
                        .groupBy { it.category }
                        .map { (category, txs) ->
                            val categoryAmount = txs.sumOf { it.amountCents }
                            CategorySpending(
                                name = category,
                                amount = categoryAmount.toDouble() / 100.0,
                                percentage = (categoryAmount.toDouble() / totalSpent * 100)
                            )
                        }
                        .sortedByDescending { it.percentage }
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

    /**
     * 计算指定周期的时间范围
     * @param period 周期类型
     * @return Pair<开始时间戳(毫秒), 结束时间戳(毫秒)>
     */
    private fun calculatePeriodRange(period: TimePeriod): Pair<Long, Long> {
        val calendar = java.util.Calendar.getInstance()
        val endMillis = calendar.timeInMillis

        when (period) {
            TimePeriod.WEEKLY -> {
                // 计算本周一
                val dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)
                val daysFromMonday = when (dayOfWeek) {
                    java.util.Calendar.SUNDAY -> 6
                    else -> dayOfWeek - 2
                }
                calendar.add(java.util.Calendar.DAY_OF_YEAR, -daysFromMonday)
            }
            TimePeriod.MONTHLY -> {
                // 设置到本月1日
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            TimePeriod.YEARLY -> {
                // 设置到本年1月1日
                calendar.set(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }

        // 清除时分秒
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)

        val startMillis = calendar.timeInMillis
        return Pair(startMillis, endMillis)
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
