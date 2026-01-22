package com.example.jitterpay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.jitterpay.data.local.entity.TransactionType
import com.example.jitterpay.ui.addtransaction.AddTransactionViewModel
import com.example.jitterpay.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddTransactionScreen(
    onClose: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()

    // 处理错误提示
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    // 处理保存成功
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navController.popBackStack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AddTransactionHeader(
            onClose = onClose,
            onDone = { viewModel.saveTransaction() }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TypeSelector(
                selectedType = uiState.selectedType,
                onTypeSelected = { viewModel.setType(it) }
            )

            AmountDisplay(amount = uiState.amount)

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val dateFormatter = remember { SimpleDateFormat("'Today', MMM dd", Locale.getDefault()) }
                val displayDate = uiState.selectedDateMillis?.let {
                    dateFormatter.format(Date(it))
                } ?: dateFormatter.format(Date())

                DateSelector(
                    date = displayDate,
                    onDateClick = { /* TODO: Implement date picker */ }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            CategoryGrid(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.setCategory(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        NumberPad(
            onNumberClick = { digit ->
                viewModel.setAmount(updateAmount(uiState.amount, digit))
            },
            onBackspace = {
                viewModel.setAmount(backspaceAmount(uiState.amount))
            },
            onConfirm = { viewModel.saveTransaction() }
        )

        SnackbarHost(hostState = snackbarHostState)
    }
}

private fun updateAmount(current: String, digit: String): String {
    when (digit) {
        "." -> {
            // 已有小数点则忽略
            if (current.contains(".")) return current
            return current
        }
        "+", "-" -> {
            return current
        }
        else -> {
            // 初始状态 "0.00" 视为无小数
            if (current == "0.00") {
                return "$digit.00"
            }

            // 已有小数点，填充小数部分
            if (current.contains(".")) {
                val parts = current.split(".")
                val intPart = parts[0]
                val decimalPart = parts.getOrElse(1) { "" }

                // 小数部分已达2位则忽略
                if (decimalPart.length >= 2) return current

                // 追加到小数部分
                val newDecimal = (decimalPart + digit).take(2)
                return "$intPart.$newDecimal"
            } else {
                // 无小数点，填充整数部分
                val newInt = (current + digit).takeLast(10)
                return "$newInt.00"
            }
        }
    }
}

private fun backspaceAmount(current: String): String {
    // 仅有 "0.00" 或更短时，重置为 "0.00"
    val numericValue = current.replace(".", "")
    if (numericValue.length <= 3) {
        return "0.00"
    }

    if (current.contains(".")) {
        val parts = current.split(".")
        val intPart = parts[0]
        val decimalPart = parts.getOrElse(1) { "" }

        // 有小数部分且长度>0，删除小数部分的最后一位
        if (decimalPart.isNotEmpty()) {
            val newDecimal = decimalPart.dropLast(1)
            return if (newDecimal.isEmpty()) "$intPart." else "$intPart.$newDecimal"
        }

        // 小数部分为空，删除整数部分的最后一位
        if (intPart.length == 1) {
            return "0.00"
        }
        return "${intPart.dropLast(1)}.00"
    } else {
        // 无小数点，删除整数部分的最后一位
        if (current.length == 1) {
            return "0.00"
        }
        return "${current.dropLast(1)}.00"
    }
}
