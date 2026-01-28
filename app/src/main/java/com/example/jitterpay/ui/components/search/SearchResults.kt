package com.example.jitterpay.ui.components.search

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.constants.NavigationRoutes
import com.example.jitterpay.data.local.entity.TransactionEntity
import com.example.jitterpay.navigation.LocalNavController
import com.example.jitterpay.ui.animation.AnimationConstants
import com.example.jitterpay.ui.components.home.TransactionItem

@Composable
fun SearchResults(
    transactions: List<TransactionEntity>,
    searchQuery: String,
    selectedType: String?,
    selectedDateRange: String?,
    modifier: Modifier = Modifier
) {
    val navController = LocalNavController.current

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RESULTS",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (transactions.isNotEmpty()) {
                Text(
                    text = "${transactions.size} found",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isBlank() && selectedType == null && selectedDateRange == null) {
            EmptySearchState(
                message = "Start typing or apply filters",
                subtitle = "Search by amount, category, or description"
            )
        } else if (transactions.isEmpty()) {
            EmptySearchState(
                message = "No transactions found",
                subtitle = "Try adjusting your search or filters"
            )
        } else {
            transactions.forEachIndexed { index, transaction ->
                key(transaction.id) {
                    var isVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        isVisible = true
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInHorizontally(
                            initialOffsetX = { it / 4 },
                            animationSpec = tween(
                                durationMillis = AnimationConstants.Duration.MEDIUM,
                                delayMillis = AnimationConstants.Stagger.listItemDelay(index),
                                easing = AnimationConstants.Easing.Entrance
                            )
                        ) + fadeIn(
                            animationSpec = tween(
                                durationMillis = AnimationConstants.Duration.MEDIUM,
                                delayMillis = AnimationConstants.Stagger.listItemDelay(index)
                            )
                        ),
                        label = "search_result_${transaction.id}"
                    ) {
                        TransactionItem(
                            transaction = transaction,
                            onDelete = { /* TODO: Implement delete */ },
                            onClick = { navController.navigate(NavigationRoutes.editTransaction(transaction.id)) }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptySearchState(
    message: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}
