package com.example.jitterpay.ui.components.statistics

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants

@Composable
fun CategoryBreakdownList(
    categories: List<CategorySpending>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "CATEGORY BREAKDOWN",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (categories.isEmpty()) {
            EmptyStateView()
        } else {
            categories.forEachIndexed { index, category ->
                CategoryBreakdownItem(
                    icon = getCategoryIcon(category.name),
                    iconBackgroundColor = category.color,
                    categoryName = category.name,
                    percentage = category.percentage,
                    amount = category.amount,
                    animationDelay = AnimationConstants.Stagger.listItemDelay(index)
                )

                if (index < categories.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(600)) + scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = "No data",
                tint = Color(0xFF5E5E5E),
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Transactions Yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Start tracking your expenses to see\ndetailed statistics and insights",
                fontSize = 14.sp,
                color = Color(0xFF8E8E93),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

private fun getCategoryIcon(categoryName: String) = when (categoryName.lowercase()) {
    "food & drinks" -> Icons.Default.Restaurant
    "transport" -> Icons.Default.DirectionsCar
    "utilities" -> Icons.Default.Bolt
    "entertainment" -> Icons.Default.Movie
    "shopping" -> Icons.Default.ShoppingBag
    "health" -> Icons.Default.LocalHospital
    else -> Icons.Default.Category
}
