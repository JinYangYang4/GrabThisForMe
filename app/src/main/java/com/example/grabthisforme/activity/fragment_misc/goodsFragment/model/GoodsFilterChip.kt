package com.example.grabthisforme.activity.fragment_misc.goodsFragment.model

data class GoodsFilterChip(
    val id: Long,
    val label: String,
    val type: GoodsFilterType,
    val isSelected: Boolean = false
)

enum class GoodsFilterType {
    ALL,
    STORE_TYPE,
    DISCOUNT,
    HOT,
    READY
}
