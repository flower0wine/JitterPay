package com.example.jitterpay.ui.components.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants

/**
 * Position of the sub-item in the list
 */
enum class SubItemPosition {
    FIRST,      // First item - line starts from middle
    MIDDLE,     // Middle item - line goes through
    LAST        // Last item - line ends at middle
}

/**
 * Draws a connector line on the left side of sub-items
 * Creates a visual hierarchy showing items belong to a parent setting
 * Uses rounded corners for smooth right-angle turns
 *
 * @param position Position of the item in the list
 * @param lineColor Color of the connector line
 * @param lineWidth Width of the connector line
 * @param horizontalBranchLength Length of the horizontal branch
 * @param cornerRadius Radius of the rounded corner where vertical meets horizontal
 * @param firstItemTopExtension Extra height for first item to connect with parent visually
 */
@Composable
fun SubItemConnector(
    position: SubItemPosition,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFFCDDC39), // Yellow-green color from image
    lineWidth: Dp = 3.dp,
    horizontalBranchLength: Dp = 13.dp,
    cornerRadius: Dp = 6.dp,
    firstItemTopExtension: Dp = 5.dp
) {
    Canvas(
        modifier = modifier
            .width(32.dp)
            .fillMaxHeight()
    ) {
        val canvasHeight = size.height
        val strokeWidth = lineWidth.toPx()
        val branchLength = horizontalBranchLength.toPx()
        val radius = cornerRadius.toPx()
        val topExtension = firstItemTopExtension.toPx()
        
        // Vertical line X position with offset from left edge for aesthetics
        val leftOffset = 20.dp.toPx()
        val verticalX = leftOffset
        val centerY = canvasHeight / 2
        
        // Horizontal branch end position
        val horizontalEndX = verticalX + branchLength
        
        // Vertical line start and end points
        val verticalTop = when (position) {
            SubItemPosition.FIRST -> -topExtension // Extend upward to connect with parent
            SubItemPosition.MIDDLE -> -strokeWidth
            SubItemPosition.LAST -> -strokeWidth
        }
        val verticalBottom = when (position) {
            SubItemPosition.FIRST -> canvasHeight + strokeWidth + radius
            SubItemPosition.MIDDLE -> canvasHeight + strokeWidth
            SubItemPosition.LAST -> centerY
        }
        
        // Draw the connector using Path with rounded corner
        val path = Path().apply {
            // Start from the top of vertical line
            moveTo(verticalX, verticalTop)
            
            // Draw vertical line down to the point before the corner
            lineTo(verticalX, centerY - radius)
            
            // Draw rounded corner using quadraticBezierTo
            // Control point is at the sharp corner position
            // End point is at the start of horizontal line
            quadraticBezierTo(
                x1 = verticalX,           // Control point X (at the corner)
                y1 = centerY,             // Control point Y (at the corner)
                x2 = verticalX + radius,  // End point X (start of horizontal)
                y2 = centerY              // End point Y (at center)
            )
            
            // Draw horizontal branch to the right
            lineTo(horizontalEndX, centerY)
        }
        
        // Draw the main path (top vertical + corner + horizontal)
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        
        // Draw bottom vertical line separately if needed
        if (centerY < verticalBottom) {
            drawLine(
                color = lineColor,
                start = Offset(verticalX, centerY - radius),
                end = Offset(verticalX, verticalBottom),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Container for sub-settings items with connector line decoration
 * Provides consistent height and visual hierarchy
 *
 * @param position Position of the item in the list
 * @param showConnector Whether to show the connector line
 * @param content Content of the sub-item
 */
@Composable
fun SubSettingsItemContainer(
    position: SubItemPosition,
    modifier: Modifier = Modifier,
    showConnector: Boolean = true,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp), // Fixed height to ensure connector line has proper height
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showConnector) {
            // Connector line on the left
            SubItemConnector(
                position = position,
                modifier = Modifier.fillMaxHeight()
            )
            
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            // Placeholder spacing when connector is hidden
            Spacer(modifier = Modifier.width(40.dp))
        }
        
        // Content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            content()
        }
    }
}

/**
 * Permission status indicator styled as a sub-item
 * Shows permission status with icon and enable button when not granted
 *
 * @param title Permission title text
 * @param isGranted Whether permission is granted
 * @param onSettingsClick Settings button click handler
 */
@Composable
fun PermissionSubItem(
    title: String,
    isGranted: Boolean,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp), // Fixed height for consistency
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Status icon with color based on permission state
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isGranted) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Enable button - fixed width container to prevent layout shift
            Row(
                modifier = Modifier.width(80.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = !isGranted,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = AnimationConstants.Duration.SHORT,
                            easing = AnimationConstants.Easing.Entrance
                        )
                    ) + scaleIn(
                        animationSpec = tween(
                            durationMillis = AnimationConstants.Duration.SHORT,
                            easing = AnimationConstants.Easing.Entrance
                        )
                    ),
                    exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = AnimationConstants.Duration.MICRO,
                            easing = AnimationConstants.Easing.Exit
                        )
                    ) + scaleOut(
                        animationSpec = tween(
                            durationMillis = AnimationConstants.Duration.MICRO,
                            easing = AnimationConstants.Easing.Exit
                        )
                    ),
                    label = "enableButton"
                ) {
                    TextButton(
                        onClick = onSettingsClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Enable",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
