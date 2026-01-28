package com.example.jitterpay.ui.recurring

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.local.entity.RecurringEntity
import com.example.jitterpay.data.repository.RecurringRepository
import com.example.jitterpay.scheduler.RecurringReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,

    // Editable fields
    val id: Long = 0,
    val title: String = "",
    val amount: String = "",
    val type: String = "EXPENSE",
    val category: String = "",
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val startDate: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,

    // Reminder (Local state only for now)
    val reminderEnabled: Boolean = false,
    val reminderDaysBefore: Int = 1
)

@HiltViewModel
class RecurringDetailViewModel @Inject constructor(
    private val recurringRepository: RecurringRepository,
    private val recurringReminderScheduler: RecurringReminderScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recurringId: Long? = savedStateHandle["recurringId"] // Key matching the route argument

    private val _uiState = MutableStateFlow(RecurringDetailUiState())
    val uiState: StateFlow<RecurringDetailUiState> = _uiState.asStateFlow()

    init {
        if (recurringId != null) {
            loadRecurringTransaction(recurringId)
        } else {
             _uiState.value = _uiState.value.copy(isLoading = false, error = "Transaction ID not found")
        }
    }

    private fun loadRecurringTransaction(id: Long) {
        viewModelScope.launch {
            try {
                recurringRepository.getByIdEntity(id)?.let { entity ->
                    val amountStr = if (entity.amountCents % 100 == 0L) {
                        (entity.amountCents / 100).toString()
                    } else {
                        String.format("%.2f", entity.amountCents / 100.0)
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        id = entity.id,
                        title = entity.title,
                        amount = amountStr,
                        type = entity.type,
                        category = entity.category,
                        frequency = RecurringFrequency.valueOf(entity.frequency.uppercase()),
                        startDate = entity.nextExecutionDateMillis,
                        isActive = entity.isActive,
                        reminderEnabled = entity.reminderEnabled,
                        reminderDaysBefore = entity.reminderDaysBefore
                    )
                } ?: run {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Transaction not found")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun setAmount(amount: String) {
        // Allow only one decimal point
        if (amount.count { it == '.' } > 1) return
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun setType(type: String) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun setCategory(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun setFrequency(frequency: RecurringFrequency) {
        _uiState.value = _uiState.value.copy(frequency = frequency)
    }

    fun setStartDate(date: Long) {
        _uiState.value = _uiState.value.copy(startDate = date)
    }

    fun setReminder(enabled: Boolean, daysBefore: Int) {
        _uiState.value = _uiState.value.copy(
            reminderEnabled = enabled,
            reminderDaysBefore = daysBefore
        )
    }

    fun toggleActive() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(isActive = !currentState.isActive)
        // We will save this change when "Save" is clicked, or we could do it immediately.
        // For detail page, usually changes are saved on "Save".
    }

    fun saveRecurring() {
        viewModelScope.launch {
            val state = _uiState.value

            if (state.title.isBlank()) {
                _uiState.value = state.copy(error = "Please enter a title")
                return@launch
            }

            val amountCents = RecurringEntity.parseAmountToCents(state.amount)
            if (amountCents <= 0) {
                _uiState.value = state.copy(error = "Please enter a valid amount")
                return@launch
            }

            try {
                recurringRepository.updateRecurring(
                    id = state.id,
                    title = state.title,
                    amountCents = amountCents,
                    type = state.type,
                    category = state.category,
                    frequency = state.frequency.name,
                    startDateMillis = state.startDate,
                    reminderEnabled = state.reminderEnabled,
                    reminderDaysBefore = state.reminderDaysBefore
                )

                // Update active state if changed
                recurringRepository.setActive(state.id, state.isActive)

                _uiState.value = state.copy(saveSuccess = true, error = null)

                // If reminders are enabled, trigger immediate check
                // to ensure reminder is scheduled without waiting for next periodic check
                if (state.reminderEnabled && state.reminderDaysBefore > 0) {
                    try {
                        recurringReminderScheduler.scheduleImmediateCheck()
                    } catch (e: Exception) {
                        // Log error but don't fail save operation
                        Log.e(
                            "RecurringDetailVM",
                            "Failed to schedule immediate reminder check",
                            e
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(error = e.message ?: "Failed to update transaction")
            }
        }
    }

    fun deleteRecurring() {
        viewModelScope.launch {
            try {
                recurringRepository.deleteById(_uiState.value.id)
                _uiState.value = _uiState.value.copy(deleteSuccess = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to delete transaction")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
