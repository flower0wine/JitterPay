package com.example.jitterpay.ui.components.goals.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.goals.GoalData

@Composable
fun GoalActionsSection(
    goal: GoalData,
    onAddFunds: () -> Unit,
    onWithdraw: () -> Unit,
    onEditGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "QUICK ACTIONS",
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!goal.isCompleted) {
                ActionButton(
                    icon = Icons.Default.Add,
                    label = "Add Funds",
                    onClick = onAddFunds,
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                )
            }

            if (goal.currentAmount > 0) {
                ActionButton(
                    icon = Icons.Default.Remove,
                    label = "Withdraw",
                    onClick = onWithdraw,
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )
            }

            ActionButton(
                icon = Icons.Default.Edit,
                label = "Edit",
                onClick = onEditGoal,
                isPrimary = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isPrimary)
                MaterialTheme.colorScheme.primary
            else
                Color(0xFF1C1C1E)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPrimary) Color.Black else Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = if (isPrimary) Color.Black else Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
