package com.example.grabthisforme.activity.fragment_misc.storeFragment.ui_model

data class SelectedGoodsItemUiModel(
    val goodsId: Long,
    val title: String,
    val price: Double,
    val priceText: String,
    val discountText: String,
    val imageUrl: String?,
    val tags: List<String>,
    val selectedCount: Int
)
