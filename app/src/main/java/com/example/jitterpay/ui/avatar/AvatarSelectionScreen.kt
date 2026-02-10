package com.example.jitterpay.ui.avatar

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.jitterpay.R
import com.example.jitterpay.data.model.UserAvatar
import com.example.jitterpay.ui.components.avatar.AvatarGrid
import com.example.jitterpay.navigation.LocalNavController
import com.example.jitterpay.ui.theme.NeonLime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarSelectionScreen(
    viewModel: AvatarSelectionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val navController = LocalNavController.current
    val selectedAvatar by viewModel.selectedAvatar.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val context = LocalContext.current

    // 注册图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            viewModel.selectCustomAvatar(selectedUri)
        }
    }

    val availableAvatars = listOf(
        R.drawable.avatar_1,
        R.drawable.avatar_2,
        R.drawable.avatar_3,
        R.drawable.avatar_4,
        R.drawable.avatar_5,
        R.drawable.avatar_6,
        R.drawable.avatar_7,
        R.drawable.avatar_8
    )

    // 检查当前是否是自定义头像
    val isUsingCustomAvatar = selectedAvatar is UserAvatar.Custom

    // 获取选中的默认头像ID（如果没有选中任何默认头像则为 null）
    val selectedDefaultAvatarId: Int? = when (val avatar = selectedAvatar) {
        is UserAvatar.Default -> avatar.resourceId
        else -> null
    }

    // 获取自定义头像的文件路径
    val customAvatarPath = when (val avatar = selectedAvatar) {
        is UserAvatar.Custom -> avatar.uri
        else -> null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Choose Avatar",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Select your profile avatar",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AvatarGrid(
                avatars = availableAvatars,
                selectedAvatarId = selectedDefaultAvatarId,
                onAvatarSelected = { viewModel.selectDefaultAvatar(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 自定义头像选择区域
            Text(
                text = "Or choose from your photos",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 自定义头像预览和选择按钮
            CustomAvatarSection(
                customAvatarPath = customAvatarPath,
                isSelected = isUsingCustomAvatar,
                onSelectClick = {
                    imagePickerLauncher.launch("image/*")
                },
                onClearClick = { viewModel.clearCustomAvatar() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.saveAvatar {
                        navController.navigateUp()
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonLime,
                    contentColor = Color.Black
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Save Avatar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CustomAvatarSection(
    customAvatarPath: String?,
    isSelected: Boolean,
    onSelectClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 自定义头像预览
        Box(
            modifier = Modifier
                .size(80.dp)
                .clickable(onClick = onSelectClick),
            contentAlignment = Alignment.Center
        ) {
            // 裁剪的 Image 区域
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(
                        width = if (isSelected) 4.dp else 2.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            ) {
                if (customAvatarPath != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(customAvatarPath)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = "Custom avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        // 清除按钮 - 溢出到头像右上角
        if (customAvatarPath != null) {
            Box(
                modifier = Modifier
                    .offset(x = (-16).dp, y = (-24).dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
                    .clickable(onClick = onClearClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove custom avatar",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 说明文字和选择按钮
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (customAvatarPath != null) "Custom avatar" else "Add your own photo",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (customAvatarPath == null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap to select from gallery",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // 重新选择按钮
        if (customAvatarPath != null) {
            TextButton(onClick = onSelectClick) {
                Text("Change")
            }
        }
    }
}
