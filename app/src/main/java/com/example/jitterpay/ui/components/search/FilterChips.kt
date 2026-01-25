package com.example.jitterpay.ui.components.search

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants

@Composable
fun FilterChips(
    selectedType: String?,
    onTypeSelected: (String?) -> Unit,
    selectedDateRange: String?,
    onDateRangeSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "TYPE",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                label = "All",
                selected = selectedType == null,
                onClick = { onTypeSelected(null) }
            )
            FilterChip(
                label = "Income",
                selected = selectedType == "INCOME",
                onClick = {
                    onTypeSelected(if (selectedType == "INCOME") null else "INCOME")
                }
            )
            FilterChip(
                label = "Expense",
                selected = selectedType == "EXPENSE",
                onClick = {
                    onTypeSelected(if (selectedType == "EXPENSE") null else "EXPENSE")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "DATE RANGE",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                label = "Today",
                selected = selectedDateRange == "TODAY",
                onClick = {
                    onDateRangeSelected(if (selectedDateRange == "TODAY") null else "TODAY")
                }
            )
            FilterChip(
                label = "This Week",
                selected = selectedDateRange == "WEEK",
                onClick = {
                    onDateRangeSelected(if (selectedDateRange == "WEEK") null else "WEEK")
                }
            )
            FilterChip(
                label = "This Month",
                selected = selectedDateRange == "MONTH",
                onClick = {
                    onDateRangeSelected(if (selectedDateRange == "MONTH") null else "MONTH")
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.SHORT
            )
        ) + scaleIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.Duration.SHORT
            )
        )
    ) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            modifier = modifier,
            colors = FilterChipDefaults.filterChipColors(
                containerColor = Color(0xFF1C1C1E),
                labelColor = Color.White,
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = Color.Black
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = Color.Transparent,
                selectedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
