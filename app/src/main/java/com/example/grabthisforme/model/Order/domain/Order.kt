package com.example.grabthisforme.model.order.domain

import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.user.domain.User

data class Order(
    val identity: OrderIdentity,
    val parties: OrderParties,
    val goodsInfo: OrderGoodsInfo,
    val routeInfo: OrderRouteInfo,
    val timeInfo: OrderTimeInfo,
    val statusInfo: OrderStatusInfo = OrderStatusInfo(),
    val isBuyerSelf: Boolean = false
) {
    val orderId: String get() = identity.orderId
    val sender: User? get() = parties.sender
    val buyer: User get() = parties.buyer
    val goods: Goods get() = goodsInfo.goods
    val shelfNumber: String get() = routeInfo.shelfNumber
    val shelf_number: String get() = shelfNumber
    val aimPosition: String get() = routeInfo.aimPosition
    val aim_position: String get() = aimPosition
    val atPosition: String get() = routeInfo.atPosition
    val at_position: String get() = atPosition
    val startTime: Long get() = timeInfo.startTime
    val endTime: Long get() = timeInfo.endTime
    val orderStatus: Int get() = statusInfo.status
    val isAccepted: Boolean get() = statusInfo.isAccepted

    constructor(
        sender: User? = null,
        orderId: String,
        buyer: User,
        goods: Goods,
        shelf_number: String = "",
        aim_position: String = "",
        at_position: String = "",
        startTime: Long = 0L,
        endTime: Long = 0L,
        orderStatus: Int = OrderStatusInfo.STATUS_PENDING_RECEIPT,
        isAccepted: Boolean = false,
        isBuyerSelf: Boolean = false
    ) : this(
        identity = OrderIdentity(orderId),
        parties = OrderParties(sender = sender, buyer = buyer),
        goodsInfo = OrderGoodsInfo(goods),
        routeInfo = OrderRouteInfo(
            shelfNumber = shelf_number,
            aimPosition = aim_position,
            atPosition = at_position
        ),
        timeInfo = OrderTimeInfo(
            startTime = startTime,
            endTime = endTime
        ),
        statusInfo = OrderStatusInfo(
            status = orderStatus,
            isAccepted = isAccepted
        ),
        isBuyerSelf = isBuyerSelf
    )

    fun isExpired(currentTime: Long = System.currentTimeMillis()): Boolean {
        return currentTime > endTime && endTime > 0L
    }
}
