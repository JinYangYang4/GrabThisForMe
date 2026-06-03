package com.example.grabthisforme.activity.LoginActivity.ui_model

import com.example.grabthisforme.model.user.domain.User

data class SwitchAccountItemUiModel(
    val userId: Long,
    val displayName: String,
    val accountText: String,
    val avatarUrl: String?,
    val isCurrent: Boolean
)

fun User.toSwitchAccountItemUiModel(): SwitchAccountItemUiModel {
    return SwitchAccountItemUiModel(
        userId = id,
        displayName = name,
        accountText = id.toString(),
        avatarUrl = headPic,
        isCurrent = isCurrent
    )
}
