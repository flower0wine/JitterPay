package com.example.jitterpay.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.theme.GrayText

@Composable
fun AmountDisplay(
    amount: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ENTER AMOUNT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = GrayText,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "$$amount",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
