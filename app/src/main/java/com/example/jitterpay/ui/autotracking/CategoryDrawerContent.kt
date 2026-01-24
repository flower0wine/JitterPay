package com.example.jitterpay.ui.autotracking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.autotracking.service.CategoryDrawerOverlayService
import com.example.jitterpay.ui.theme.SurfaceDark
import kotlinx.coroutines.flow.StateFlow

/**
 * Category data class for transaction categories
 */
data class Category(
    val name: String,
    val icon: ImageVector
)

/**
 * Category drawer content displayed in overlay
 *
 * @param transactionData The transaction data to display
 * @param onCategorySelected Callback when user selects a category
 * @param onCancel Callback when user cancels
 */
@Composable
fun CategoryDrawerContent(
    transactionData: StateFlow<CategoryDrawerOverlayService.TransactionData?>,
    onCategorySelected: (String) -> Unit,
    onCancel: () -> Unit
) {
    val data: CategoryDrawerOverlayService.TransactionData? by transactionData.collectAsState()
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // 默认选中第一项
    LaunchedEffect(Unit) {
        if (selectedCategory == null) {
            selectedCategory = "Dining"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onCancel), // Dim background to cancel
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {} // Prevent clicks from passing through
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Transaction details header
                val transaction = data
                if (transaction != null) {
                    TransactionHeader(
                        amount = transaction.amount,
                        paymentMethod = transaction.paymentMethod,
                        description = transaction.description
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Category selection title
                Text(
                    text = "SELECT CATEGORY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category grid
                CategoryGrid(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { category ->
                        selectedCategory = category
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            selectedCategory?.let { onCategorySelected(it) }
                        },
                        enabled = selectedCategory != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Transaction")
                    }
                }
            }
        }
    }
}

/**
 * Transaction header displaying amount and payment details
 */
@Composable
private fun TransactionHeader(
    amount: String,
    paymentMethod: String,
    description: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Transaction Detected",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "$${amount}",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = paymentMethod,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Category grid for selection
 */
@Composable
private fun CategoryGrid(
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        Category("Dining", Icons.Default.Restaurant),
        Category("Groceries", Icons.Default.ShoppingCart),
        Category("Travel", Icons.Default.Flight),
        Category("Shopping", Icons.Default.ShoppingBag),
        Category("Health", Icons.Default.Favorite),
        Category("Bills", Icons.Default.Receipt)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
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

/**
 * Individual category item
 */
@Composable
private fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
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
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
