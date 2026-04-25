package com.example.grabthisforme.model.goods.data.mock

import com.example.grabthisforme.model.goods.domain.Goods
import java.util.UUID

internal object GoodsSampleData {
    fun get20RepeatGoods(): List<Goods> {
        val goodsList = mutableListOf<Goods>()
        repeat(20) { index ->
            goodsList.add(
                Goods(
                    id = (index + 1).toLong(),
                    name = "经典麦丽素${index + 1}",
                    message = "一口酥脆，满心甜蜜，买一送一",
                    price = 60.00,
                    sale_number = 500000,
                    pic = "food_pic",
                    category = Goods.GoodsCategory.FOOD,
                    discountPrice = 49.9,
                    discountTag = "限时优惠",
                    tag = "秒送",
                    stock = 100,
                    isHot = true
                )
            )
        }
        return goodsList
    }

    @Volatile
    private var mockSingleGoods: Goods? = null

    fun getSingleVirtualGoods(): Goods {
        if (mockSingleGoods == null) {
            synchronized(this) {
                if (mockSingleGoods == null) {
                    mockSingleGoods = Goods(
                        id = 1000 + (1..999).random().toLong(),
                        name = "经典麦丽素${UUID.randomUUID().toString().substring(0, 4)}",
                        message = "一口酥脆，满心甜蜜，买一送一，限时特惠",
                        price = 59.9 + (0..9).random() * 0.1,
                        sale_number = 500000 + (1..10000).random().toLong(),
                        pic = "food_pic_${(1..10).random()}",
                        category = Goods.GoodsCategory.FOOD,
                        discountPrice = 48.8,
                        discountTag = "特价",
                        tag = "热销",
                        stock = 88,
                        isHot = true
                    )
                }
            }
        }
        return mockSingleGoods!!
    }
}

