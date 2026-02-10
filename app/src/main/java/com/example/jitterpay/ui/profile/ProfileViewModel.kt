package com.example.jitterpay.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.data.model.UserAvatar
import com.example.jitterpay.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Profile界面的ViewModel
 *
 * 管理用户头像等个人信息
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    /**
     * 用户头像
     *
     * 统一返回 UserAvatar，调用方无需关心是默认还是自定义
     */
    val avatar: StateFlow<UserAvatar> = userPreferencesRepository.getUserAvatar()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserAvatar.Default(com.example.jitterpay.R.drawable.avatar_1)
        )
}
