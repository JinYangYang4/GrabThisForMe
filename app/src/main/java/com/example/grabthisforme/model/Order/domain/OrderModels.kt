package com.example.grabthisforme.model.order.domain

import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.user.domain.User

data class OrderIdentity(
    val orderId: String
)

data class OrderParties(
    val sender: User? = null,
    val buyer: User
)

data class OrderGoodsInfo(
    val goods: Goods
)

data class OrderRouteInfo(
    val shelfNumber: String = "",  // 货架编号
    val aimPosition: String = "",   // 目标位置
    val atPosition: String = ""     // 当前位置
)

data class OrderTimeInfo(
    val startTime: Long = 0L,
    val endTime: Long = 0L
)

data class OrderStatusInfo(
    val status: Int = STATUS_PENDING_RECEIPT,
    val isAccepted: Boolean = false
) {
    companion object {
        const val STATUS_PENDING_RECEIPT = 0
        const val STATUS_PENDING_DELIVERY = 1
        const val STATUS_COMPLETED = 2
    }
}
