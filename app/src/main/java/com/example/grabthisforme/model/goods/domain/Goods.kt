package com.example.grabthisforme.model.goods.domain

import com.example.grabthisforme.model.goods.data.mock.GoodsSampleData

open class Goods(
    val baseInfo: GoodsBaseInfo,
    var priceInfo: GoodsPriceInfo = GoodsPriceInfo(),
    var uiInfo: GoodsUiInfo = GoodsUiInfo(),
    var stateInfo: GoodsStateInfo = GoodsStateInfo()
) {

    val id: Long get() = baseInfo.id
    val storeId: Long get() = baseInfo.storeId
    val name: String get() = baseInfo.name
    val message: String get() = baseInfo.message
    val category: GoodsCategory? get() = baseInfo.category
    val price: Double get() = priceInfo.price
    val discountPrice: Double get() = priceInfo.discountPrice
    val discountTag: String get() = priceInfo.discountTag
    val pic: String get() = uiInfo.pic
    val tag: String get() = uiInfo.tag
    val unit: String get() = uiInfo.unit
    val saleNumber: Long get() = stateInfo.saleNumber
    val sale_number: Long get() = saleNumber
    val stock: Int get() = stateInfo.stock
    val isSoldOut: Boolean get() = stateInfo.isSoldOut
    val isHot: Boolean get() = stateInfo.isHot
    val purchaseStatus: Int get() = stateInfo.purchaseStatus
    val soldCount: Long get() = stateInfo.soldCount

    constructor(
        id: Long,
        storeId: Long = 0L,
        name: String = "",
        message: String = "",
        price: Double = 0.0,
        sale_number: Long = 0,
        pic: String = "",
        category: GoodsCategory? = null,
        discountPrice: Double = 0.0,
        discountTag: String = "",
        tag: String = "",
        stock: Int = 0,
        isSoldOut: Boolean = false,
        isHot: Boolean = false,
        purchaseStatus: Int = GoodsStateInfo.PURCHASE_STATUS_NO_PURCHASE,
        soldCount: Long = 0L,
        unit: String = ""
    ) : this(
        baseInfo = GoodsBaseInfo(
            id = id,
            storeId = storeId,
            name = name,
            message = message,
            category = category
        ),
        priceInfo = GoodsPriceInfo(
            price = price,
            discountPrice = discountPrice,
            discountTag = discountTag
        ),
        uiInfo = GoodsUiInfo(
            pic = pic,
            tag = tag,
            unit = unit
        ),
        stateInfo = GoodsStateInfo(
            saleNumber = sale_number,
            stock = stock,
            isSoldOut = isSoldOut,
            isHot = isHot,
            purchaseStatus = purchaseStatus,
            soldCount = soldCount
        )
    )

    fun getLocationInfo(): String {
        return "${name.ifBlank { "商品" }}：$message"
    }

    companion object {
        fun get20RepeatGoods(): List<Goods> = GoodsSampleData.get20RepeatGoods()

        fun getSingleVirtualGoods(): Goods = GoodsSampleData.getSingleVirtualGoods()
    }

    enum class GoodsCategory(val desc: String) {
        DIGITAL("数码产品"),
        CLOTHING("服饰鞋帽"),
        HOME("家居用品"),
        BOOK("图书文具"),
        BEAUTY("美妆护肤"),
        SPORT("运动器材"),
        FOOD("食品"),
        OTHER("其他物品")
    }
}
