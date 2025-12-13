package com.example.grabthisforme.model.goos

class Goods(
    val id: Long,
    val name: String,
    val message: String,
    val price: Double,
    val shop: String,
    val sale: Long,
    val pic: String
) {
    companion object {
        fun get20RepeatGoods(): List<Goods> {
            val baseGoods = Goods(
                id = 1,
                name = "经典麦丽素",
                message = "一口酥脆，满心甜蜜！买一送一",
                price = 60.00,
                shop = "零食优选旗舰店",
                sale = 500000,
                pic = "food_pic"
            )
            val goodsList = mutableListOf<Goods>()
            repeat(20) { index ->
                val newGoods = Goods(
                    id = 1 + index.toLong(),
                    name = "经典麦丽素",
                    message = "一口酥脆，满心甜蜜！买一送一",
                    price = 60.00,
                    shop = "零食优选旗舰店",
                    sale = 500000,
                    pic = "food_pic"
                )
                goodsList.add(newGoods)
            }
            return goodsList
        }
    }
}