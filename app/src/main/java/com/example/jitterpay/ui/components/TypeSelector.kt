package com.example.jitterpay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TypeSelector(
    selectedType: String = "Expense",
    onTypeSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1C1C1E)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (selectedType == "Expense") MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onTypeSelected("Expense") },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Expense",
                color = if (selectedType == "Expense") Color.Black else Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (selectedType == "Income") MaterialTheme.colorScheme.primary else Color.Transparent)
                .clickable { onTypeSelected("Income") },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Income",
                color = if (selectedType == "Income") Color.Black else Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
