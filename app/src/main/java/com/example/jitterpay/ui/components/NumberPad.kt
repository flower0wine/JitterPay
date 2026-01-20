package com.example.jitterpay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumberPad(
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: 1, 2, 3, backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberButton("1", onNumberClick, Modifier.weight(1f))
            NumberButton("2", onNumberClick, Modifier.weight(1f))
            NumberButton("3", onNumberClick, Modifier.weight(1f))
            IconButton(
                icon = Icons.Default.Backspace,
                onClick = onBackspace,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Row 2: 4, 5, 6, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberButton("4", onNumberClick, Modifier.weight(1f))
            NumberButton("5", onNumberClick, Modifier.weight(1f))
            NumberButton("6", onNumberClick, Modifier.weight(1f))
            OperatorButton("+", onNumberClick, Modifier.weight(1f))
        }
        
        // Row 3: 7, 8, 9, -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberButton("7", onNumberClick, Modifier.weight(1f))
            NumberButton("8", onNumberClick, Modifier.weight(1f))
            NumberButton("9", onNumberClick, Modifier.weight(1f))
            OperatorButton("-", onNumberClick, Modifier.weight(1f))
        }
        
        // Row 4: ., 0, confirm button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NumberButton(".", onNumberClick, Modifier.weight(1f))
            NumberButton("0", onNumberClick, Modifier.weight(1f))
            ConfirmButton(
                onClick = onConfirm,
                modifier = Modifier.weight(2f)
            )
        }
    }
}

@Composable
private fun NumberButton(
    number: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(number) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun OperatorButton(
    operator: String,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(operator) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = operator,
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun IconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ConfirmButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(2f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Confirm",
            tint = Color.Black,
            modifier = Modifier.size(28.dp)
        )
    }
}
