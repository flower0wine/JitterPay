package com.example.jitterpay.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SearchUiState(
    val allTransactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType.asStateFlow()

    private val _selectedDateRange = MutableStateFlow<String?>("TODAY")
    val selectedDateRange: StateFlow<String?> = _selectedDateRange.asStateFlow()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
        observeFilters()
    }

    private fun loadTransactions() {
        viewModelScope.launch {
            transactionRepository.getAllTransactions()
                .collect { transactions ->
                    _uiState.update { it.copy(allTransactions = transactions) }
                    applyFilters()
                }
        }
    }

    private fun observeFilters() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _selectedType,
                _selectedDateRange
            ) { _, _, _ -> Unit }
                .collect {
                    applyFilters()
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedType(type: String?) {
        _selectedType.value = type
    }

    fun updateSelectedDateRange(dateRange: String?) {
        _selectedDateRange.value = dateRange
    }

    private fun applyFilters() {
        val query = _searchQuery.value
        val type = _selectedType.value
        val dateRange = _selectedDateRange.value
        val allTransactions = _uiState.value.allTransactions

        val filtered = allTransactions.filter { transaction ->
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                transaction.category.contains(query, ignoreCase = true) ||
                        transaction.description.contains(query, ignoreCase = true) ||
                        transaction.getFormattedAmount().contains(query, ignoreCase = true)
            }

            val matchesType = type == null || transaction.type == type

            val matchesDateRange = when (dateRange) {
                "TODAY" -> isToday(transaction.dateMillis)
                "WEEK" -> isThisWeek(transaction.dateMillis)
                "MONTH" -> isThisMonth(transaction.dateMillis)
                else -> true
            }

            matchesQuery && matchesType && matchesDateRange
        }

        _uiState.update { it.copy(filteredTransactions = filtered) }
    }

    private fun isToday(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return diff < TimeUnit.DAYS.toMillis(1) && diff >= 0
    }

    private fun isThisWeek(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return diff < TimeUnit.DAYS.toMillis(7) && diff >= 0
    }

    private fun isThisMonth(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return diff < TimeUnit.DAYS.toMillis(30) && diff >= 0
    }
}
