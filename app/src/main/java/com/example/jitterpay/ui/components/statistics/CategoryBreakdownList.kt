package com.example.jitterpay.ui.components.statistics

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

private fun getCategoryIcon(categoryName: String) = when (categoryName.lowercase()) {
    "food & drinks" -> Icons.Default.Restaurant
    "transport" -> Icons.Default.DirectionsCar
    "utilities" -> Icons.Default.Bolt
    "entertainment" -> Icons.Default.Movie
    "shopping" -> Icons.Default.ShoppingBag
    "health" -> Icons.Default.LocalHospital
    else -> Icons.Default.Category
}
