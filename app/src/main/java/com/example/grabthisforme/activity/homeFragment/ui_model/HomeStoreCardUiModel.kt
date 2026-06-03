package com.example.grabthisforme.activity.homeFragment.ui_model

data class HomeStoreCardUiModel(
    val storeId: Long,
    val storeName: String,
    val salesText: String,
    val distanceText: String,
    val storeImageUrl: String?,
    val previewGoods: List<HomeStorePreviewItemUiModel>
)
