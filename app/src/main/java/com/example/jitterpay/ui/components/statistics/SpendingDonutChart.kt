package com.example.jitterpay.ui.components.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.theme.GrayText

data class SpendingData(
    val totalSpent: Double,
    val percentageChange: Double,
    val categories: List<CategorySpending>
)

data class CategorySpending(
    val name: String,
    val amount: Double,
    val percentage: Double,
    val color: Color
)

@Composable
fun SpendingDonutChart(
    data: SpendingData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Donut Chart
        Canvas(
            modifier = Modifier.size(240.dp)
        ) {
            val strokeWidth = 40.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeft = Offset(
                x = (size.width - radius * 2 - strokeWidth) / 2,
                y = (size.height - radius * 2 - strokeWidth) / 2
            )
            val chartSize = Size(radius * 2 + strokeWidth, radius * 2 + strokeWidth)
            
            // Background circle (dark gray)
            drawArc(
                color = Color(0xFF2C2C2E),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = chartSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Draw category arcs
            var startAngle = -90f
            data.categories.forEach { category ->
                val sweepAngle = (category.percentage.toFloat() / 100f) * 360f
                drawArc(
                    color = category.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = chartSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }
        
        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TOTAL SPENT",
                fontSize = 10.sp,
                color = GrayText,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$${String.format("%.2f", data.totalSpent)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${if (data.percentageChange >= 0) "↑" else "↓"} ${String.format("%.1f", kotlin.math.abs(data.percentageChange))}%",
                fontSize = 12.sp,
                color = if (data.percentageChange >= 0) Color(0xFFFF3B30) else Color(0xFF34C759),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
