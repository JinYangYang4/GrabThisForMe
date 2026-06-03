package com.example.grabthisforme.model.goods.domain

data class GoodsBaseInfo(
    val id: Long,
    val storeId: Long = 0L,
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
    val pic: String = "",
    val tag: String = "",
    val unit: String = ""
)

data class GoodsStateInfo(
    val saleNumber: Long = 0,
    val stock: Int = 0,
    val isSoldOut: Boolean = false,
    val isHot: Boolean = false,
    val purchaseStatus: Int = PURCHASE_STATUS_NO_PURCHASE,
    val soldCount: Long = 0L
) {
    companion object {
        const val PURCHASE_STATUS_NO_PURCHASE = 0
        const val PURCHASE_STATUS_SOLD_PARTIAL = 1
        const val PURCHASE_STATUS_SOLD_OUT = 2
    }
}
