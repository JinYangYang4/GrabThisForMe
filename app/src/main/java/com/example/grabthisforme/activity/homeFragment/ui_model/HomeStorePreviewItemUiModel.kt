package com.example.grabthisforme.activity.homeFragment.ui_model

import com.example.grabthisforme.model.goods.domain.Goods
import java.util.Locale

data class HomeStorePreviewItemUiModel(
    val goodsId: Long?,
    val title: String,
    val priceText: String,
    val imageUrl: String?,
    val isMoreEntry: Boolean = false
)

fun Goods.toHomeStorePreviewItemUiModel(): HomeStorePreviewItemUiModel {
    return HomeStorePreviewItemUiModel(
        goodsId = id,
        title = name,
        priceText = String.format(Locale.getDefault(), "%.2f", price),
        imageUrl = pic
    )
}

fun createHomeStoreMoreEntryUiModel(): HomeStorePreviewItemUiModel {
    return HomeStorePreviewItemUiModel(
        goodsId = null,
        title = "",
        priceText = "",
        imageUrl = null,
        isMoreEntry = true
    )
}
