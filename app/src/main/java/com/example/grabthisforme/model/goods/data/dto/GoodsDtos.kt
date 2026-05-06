package com.example.grabthisforme.model.goods.data.dto

data class GoodsBaseDto(
    val id: Long,
    val name: String = "",
    val message: String = "",
    val category: String? = null
)

data class GoodsPriceDto(
    val price: Double = 0.0,
    val discountPrice: Double = 0.0,
    val discountTag: String = ""
)

data class GoodsUiDto(
    val pic: String = "",
    val tag: String = "",
    val unit: String = "",
    val selectedCount: Int = 0
)

data class GoodsStateDto(
    val saleNumber: Long = 0,
    val stock: Int = 0,
    val isSoldOut: Boolean = false,
    val isHot: Boolean = false,
    val purchaseStatus: Int = 0,
    val soldCount: Long = 0L
)

data class GoodsDto(
    val base: GoodsBaseDto,
    val price: GoodsPriceDto = GoodsPriceDto(),
    val ui: GoodsUiDto = GoodsUiDto(),
    val state: GoodsStateDto = GoodsStateDto()
)
