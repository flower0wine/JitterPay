package com.example.jitterpay.ui.components.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants

/**
 * Profile action buttons with staggered entrance animations
 *
 * @param onSwitchAccount Switch account button click handler
 * @param onSignOut Sign out button click handler
 * @param modifier Modifier for the column container
 */
@Composable
fun ProfileActions(
    onSwitchAccount: () -> Unit = {},
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.MEDIUM,
                    delayMillis = AnimationConstants.Stagger.QUICK_ACTIONS[0],
                    easing = AnimationConstants.Easing.Entrance
                ),
                initialScale = 0.8f
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.SHORT,
                    delayMillis = AnimationConstants.Stagger.QUICK_ACTIONS[0],
                    easing = AnimationConstants.Easing.Entrance
                )
            ),
            label = "switchAccountButton"
        ) {
            Button(
                onClick = onSwitchAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Switch Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = true,
            enter = scaleIn(
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.MEDIUM,
                    delayMillis = AnimationConstants.Stagger.QUICK_ACTIONS[1],
                    easing = AnimationConstants.Easing.Entrance
                ),
                initialScale = 0.8f
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.SHORT,
                    delayMillis = AnimationConstants.Stagger.QUICK_ACTIONS[1],
                    easing = AnimationConstants.Easing.Entrance
                )
            ),
            label = "signOutButton"
        ) {
            TextButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Sign Out",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Red
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.SHORT,
                    delayMillis = AnimationConstants.Stagger.QUICK_ACTIONS[2],
                    easing = AnimationConstants.Easing.Entrance
                )
            ),
            label = "versionText"
        ) {
            Text(
                text = "Version 2.4.1 (Build 1082)",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

