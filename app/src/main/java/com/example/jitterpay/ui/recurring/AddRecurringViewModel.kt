package com.example.jitterpay.ui.recurring

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.local.entity.RecurringEntity
import com.example.jitterpay.data.repository.RecurringRepository
import com.example.jitterpay.scheduler.RecurringReminderScheduler
import com.example.jitterpay.worker.RecurringReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddRecurringUiState(
    val title: String = "",
    val amount: String = "0.00",
    val category: String = "Transport",
    val frequency: RecurringFrequency = RecurringFrequency.DAILY,
    val startDate: Long = System.currentTimeMillis(),
    val type: String = "EXPENSE",
    val reminderEnabled: Boolean = false,
    val reminderDaysBefore: Int = 0,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddRecurringViewModel @Inject constructor(
    private val recurringRepository: RecurringRepository,
    private val recurringReminderScheduler: RecurringReminderScheduler,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(RecurringReminderWorker.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _uiState = MutableStateFlow(AddRecurringUiState())
    val uiState: StateFlow<AddRecurringUiState> = _uiState.asStateFlow()

    fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun setAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
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

    fun setType(type: String) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun setReminder(enabled: Boolean, daysBefore: Int) {
        _uiState.value = _uiState.value.copy(
            reminderEnabled = enabled,
            reminderDaysBefore = daysBefore
        )

        // If enabling reminders, clear any existing reminder sent status
        // This ensures the new reminder settings take effect immediately
        if (enabled && daysBefore > 0) {
            clearReminderStatus()
        }
    }

    private fun clearReminderStatus() {
        // Clear all reminder sent status for this recurring transaction
        // This allows the reminder to be re-sent with new settings
        val currentState = _uiState.value
        val keyPrefix = RecurringReminderWorker.KEY_REMINDER_SENT_PREFIX
        prefs.edit().remove(keyPrefix).apply()
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
                recurringRepository.addRecurring(
                    title = state.title,
                    amountCents = amountCents,
                    type = state.type,
                    category = state.category,
                    frequency = state.frequency.name,
                    startDateMillis = state.startDate,
                    reminderEnabled = state.reminderEnabled,
                    reminderDaysBefore = state.reminderDaysBefore
                )
                _uiState.value = state.copy(saveSuccess = true, error = null)

                // If reminders are enabled, trigger immediate check
                // to ensure reminder is scheduled without waiting for next periodic check
                if (state.reminderEnabled && state.reminderDaysBefore > 0) {
                    try {
                        recurringReminderScheduler.scheduleImmediateCheck()
                    } catch (e: Exception) {
                        // Log error but don't fail save operation
                        Log.e(
                            "AddRecurringVM",
                            "Failed to schedule immediate reminder check",
                            e
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(error = e.message ?: "Failed to save recurring transaction")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
