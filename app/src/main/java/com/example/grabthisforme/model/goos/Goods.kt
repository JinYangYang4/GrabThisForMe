package com.example.grabthisforme.model.goos

import com.example.grabthisforme.model.store.Store

open class Goods(
    val id: Long,
    val name: String,
    val message: String,
    var shelf_number : String = "",
    var aim_position : String= "",
    val price: Double,
    val store: Store?=null,
    val sale_number: Long,
    val pic: String,
    var startTime: Long = 0,
    var endTime: Long= 0
) {
    companion object {
        fun get20RepeatGoods(): List<Goods> {
            val baseGoods = Goods(
                id = 1,
                name = "经典麦丽素",
                message = "一口酥脆，满心甜蜜！买一送一",
                price = 60.00,
                sale_number = 500000,
                pic = "food_pic"
            )
            val goodsList = mutableListOf<Goods>()
            repeat(20) { index ->
                val newGoods = Goods(
                    id = 1 + index.toLong(),
                    name = "经典麦丽素",
                    message = "一口酥脆，满心甜蜜！买一送一",
                    price = 60.00,
                    sale_number = 500000,
                    pic = "food_pic"
                )
                goodsList.add(newGoods)
            }
            return goodsList
        }
    }
}