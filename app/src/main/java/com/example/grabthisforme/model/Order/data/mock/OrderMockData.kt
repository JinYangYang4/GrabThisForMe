package com.example.grabthisforme.model.order.data.mock

import com.example.grabthisforme.model.order.data.network.dto.OrderDto
import com.example.grabthisforme.model.order.domain.Order
import com.example.grabthisforme.model.order.mapper.toDomain

object OrderMockData {

    private var buyerSnapshots: List<OrderDto>? = null
    private var orderList: List<Order>? = null

    private fun getBuyerSnapshots(): List<OrderDto> {
        if (buyerSnapshots == null) {
            buyerSnapshots = listOf(
                OrderDto(
                    orderId = "buyer_10001",
                    buyerId = 10001L,
                    buyerName = "ZhangSan",
                    buyerAvatarUrl = "avatar_zhangsan"
                ),
                OrderDto(
                    orderId = "buyer_10002",
                    buyerId = 10002L,
                    buyerName = "LiSi",
                    buyerAvatarUrl = "avatar_lisi"
                ),
                OrderDto(
                    orderId = "buyer_10003",
                    buyerId = 10003L,
                    buyerName = "WangWu",
                    buyerAvatarUrl = "avatar_wangwu"
                ),
                OrderDto(
                    orderId = "buyer_10004",
                    buyerId = 10004L,
                    buyerName = "ZhaoLiu",
                    buyerAvatarUrl = "avatar_zhaoliu"
                ),
                OrderDto(
                    orderId = "buyer_10005",
                    buyerId = 10005L,
                    buyerName = "SunQi",
                    buyerAvatarUrl = "avatar_sunqi"
                )
            )
        }
        return buyerSnapshots!!
    }

    fun getOrderList(): List<Order> {
        if (orderList == null) {
            val buyers = getBuyerSnapshots()
            val now = System.currentTimeMillis()

            val orderDtos = listOf(
                OrderDto(
                    orderId = (now - 100000).toString(),
                    senderId = 20001L,
                    senderName = "Delivery-8",
                    senderAvatarUrl = "delivery_avatar_maba",
                    buyerId = buyers[0].buyerId,
                    buyerName = buyers[0].buyerName,
                    buyerAvatarUrl = buyers[0].buyerAvatarUrl,
                    goodsId = 1001L,
                    goodsName = "Snack combo",
                    goodsMessage = "Original flavor snacks",
                    goodsPrice = 19.9,
                    goodsPic = "food_snack",
                    shelfNumber = "A-08",
                    aimPosition = "Building 2, Unit 502",
                    startTime = now - 10 * 60 * 1000,
                    endTime = now + 3 * 60 * 60 * 1000L
                ),
                OrderDto(
                    orderId = (now - 80000).toString(),
                    senderId = 20002L,
                    senderName = "Delivery-9",
                    senderAvatarUrl = "delivery_avatar_zhoujiu",
                    buyerId = buyers[1].buyerId,
                    buyerName = buyers[1].buyerName,
                    buyerAvatarUrl = buyers[1].buyerAvatarUrl,
                    goodsId = 1002L,
                    goodsName = "Tissue + detergent",
                    goodsMessage = "Home supply set",
                    goodsPrice = 45.5,
                    goodsPic = "daily_item",
                    shelfNumber = "A-08",
                    aimPosition = "Building 2, Unit 502",
                    startTime = now + 1 * 60 * 60 * 1000L,
                    endTime = now + 4 * 60 * 60 * 1000L
                ),
                OrderDto(
                    orderId = (now - 60000).toString(),
                    senderId = 20003L,
                    senderName = "Delivery-10",
                    senderAvatarUrl = "delivery_avatar_wushi",
                    buyerId = buyers[2].buyerId,
                    buyerName = buyers[2].buyerName,
                    buyerAvatarUrl = buyers[2].buyerAvatarUrl,
                    goodsId = 1003L,
                    goodsName = "Bubble tea",
                    goodsMessage = "Less sugar, less ice",
                    goodsPrice = 28.0,
                    goodsPic = "drink",
                    shelfNumber = "B-12",
                    aimPosition = "Mall 5F food court",
                    startTime = now + 30 * 60 * 1000L,
                    endTime = now + 2 * 60 * 60 * 1000L
                ),
                OrderDto(
                    orderId = (now - 40000).toString(),
                    senderId = 20001L,
                    senderName = "Delivery-8",
                    senderAvatarUrl = "delivery_avatar_maba",
                    buyerId = buyers[3].buyerId,
                    buyerName = buyers[3].buyerName,
                    buyerAvatarUrl = buyers[3].buyerAvatarUrl,
                    goodsId = 1004L,
                    goodsName = "Fresh fruit box",
                    goodsMessage = "Choose fresh strawberries",
                    goodsPrice = 32.8,
                    goodsPic = "fresh_food",
                    shelfNumber = "F-15",
                    aimPosition = "Building 1, Unit 101",
                    startTime = now + 30 * 60 * 1000L,
                    endTime = now + 2 * 60 * 60 * 1000L
                ),
                OrderDto(
                    orderId = (now - 20000).toString(),
                    senderId = 20002L,
                    senderName = "Delivery-9",
                    senderAvatarUrl = "delivery_avatar_zhoujiu",
                    buyerId = buyers[4].buyerId,
                    buyerName = buyers[4].buyerName,
                    buyerAvatarUrl = buyers[4].buyerAvatarUrl,
                    goodsId = 1005L,
                    goodsName = "Laptop accessories pack",
                    goodsMessage = "Keyboard and mouse set",
                    goodsPrice = 15.3,
                    goodsPic = "stationery",
                    shelfNumber = "F-15",
                    aimPosition = "Building 1, Unit 101",
                    startTime = now + 1 * 60 * 60 * 1000L,
                    endTime = now + 3 * 60 * 60 * 1000L
                )
            )
            orderList = orderDtos.map { it.toDomain() }
        }
        return orderList!!
    }

    fun getOrderById(orderId: String): Order? {
        return getOrderList().firstOrNull { it.orderId == orderId }
    }
}
