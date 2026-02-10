package com.example.jitterpay.ui.avatar

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.R
import com.example.jitterpay.data.local.AvatarFileManager
import com.example.jitterpay.data.model.UserAvatar
import com.example.jitterpay.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 头像选择界面的ViewModel
 *
 * 管理头像选择的状态和业务逻辑
 */
@HiltViewModel
class AvatarSelectionViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val avatarFileManager: AvatarFileManager
) : ViewModel() {

    private val _selectedAvatar = MutableStateFlow<UserAvatar>(UserAvatar.Default(R.drawable.avatar_1))
    val selectedAvatar: StateFlow<UserAvatar> = _selectedAvatar.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private var currentCustomAvatarPath: String? = null

    init {
        loadSavedAvatar()
    }

    /**
     * 加载已保存的头像
     */
    private fun loadSavedAvatar(): Unit {
        viewModelScope.launch {
            userPreferencesRepository.getUserAvatar().collect { avatar ->
                _selectedAvatar.value = avatar
            }
        }
    }

    /**
     * 选择默认头像
     *
     * @param avatarId 头像资源ID
     */
    fun selectDefaultAvatar(avatarId: Int): Unit {
        _selectedAvatar.value = UserAvatar.Default(avatarId)
    }

    /**
     * 选择自定义本地图片作为头像
     *
     * 将用户选择的图片复制到内部存储
     *
     * @param uri 图片的 URI
     */
    fun selectCustomAvatar(uri: Uri): Unit {
        val result = avatarFileManager.copyAvatarToInternalStorage(uri)
        result.onSuccess { filePath ->
            currentCustomAvatarPath = filePath
            _selectedAvatar.value = UserAvatar.Custom(filePath)
        }
    }

    /**
     * 清除自定义头像，恢复选择默认头像
     */
    fun clearCustomAvatar(): Unit {
        currentCustomAvatarPath?.let { path ->
            avatarFileManager.deleteAvatarFile(path)
        }
        currentCustomAvatarPath = null
        _selectedAvatar.value = UserAvatar.Default(R.drawable.avatar_1)
    }

    /**
     * 保存选择的头像
     *
     * @param onSuccess 保存成功的回调
     */
    fun saveAvatar(onSuccess: () -> Unit): Unit {
        viewModelScope.launch {
            _isSaving.value = true
            try {
                when (val avatar = _selectedAvatar.value) {
                    is UserAvatar.Default -> {
                        userPreferencesRepository.saveAvatarId(avatar.resourceId)
                        userPreferencesRepository.saveCustomAvatarPath("")
                        avatarFileManager.clearAllAvatars()
                    }
                    is UserAvatar.Custom -> {
                        val newPath = avatar.uri
                        userPreferencesRepository.saveCustomAvatarPath(newPath)
                        userPreferencesRepository.saveAvatarId(0)
                        // 清理旧文件
                        avatarFileManager.cleanupOldAvatars(newPath)
                    }
                }
                onSuccess()
            } finally {
                _isSaving.value = false
            }
        }
    }
}