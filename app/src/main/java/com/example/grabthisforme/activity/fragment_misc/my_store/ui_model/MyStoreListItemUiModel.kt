package com.example.grabthisforme.activity.fragment_misc.my_store.ui_model

import com.example.grabthisforme.model.store.domain.Store

data class MyStoreListItemUiModel(
    val storeId: Long,
    val storeName: String,
    val businessHoursText: String,
    val addressText: String,
    val salesText: String,
    val imageUrl: String?
)

fun Store.toMyStoreListItemUiModel(): MyStoreListItemUiModel {
    return MyStoreListItemUiModel(
        storeId = id,
        storeName = name,
        businessHoursText = businessHours
            ?.takeIf { it.isNotBlank() }
            ?.let { "营业时间：$it" }
            ?: "营业时间：暂未设置",
        addressText = address,
        salesText = "销量：$salesVolume",
        imageUrl = pic
    )
}
