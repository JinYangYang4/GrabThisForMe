package com.example.grabthisforme.model.order.data.dto

data class OrderDto(
    val orderId: String,
    val senderId: Long? = null,
    val senderName: String = "",
    val senderAvatarUrl: String = "",
    val buyerId: Long = 0L,
    val buyerName: String = "",
    val buyerAvatarUrl: String = "",
    val goodsId: Long = 0L,
    val goodsName: String = "",
    val goodsMessage: String = "",
    val goodsPrice: Double = 0.0,
    val goodsPic: String = "",
    val shelfNumber: String = "",
    val aimPosition: String = "",
    val atPosition: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L
)
