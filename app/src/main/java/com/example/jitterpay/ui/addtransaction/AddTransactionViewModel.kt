package com.example.jitterpay.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 添加交易ViewModel - 管理添加交易表单状态
 */
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun setType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun setAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun setCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setDate(dateMillis: Long) {
        _uiState.value = _uiState.value.copy(selectedDateMillis = dateMillis)
    }

    fun setDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun saveTransaction() {
        val state = _uiState.value

        // 验证必填字段
        if (state.selectedCategory.isNullOrBlank() || state.amount == "0.00") {
            _uiState.value = state.copy(error = "Please fill in all required fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)

            try {
                val amountCents = TransactionEntity.parseAmountToCents(state.amount)
                val dateMillis = state.selectedDateMillis ?: System.currentTimeMillis()

                transactionRepository.addTransaction(
                    type = state.selectedType,
                    amountCents = amountCents,
                    category = state.selectedCategory!!,
                    description = state.description,
                    dateMillis = dateMillis
                )

                _uiState.value = state.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save transaction"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * 添加交易UI状态
 */
data class AddTransactionUiState(
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val amount: String = "0.00",
    val selectedCategory: String? = null,
    val selectedDateMillis: Long? = null,
    val description: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)
