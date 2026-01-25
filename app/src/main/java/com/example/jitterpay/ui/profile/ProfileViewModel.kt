package com.example.jitterpay.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jitterpay.R
import com.example.jitterpay.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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
     * 用户头像ID
     * 如果未设置，默认使用 avatar_1
     */
    val avatarId: StateFlow<Int> = userPreferencesRepository.getAvatarId()
        .map { savedId ->
            if (savedId == -1) R.drawable.avatar_1 else savedId
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = R.drawable.avatar_1
        )
}
