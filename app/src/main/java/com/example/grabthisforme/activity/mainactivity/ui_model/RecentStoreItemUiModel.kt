package com.example.grabthisforme.activity.mainactivity.ui_model

import com.example.grabthisforme.model.store.domain.Store

data class RecentStoreItemUiModel(
    val storeId: Long,
    val name: String,
    val badgeText: String,
    val imageUrl: String?
)

fun Store.toRecentStoreItemUiModel(): RecentStoreItemUiModel {
    return RecentStoreItemUiModel(
        storeId = id,
        name = name,
        badgeText = type.ifBlank { "店铺" },
        imageUrl = pic
    )
}
