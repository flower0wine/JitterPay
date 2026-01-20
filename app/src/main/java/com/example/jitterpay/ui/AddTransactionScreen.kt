package com.example.jitterpay.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jitterpay.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddTransactionScreen(
    onClose: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var amount by remember { mutableStateOf("0.00") }
    var selectedType by remember { mutableStateOf("Expense") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { 
        mutableStateOf(
            SimpleDateFormat("'Today', MMM dd", Locale.getDefault()).format(Date())
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AddTransactionHeader(
            onClose = onClose,
            onDone = {
                if (selectedCategory != null && amount != "0.00") {
                    onSave(selectedType, amount, selectedCategory!!, selectedDate)
                    onClose()
                }
            }
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            TypeSelector(
                selectedType = selectedType,
                onTypeSelected = { selectedType = it }
            )
            
            AmountDisplay(amount = amount)
            
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                DateSelector(
                    date = selectedDate,
                    onDateClick = { /* TODO: Implement date picker */ }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            CategoryGrid(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        NumberPad(
            onNumberClick = { digit ->
                amount = updateAmount(amount, digit)
            },
            onBackspace = {
                amount = backspaceAmount(amount)
            },
            onConfirm = {
                if (selectedCategory != null && amount != "0.00") {
                    onSave(selectedType, amount, selectedCategory!!, selectedDate)
                    onClose()
                }
            }
        )
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
