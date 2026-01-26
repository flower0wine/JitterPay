package com.example.jitterpay.ui.components.recurring.addrecurring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CategoryOption(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

private val categories = listOf(
    CategoryOption("Transport", Icons.Default.DirectionsCar, Color(0xFF5AC8FA)),
    CategoryOption("Food", Icons.Default.Restaurant, Color(0xFFFF3B30)),
    CategoryOption("Shopping", Icons.Default.ShoppingBag, Color(0xFFAF52DE)),
    CategoryOption("Bills", Icons.Default.Receipt, Color(0xFFFF2D55)),
    CategoryOption("Entertainment", Icons.Default.Movie, Color(0xFFFF9500)),
    CategoryOption("Health", Icons.Default.LocalHospital, Color(0xFF32ADE6)),
    CategoryOption("Income", Icons.Default.AttachMoney, Color(0xFF34C759)),
    CategoryOption("Other", Icons.Default.Category, Color(0xFF8E8E93))
)

@Composable
fun RecurringCategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Category",
            color = Color.Gray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(categories) { category ->
                CategoryItem(
                    category = category,
                    isSelected = selectedCategory == category.name,
                    onClick = { onCategorySelected(category.name) }
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: CategoryOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) 
                        category.color 
                    else 
                        category.color.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = if (isSelected) Color.Black else category.color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
