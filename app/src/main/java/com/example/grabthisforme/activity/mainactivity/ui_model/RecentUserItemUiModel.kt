package com.example.grabthisforme.activity.mainactivity.ui_model

import com.example.grabthisforme.model.user.domain.User

data class RecentUserItemUiModel(
    val userId: Long,
    val name: String,
    val badgeText: String,
    val imageUrl: String?
)

fun User.toRecentUserItemUiModel(): RecentUserItemUiModel {
    return RecentUserItemUiModel(
        userId = id,
        name = name,
        badgeText = "常联系",
        imageUrl = headPic
    )
}
