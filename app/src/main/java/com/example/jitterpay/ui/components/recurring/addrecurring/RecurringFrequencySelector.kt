package com.example.jitterpay.ui.components.recurring.addrecurring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.recurring.RecurringFrequency

@Composable
fun RecurringFrequencySelector(
    selectedFrequency: RecurringFrequency,
    onFrequencySelected: (RecurringFrequency) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Frequency",
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RecurringFrequency.entries.forEach { frequency ->
                FrequencyItem(
                    frequency = frequency,
                    isSelected = selectedFrequency == frequency,
                    onClick = { onFrequencySelected(frequency) }
                )
            }
        }
    }
}

@Composable
private fun FrequencyItem(
    frequency: RecurringFrequency,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else 
                    Color(0xFF1C1C1E)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = frequency.displayName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = getFrequencyDescription(frequency),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun getFrequencyDescription(frequency: RecurringFrequency): String {
    return when (frequency) {
        RecurringFrequency.DAILY -> "Every day"
        RecurringFrequency.WEEKLY -> "Once a week"
        RecurringFrequency.BIWEEKLY -> "Every two weeks"
        RecurringFrequency.MONTHLY -> "Once a month"
        RecurringFrequency.YEARLY -> "Once a year"
    }
}
