package com.example.jitterpay.ui.components.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jitterpay.ui.animation.AnimationConstants

/**
 * Profile header with staggered entrance animations for smooth appearance
 *
 * @param userName User's display name
 * @param userEmail User's email address
 * @param isPro Whether user has pro plan
 * @param modifier Modifier for column container
 */
@Composable
fun ProfileHeader(
    userName: String,
    userEmail: String,
    isPro: Boolean = true,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Avatar scale animation
    val avatarScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "avatarScale"
    )

    // Edit button animation delay
    val editButtonAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.SHORT,
            delayMillis = 100,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "editButtonAlpha"
    )

    // User name slide animation
    val userNameAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.SHORT,
            delayMillis = 100,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "userNameAlpha"
    )

    val userNameOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 30f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.MEDIUM,
            delayMillis = 100,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "userNameOffset"
    )

    // User email animation delay
    val userEmailAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.SHORT,
            delayMillis = 150,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "userEmailAlpha"
    )

    val userEmailOffset by animateFloatAsState(
        targetValue = if (isVisible) 0f else 30f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.MEDIUM,
            delayMillis = 150,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "userEmailOffset"
    )

    // Pro badge animation delay
    val proBadgeAlpha by animateFloatAsState(
        targetValue = if (isVisible && isPro) 1f else 0f,
        animationSpec = tween(
            durationMillis = AnimationConstants.Duration.SHORT,
            delayMillis = 200,
            easing = AnimationConstants.Easing.Entrance
        ),
        label = "proBadgeAlpha"
    )

    val proBadgeScale by animateFloatAsState(
        targetValue = if (isVisible && isPro) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "proBadgeScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar container with scale animation
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = AnimationConstants.Duration.MEDIUM,
                    easing = AnimationConstants.Easing.Entrance
                )
            ),
            label = "avatarContainer"
        ) {
            Box(
                modifier = Modifier.scale(avatarScale),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                )

                // Edit button with staggered appearance
                Box(
                    modifier = Modifier.alpha(editButtonAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User name with slide up and fade
        Text(
            text = userName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .alpha(userNameAlpha)
                .offset(y = userNameOffset.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // User email with slide up and fade (staggered)
        Text(
            text = userEmail,
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier
                .alpha(userEmailAlpha)
                .offset(y = userEmailOffset.dp)
        )

        // Pro badge with scale animation (conditional)
        if (isPro) {
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.alpha(proBadgeAlpha).scale(proBadgeScale),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "PRO PLAN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}
