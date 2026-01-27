package com.example.jitterpay.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.repository.RecurringRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringTransaction(
    val id: Long,
    val title: String,
    val category: String,
    val amount: Long, // in cents
    val frequency: RecurringFrequency,
    val nextExecutionDate: Long, // timestamp
    val isActive: Boolean,
    val type: String, // INCOME or EXPENSE
    val estimatedMonthlyAmount: Long // in cents
)

enum class RecurringFrequency(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    BIWEEKLY("Every 2 Weeks"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

data class RecurringUiState(
    val recurringTransactions: List<RecurringTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val recurringRepository: RecurringRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()

    init {
        loadRecurringTransactions()
    }

    private fun loadRecurringTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            recurringRepository.getAllRecurring()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load recurring transactions"
                    )
                }
                .collect { transactions ->
                    _uiState.value = _uiState.value.copy(
                        recurringTransactions = transactions,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun toggleRecurringActive(id: Long) {
        viewModelScope.launch {
            try {
                recurringRepository.toggleActive(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to toggle recurring transaction"
                )
            }
        }
    }

    fun deleteRecurring(id: Long) {
        viewModelScope.launch {
            try {
                recurringRepository.deleteById(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to delete recurring transaction"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
