package com.example.jitterpay.ui.avatar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.R
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
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _selectedAvatarId = MutableStateFlow(R.drawable.avatar_1)
    val selectedAvatarId: StateFlow<Int> = _selectedAvatarId.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        loadSavedAvatar()
    }

    /**
     * 加载已保存的头像
     */
    private fun loadSavedAvatar(): Unit {
        viewModelScope.launch {
            userPreferencesRepository.getAvatarId().collect { avatarId ->
                if (avatarId != -1) {
                    _selectedAvatarId.value = avatarId
                }
            }
        }
    }

    /**
     * 选择头像
     *
     * @param avatarId 头像资源ID
     */
    fun selectAvatar(avatarId: Int): Unit {
        _selectedAvatarId.value = avatarId
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
                userPreferencesRepository.saveAvatarId(_selectedAvatarId.value)
                onSuccess()
            } finally {
                _isSaving.value = false
            }
        }
    }
}
