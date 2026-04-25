package com.example.grabthisforme.model.secondhandGoods.domain

import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.goods.domain.GoodsBaseInfo
import com.example.grabthisforme.model.goods.domain.GoodsPriceInfo
import com.example.grabthisforme.model.goods.domain.GoodsStateInfo
import com.example.grabthisforme.model.goods.domain.GoodsUiInfo
import com.example.grabthisforme.model.secondhandGoods.data.mock.SecondhandGoodsSampleData
import com.example.grabthisforme.model.user.domain.User

open class SecondhandGoods(
    saleUser: User? = null,
    id: Long,
    name: String,
    message: String,
    secondhandPrice: Double,
    sale_number: Long,
    pic: String,
    val originalPrice: Double,
    val quality: String,
    val usedTime: String?,
    val tradeStatus: Int = SecondhandTradeInfo.STATUS_ON_SALE,
    val negotiable: Boolean = true
) : Goods(
    baseInfo = GoodsBaseInfo(
        id = id,
        name = name,
        message = message,
        category = Goods.GoodsCategory.CLOTHING
    ),
    priceInfo = GoodsPriceInfo(
        price = secondhandPrice
    ),
    uiInfo = GoodsUiInfo(
        pic = pic
    ),
    stateInfo = GoodsStateInfo(
        saleNumber = sale_number
    )
) {
    val saleUser: User? = saleUser

    val tradeInfo: SecondhandTradeInfo = SecondhandTradeInfo(
        saleUser = saleUser,
        originalPrice = originalPrice,
        quality = quality,
        usedTime = usedTime,
        tradeStatus = tradeStatus,
        negotiable = negotiable
    )

    companion object SecondhandGoodsMockData {
        fun generateMockData(count: Int): List<SecondhandGoods> = SecondhandGoodsSampleData.generateMockData(count)

        fun generateDefaultMockData(): List<SecondhandGoods> = SecondhandGoodsSampleData.generateDefaultMockData()
    }
}
