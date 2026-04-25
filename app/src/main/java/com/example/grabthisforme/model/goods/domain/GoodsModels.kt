package com.example.grabthisforme.model.goods.domain

data class GoodsBaseInfo(
    val id: Long,
    val name: String = "",
    val message: String = "",
    val category: Goods.GoodsCategory? = null
)

data class GoodsPriceInfo(
    val price: Double = 0.0,
    val discountPrice: Double = 0.0,
    val discountTag: String = ""
)

data class GoodsUiInfo(
    val pic: String = "",         // 商品图片链接
    val tag: String = "",        // 商品标签（热销/新品/爆款）
    val unit: String = "",       // 单位（件/瓶/盒/斤）
    val selectedCount: Int = 0  // 购物车选中的数量
)

data class GoodsStateInfo(
    val saleNumber: Long = 0,      // 销量
    val stock: Int = 0,            // 库存数量
    val isSoldOut: Boolean = false,// 是否售罄
    val isHot: Boolean = false     // 是否热销商品
)

