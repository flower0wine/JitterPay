package com.example.jitterpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.jitterpay.ui.AddTransactionScreen
import com.example.jitterpay.ui.HomeScreen
import com.example.jitterpay.ui.theme.JitterPayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JitterPayTheme {
                var showAddTransaction by remember { mutableStateOf(false) }
                
                if (showAddTransaction) {
                    AddTransactionScreen(
                        onClose = { showAddTransaction = false },
                        onSave = { type, amount, category, date ->
                            // TODO: Save transaction to database/state
                            println("Transaction saved: $type, $amount, $category, $date")
                        }
                    )
                } else {
                    HomeScreen(
                        onAddTransactionClick = { showAddTransaction = true }
                    )
                }
            }
        }
    }
}
