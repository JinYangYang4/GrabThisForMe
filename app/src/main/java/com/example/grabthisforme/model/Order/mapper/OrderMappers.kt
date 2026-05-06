package com.example.grabthisforme.model.order.mapper

import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.order.data.dto.OrderDto
import com.example.grabthisforme.model.order.data.entity.OrderEntity
import com.example.grabthisforme.model.order.domain.Order
import com.example.grabthisforme.model.order.domain.OrderGoodsInfo
import com.example.grabthisforme.model.order.domain.OrderIdentity
import com.example.grabthisforme.model.order.domain.OrderParties
import com.example.grabthisforme.model.order.domain.OrderRouteInfo
import com.example.grabthisforme.model.order.domain.OrderStatusInfo
import com.example.grabthisforme.model.order.domain.OrderTimeInfo
import com.example.grabthisforme.model.user.domain.User

private fun buildUser(
    userId: Long?,
    name: String,
    avatarUrl: String
): User? {
    if (userId == null && name.isBlank() && avatarUrl.isBlank()) {
        return null
    }
    return User(
        id = userId ?: 0L,
        name = name.ifBlank { "Anonymous" },
        headPic = avatarUrl
    )
}

private fun buildGoods(
    goodsId: Long,
    name: String,
    message: String,
    price: Double,
    pic: String
): Goods {
    return Goods(
        id = goodsId,
        name = name,
        message = message,
        price = price,
        pic = pic
    )
}

fun OrderDto.toDomain(): Order {
    return Order(
        identity = OrderIdentity(orderId),
        parties = OrderParties(
            sender = buildUser(senderId, senderName, senderAvatarUrl),
            buyer = User(
                id = buyerId,
                name = buyerName.ifBlank { "Anonymous" },
                headPic = buyerAvatarUrl
            )
        ),
        goodsInfo = OrderGoodsInfo(
            goods = buildGoods(
                goodsId = goodsId,
                name = goodsName,
                message = goodsMessage,
                price = goodsPrice,
                pic = goodsPic
            )
        ),
        routeInfo = OrderRouteInfo(
            shelfNumber = shelfNumber,
            aimPosition = aimPosition,
            atPosition = atPosition
        ),
        timeInfo = OrderTimeInfo(
            startTime = startTime,
            endTime = endTime
        ),
        statusInfo = OrderStatusInfo(
            status = orderStatus,
            isAccepted = isAccepted
        )
    )
}

fun Order.toDto(): OrderDto {
    return OrderDto(
        orderId = orderId,
        senderId = sender?.id,
        senderName = sender?.name.orEmpty(),
        senderAvatarUrl = sender?.headPic.orEmpty(),
        buyerId = buyer.id,
        buyerName = buyer.name,
        buyerAvatarUrl = buyer.headPic,
        goodsId = goods.id,
        goodsName = goods.name,
        goodsMessage = goods.message,
        goodsPrice = goods.price,
        goodsPic = goods.pic,
        shelfNumber = shelfNumber,
        aimPosition = aimPosition,
        atPosition = atPosition,
        startTime = startTime,
        endTime = endTime,
        orderStatus = orderStatus,
        isAccepted = isAccepted
    )
}

fun OrderEntity.toDomain(): Order {
    return Order(
        identity = OrderIdentity(orderId),
        parties = OrderParties(
            sender = buildUser(senderId, senderName, senderAvatarUrl),
            buyer = User(
                id = buyerId,
                name = buyerName.ifBlank { "Anonymous" },
                headPic = buyerAvatarUrl
            )
        ),
        goodsInfo = OrderGoodsInfo(
            goods = buildGoods(
                goodsId = goodsId,
                name = goodsName,
                message = goodsMessage,
                price = goodsPrice,
                pic = goodsPic
            )
        ),
        routeInfo = OrderRouteInfo(
            shelfNumber = shelfNumber,
            aimPosition = aimPosition,
            atPosition = atPosition
        ),
        timeInfo = OrderTimeInfo(
            startTime = startTime,
            endTime = endTime
        ),
        statusInfo = OrderStatusInfo(
            status = orderStatus,
            isAccepted = isAccepted
        )
    )
}

fun Order.toEntity(): OrderEntity {
    return OrderEntity(
        orderId = orderId,
        senderId = sender?.id,
        senderName = sender?.name.orEmpty(),
        senderAvatarUrl = sender?.headPic.orEmpty(),
        buyerId = buyer.id,
        buyerName = buyer.name,
        buyerAvatarUrl = buyer.headPic,
        goodsId = goods.id,
        goodsName = goods.name,
        goodsMessage = goods.message,
        goodsPrice = goods.price,
        goodsPic = goods.pic,
        shelfNumber = shelfNumber,
        aimPosition = aimPosition,
        atPosition = atPosition,
        startTime = startTime,
        endTime = endTime,
        orderStatus = orderStatus,
        isAccepted = isAccepted
    )
}
