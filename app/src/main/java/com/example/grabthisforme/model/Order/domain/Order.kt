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
    val purchaseInfo: OrderPurchaseInfo = OrderPurchaseInfo(),
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
    val quantity: Int get() = goodsInfo.quantity
    val unitPrice: Double get() = goodsInfo.unitPrice
    val totalAmount: Double get() = goodsInfo.totalAmount
    val orderType: String get() = purchaseInfo.orderType
    val purchaseId: String? get() = purchaseInfo.purchaseId
    val storeId: Long get() = purchaseInfo.storeId
    val storeName: String get() = purchaseInfo.storeName
    val subtotalAmount: Double get() = purchaseInfo.subtotalAmount
    val discountAmount: Double get() = purchaseInfo.discountAmount
    val userCouponId: String? get() = purchaseInfo.userCouponId

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
        quantity: Int = 1,
        unitPrice: Double = goods.price,
        totalAmount: Double = unitPrice * quantity,
        orderType: String = OrderPurchaseInfo.TYPE_ERRAND,
        purchaseId: String? = null,
        storeId: Long = goods.storeId,
        storeName: String = "",
        subtotalAmount: Double = totalAmount,
        discountAmount: Double = 0.0,
        userCouponId: String? = null,
        isBuyerSelf: Boolean = false
    ) : this(
        identity = OrderIdentity(orderId),
        parties = OrderParties(sender = sender, buyer = buyer),
        goodsInfo = OrderGoodsInfo(goods, quantity, unitPrice, totalAmount),
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
        purchaseInfo = OrderPurchaseInfo(
            orderType, purchaseId, storeId, storeName, subtotalAmount, discountAmount, userCouponId
        ),
        isBuyerSelf = isBuyerSelf
    )

    fun isExpired(currentTime: Long = System.currentTimeMillis()): Boolean {
        return currentTime > endTime && endTime > 0L
    }
}
