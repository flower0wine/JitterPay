package com.example.jitterpay.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    // TODO: Inject repository when implemented
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecurringUiState())
    val uiState: StateFlow<RecurringUiState> = _uiState.asStateFlow()

    init {
        loadRecurringTransactions()
    }

    private fun loadRecurringTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // TODO: Load from repository
            // For now, using mock data
            val mockData = listOf(
                RecurringTransaction(
                    id = 1,
                    title = "Morning Commute",
                    category = "Transport",
                    amount = 350, // $3.50
                    frequency = RecurringFrequency.DAILY,
                    nextExecutionDate = System.currentTimeMillis(),
                    isActive = true,
                    type = "EXPENSE",
                    estimatedMonthlyAmount = 10500 // $105
                ),
                RecurringTransaction(
                    id = 2,
                    title = "Netflix Subscription",
                    category = "Entertainment",
                    amount = 1599, // $15.99
                    frequency = RecurringFrequency.MONTHLY,
                    nextExecutionDate = System.currentTimeMillis() + 86400000L * 5,
                    isActive = true,
                    type = "EXPENSE",
                    estimatedMonthlyAmount = 1599
                ),
                RecurringTransaction(
                    id = 3,
                    title = "Salary",
                    category = "Income",
                    amount = 500000, // $5000
                    frequency = RecurringFrequency.MONTHLY,
                    nextExecutionDate = System.currentTimeMillis() + 86400000L * 15,
                    isActive = true,
                    type = "INCOME",
                    estimatedMonthlyAmount = 500000
                )
            )
            
            _uiState.value = _uiState.value.copy(
                recurringTransactions = mockData,
                isLoading = false
            )
        }
    }

    fun toggleRecurringActive(id: Long) {
        viewModelScope.launch {
            val updated = _uiState.value.recurringTransactions.map { recurring ->
                if (recurring.id == id) {
                    recurring.copy(isActive = !recurring.isActive)
                } else {
                    recurring
                }
            }
            _uiState.value = _uiState.value.copy(recurringTransactions = updated)
            
            // TODO: Update in repository
        }
    }

    fun deleteRecurring(id: Long) {
        viewModelScope.launch {
            val updated = _uiState.value.recurringTransactions.filter { it.id != id }
            _uiState.value = _uiState.value.copy(recurringTransactions = updated)
            
            // TODO: Delete from repository
        }
    }
}
