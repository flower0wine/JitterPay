package com.example.jitterpay.data.model

import androidx.annotation.DrawableRes

/**
 * 用户头像的数据模型
 *
 * 统一封装默认头像和自定义头像，隐藏实现细节
 */
sealed class UserAvatar {

    /**
     * 默认头像
     *
     * @param resourceId 头像资源ID
     */
    data class Default(
        @DrawableRes val resourceId: Int
    ) : UserAvatar()

    /**
     * 自定义头像
     *
     * @param uri 图片的 URI
     */
    data class Custom(
        val uri: String
    ) : UserAvatar()

    companion object {
        /**
         * 默认头像（当用户未选择任何头像时使用）
         */
        val DEFAULT = Default(
            resourceId = com.example.jitterpay.R.drawable.avatar_1
        )
    }
}
