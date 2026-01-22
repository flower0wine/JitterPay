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
    // Remove leading zeros and handle decimal point
    val cleaned = current.replace(".", "")
    
    when (digit) {
        "." -> {
            // Decimal point is already handled in the format
            return current
        }
        "+", "-" -> {
            // TODO: Handle operators for calculations
            return current
        }
        else -> {
            val newValue = if (cleaned == "000") {
                digit.padStart(3, '0')
            } else {
                (cleaned + digit).takeLast(10) // Limit to reasonable length
            }
            
            // Format as currency with 2 decimal places
            val intPart = newValue.dropLast(2).ifEmpty { "0" }
            val decimalPart = newValue.takeLast(2)
            
            return "$intPart.$decimalPart"
        }
    }
}

private fun backspaceAmount(current: String): String {
    val cleaned = current.replace(".", "")
    
    if (cleaned.length <= 3) {
        return "0.00"
    }
    
    val newValue = cleaned.dropLast(1)
    val intPart = newValue.dropLast(2).ifEmpty { "0" }
    val decimalPart = newValue.takeLast(2)
    
    return "$intPart.$decimalPart"
}
