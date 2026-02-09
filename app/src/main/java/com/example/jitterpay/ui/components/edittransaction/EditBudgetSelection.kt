package com.example.jitterpay.ui.components.edittransaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

/**
 * 编辑交易时的预算选择组件
 *
 * 显示当前关联的预算，点击可跳转到预算选择页面
 */
@Composable
fun EditBudgetSelection(
    budgetId: Long?,
    hasBudgets: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = hasBudgets, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C1E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标
                Text(
                    text = "💰",
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Budget",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    if (budgetId != null) {
                        Text(
                            text = "Linked to budget",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = "No budget linked",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (hasBudgets) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Select budget",
                    tint = Color.Gray
                )
            }
        }
    }
}
