package com.example.grabthisforme.model.goods

import java.util.UUID

open class Goods(
    val id: Long,
    val name: String = "",
    val message: String = "",
    val price: Double = 0.0,
    val sale_number: Long = 0,
    val pic: String = "",
    val category: GoodsCategory?= null,
    val discountPrice: Double = 0.0,    // 优惠价 / 秒杀价
    val discountTag: String = "",       // 优惠标签（如：买3件优惠）
    val tag: String = "",               // 角标标签（秒送/热销/新品/限购）
    val stock: Int = 0,                 // 库存
    val isSoldOut: Boolean = false,     // 是否售罄
    val isHot: Boolean = false,         // 是否热销
    val unit: String = "",
    var selectedCount: Int = 0
) {

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
    companion object {
        fun get20RepeatGoods(): List<Goods> {
            val goodsList = mutableListOf<Goods>()
            repeat(20) { index ->
                goodsList.add(
                    Goods(
                        id = (index + 1).toLong(),
                        name = "经典麦丽素_${index + 1}",
                        message = "一口酥脆，满心甜蜜！买一送一",
                        price = 60.00,
                        sale_number = 500000,
                        pic = "food_pic",

                        // 补全
                        category = GoodsCategory.FOOD,
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
                        // 构建单个虚拟商品（补充配送时间、货架号、目标位置等字段）
                        val currentTime = System.currentTimeMillis()
                        // 配送开始时间：当前时间 + 30分钟
                        val startTime = currentTime + 30 * 60 * 1000
                        // 配送结束时间：当前时间 + 2小时
                        val endTime = currentTime + 2 * 60 * 60 * 1000

                        mockSingleGoods = Goods(
                            id = 1000 + (1..999).random().toLong(), // 随机ID（1000-1999）
                            name = "经典麦丽素_${UUID.randomUUID().toString().substring(0, 4)}", // 带随机后缀
                            message = "一口酥脆，满心甜蜜！买一送一，限时特惠",
                            price = 59.9 + (0..9).random() * 0.1, // 随机价格（59.9-60.8）
                            sale_number = 500000 + (1..10000).random().toLong(), // 随机销量
                            pic = "food_pic_${(1..10).random()}", // 随机图片标识
                        )
                    }
                }
            }
            return mockSingleGoods!!
        }

    }
}