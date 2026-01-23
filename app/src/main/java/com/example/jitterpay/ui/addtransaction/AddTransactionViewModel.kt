package com.example.jitterpay.ui.addtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.data.repository.TransactionRepository
import com.example.jitterpay.domain.model.Money
import com.example.jitterpay.domain.usecase.AmountCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 添加交易ViewModel - 管理添加交易表单状态
 *
 * 职责：
 * - 管理表单状态（类型、金额、分类、日期、描述）
 * - 处理用户输入（委托给 AmountCalculator）
 * - 验证并保存交易
 */
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val amountCalculator: AmountCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun setType(type: TransactionType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
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

    /**
     * 处理数字输入
     */
    fun onDigitClick(digit: String) {
        val newState = amountCalculator.process(AmountCalculator.Input.Digit(digit))
        updateAmountState(newState)
    }

    /**
     * 处理运算符输入 (+/-)
     */
    fun onOperatorClick(operator: String) {
        val newState = amountCalculator.process(AmountCalculator.Input.Operator(operator))
        updateAmountState(newState)
    }

    /**
     * 处理小数点输入
     */
    fun onDecimalClick() {
        val newState = amountCalculator.process(AmountCalculator.Input.Decimal)
        updateAmountState(newState)
    }

    /**
     * 处理退格键
     */
    fun onBackspaceClick() {
        val newState = amountCalculator.process(AmountCalculator.Input.Backspace)
        updateAmountState(newState)
    }

    /**
     * 清除金额输入
     */
    fun onClearAmount() {
        amountCalculator.reset()
        _uiState.value = _uiState.value.copy(
            amount = Money.ZERO,
            displayAmount = ""
        )
    }

    private fun updateAmountState(calcState: AmountCalculator.State) {
        _uiState.value = _uiState.value.copy(
            amount = calcState.currentAmount,
            displayAmount = calcState.displayValue
        )
    }

    fun saveTransaction() {
        val state = _uiState.value

        // 验证必填字段
        val amount = state.amount
        if (amount.isZero() || state.selectedCategory.isNullOrBlank()) {
            _uiState.value = state.copy(error = "Please fill in all required fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)

            try {
                val amountCents = TransactionEntity.parseAmountToCents(amount)
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
    val amount: Money = Money.ZERO,
    val displayAmount: String = "",  // 用于显示的原始输入
    val selectedCategory: String? = null,
    val selectedDateMillis: Long? = null,
    val description: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)
