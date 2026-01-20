package com.example.jitterpay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.theme.GrayText
import com.example.jitterpay.ui.theme.SurfaceDark

data class Category(
    val name: String,
    val icon: ImageVector
)

@Composable
fun CategoryGrid(
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        Category("Dining", Icons.Default.Restaurant),
        Category("Groceries", Icons.Default.ShoppingCart),
        Category("Travel", Icons.Default.Flight),
        Category("Shopping", Icons.Default.ShoppingBag),
        Category("Health", Icons.Default.Favorite),
        Category("Bills", Icons.Default.Receipt)
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "SELECT CATEGORY",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = GrayText,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categories.chunked(3).forEach { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowCategories.forEach { category ->
                        CategoryItem(
                            category = category,
                            isSelected = selectedCategory == category.name,
                            onClick = { onCategorySelected(category.name) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // Fill remaining space if row is not complete
                    repeat(3 - rowCategories.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = category.name,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = category.name,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else GrayText
        )
    }
}
