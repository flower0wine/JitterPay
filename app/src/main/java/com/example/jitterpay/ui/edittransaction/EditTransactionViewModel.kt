package com.example.jitterpay.ui.edittransaction

import androidx.lifecycle.SavedStateHandle
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
 * 编辑交易ViewModel - 管理编辑交易表单状态
 *
 * 职责：
 * - 加载现有交易数据
 * - 管理表单状态（类型、金额、分类、日期、描述）
 * - 处理用户输入（委托给 AmountCalculator）
 * - 验证并更新交易
 * - 删除交易
 */
@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val amountCalculator: AmountCalculator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<Long>("transactionId") ?: 0L

    private val _uiState = MutableStateFlow(EditTransactionUiState())
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()

    init {
        loadTransaction()
    }

    /**
     * 加载交易数据
     */
    private fun loadTransaction() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val transaction = transactionRepository.getTransactionById(transactionId)
                if (transaction != null) {
                    val money = Money.fromCents(transaction.amountCents)
                    val displayAmount = money.formatSimplified()

                    // 初始化 AmountCalculator 的状态
                    amountCalculator.reset()
                    displayAmount.forEach { char ->
                        when {
                            char.isDigit() -> amountCalculator.process(AmountCalculator.Input.Digit(char.toString()))
                            char == '.' -> amountCalculator.process(AmountCalculator.Input.Decimal)
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        originalTransaction = transaction,
                        selectedType = TransactionType.valueOf(transaction.type),
                        amount = money,
                        displayAmount = displayAmount,
                        selectedCategory = transaction.category,
                        selectedDateMillis = transaction.dateMillis,
                        description = transaction.description
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Transaction not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load transaction"
                )
            }
        }
    }

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

    private fun updateAmountState(calcState: AmountCalculator.State) {
        _uiState.value = _uiState.value.copy(
            amount = calcState.currentAmount,
            displayAmount = calcState.displayValue
        )
    }

    /**
     * 更新交易
     */
    fun updateTransaction() {
        val state = _uiState.value

        // 验证必填字段
        val amount = state.amount
        if (amount.isZero() || state.selectedCategory.isBlank()) {
            _uiState.value = state.copy(error = "Please fill in all required fields")
            return
        }

        val originalTransaction = state.originalTransaction
        if (originalTransaction == null) {
            _uiState.value = state.copy(error = "Transaction not found")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)

            try {
                val amountCents = TransactionEntity.parseAmountToCents(amount)
                val dateMillis = state.selectedDateMillis

                val updatedTransaction = originalTransaction.copy(
                    type = state.selectedType.name,
                    amountCents = amountCents,
                    category = state.selectedCategory,
                    description = state.description,
                    dateMillis = dateMillis,
                    updatedAt = System.currentTimeMillis()
                )

                transactionRepository.updateTransaction(updatedTransaction)

                _uiState.value = state.copy(isSaving = false, saveSuccess = true)
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to update transaction"
                )
            }
        }
    }

    /**
     * 删除交易
     */
    fun deleteTransaction() {
        val state = _uiState.value
        val originalTransaction = state.originalTransaction

        if (originalTransaction == null) {
            _uiState.value = state.copy(error = "Transaction not found")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isDeleting = true)

            try {
                transactionRepository.deleteTransaction(originalTransaction)
                _uiState.value = state.copy(isDeleting = false, deleteSuccess = true)
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isDeleting = false,
                    error = e.message ?: "Failed to delete transaction"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * 编辑交易UI状态
 */
data class EditTransactionUiState(
    val isLoading: Boolean = false,
    val originalTransaction: TransactionEntity? = null,
    val selectedType: TransactionType = TransactionType.EXPENSE,
    val amount: Money = Money.ZERO,
    val displayAmount: String = "",
    val selectedCategory: String = "",
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val description: String = "",
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)
