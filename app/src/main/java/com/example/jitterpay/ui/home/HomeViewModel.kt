package com.example.jitterpay.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 首页ViewModel - 管理首页数据状态
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // 合并交易列表和总余额
            combine(
                transactionRepository.getAllTransactions(),
                transactionRepository.getTotalBalance()
            ) { transactions, balance ->
                HomeUiState(
                    transactions = transactions,
                    totalBalance = balance,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadData()
    }
}

/**
 * 首页UI状态
 */
data class HomeUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val totalBalance: Long = 0L,
    val isLoading: Boolean = true,
    val error: String? = null
)
